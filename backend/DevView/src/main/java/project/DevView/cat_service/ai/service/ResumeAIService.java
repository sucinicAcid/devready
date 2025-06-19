package project.DevView.cat_service.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.DevView.cat_service.resume.dto.ResumeTagResponse;
import project.DevView.cat_service.resume.dto.TagQuestionResponse;
import project.DevView.cat_service.resume.entity.ResumeTag;
import project.DevView.cat_service.resume.entity.TagType;
import project.DevView.cat_service.resume.dto.ResumeTagForQuestionDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern NEWLINE_IN_QUOTES = Pattern.compile("\"([^\"]*?)\r?\n([^\"]*?)\"");
private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("(?s)^```(?:json)?\\s*|```[\\r\\n]*$");  // 끝부분 공백·개행까지 제거

/* ------------- ② 메서드 전체 교체 ------------- */
/* cleanJsonResponse ― 마지막 return 직전에 한 줄 추가 */
    /**
     * OpenAI가 돌려준 문자열에서
     * 1) ``` … ``` 코드블록 제거
     * 2) JSON이 실제로 시작되는 지점부터 끝까지만 취득
     * 3) 문자열 내부의 개행( \n , \r\n )을 \\n 으로 이스케이프
     * - 그 외 문자는 건드리지 않는다
     */
    private String cleanJsonResponse(String raw) {
        if (raw == null || raw.isBlank()) return "";

        /* --- 1. 코드블록 fence 제거 --- */
        String s = raw.replaceAll("(?s)```(?:json)?\\s*|```\\s*", "").trim();

        /* --- 2. JSON 본문만 자르기 --- */
        int obj = s.indexOf('{'), arr = s.indexOf('[');
        int start = (obj < 0) ? arr : (arr < 0 ? obj : Math.min(obj, arr));
        if (start < 0) throw new IllegalStateException("JSON 시작 문자({ or [) 없음");
        s = s.substring(start);                          // 앞 머리 자르고
        int endObj = s.lastIndexOf('}');                 // 맨 끝 위치 찾기
        int endArr = s.lastIndexOf(']');
        int end   = Math.max(endObj, endArr);
        if (end >= 0) s = s.substring(0, end + 1);       // 꼬리 자르기

        /* --- 3. 문자열 내부 개행 → \\n --- */
        StringBuilder sb = new StringBuilder(s.length() + 32);
        boolean inStr = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // 따옴표 안/밖 판별 (백슬래시 이스케이프 고려)
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inStr = !inStr;
                sb.append(c);
                continue;
            }

            if (inStr) {
                if (c == '\r') continue;           // CR 버림
                if (c == '\n') {
                    sb.append("\\n");               // LF → \n
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }


    private void saveFailedResponse(String raw, String cleaned, Exception e) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dir = "logs/ai-responses";
            Files.createDirectories(Path.of(dir));

            // 원본 응답 저장
            Files.writeString(
                Path.of(dir, String.format("failed-response-%s-raw.txt", timestamp)),
                String.format("=== 원본 응답 ===\n%s\n\n=== 예외 ===\n%s", raw, e)
            );

            // 정제된 JSON 저장
            if (cleaned != null) {
                Files.writeString(
                    Path.of(dir, String.format("failed-response-%s-cleaned.txt", timestamp)),
                    cleaned
                );
            }
        } catch (IOException ex) {
            log.error("실패한 응답 저장 실패", ex);
        }
    }

    public ResumeTagResponse generateTags(String classifiedItemsJson) {
        log.info("[ResumeAIService] 태그 생성 시작");

        /* ---------- 1. Prompt 구성 ---------- */
        String fullPrompt = """
            너는 **시니어 기술면접관兼프롬프트 엔지니어**다.

            ### 입력
            - `[CLASSIFIED_ITEMS]` : 지원자 이력서에서 문장·항목만 추출해 나열한 **문장 배열(JSON)**

            ### 목표
            1. 이력서에서 **깊게 질문할 만한 핵심 키워드** 10개를 뽑는다. 기술적으로 어려운 문제를 해결했거나 본인이 열심히 구현을 하고 해결했다 보이는 키워드를 뽑을수록 좋다.
            2. 각 키워드에 대해
               - `tagType` : 아래 TagType enum 중 하나
               - `keyword` : 원문 키워드 / 문구
               - `detail`  : 키워드가 등장한 문장(원문 유지)
               - `depth_score` (1–5)
               - `priority_score` (0–100)
               을 산출한다.
            3. **TagQuestion은 이번 응답에 포함하지 않는다.**

            ---
            ### TagType 정의 & depth_score 기준 (요약)
            | Tag | 상황 | 키워드별 질문 할 포인트 |
            | --- | --- | --- |
            | **1 TECH_ONLY** | 기술만 언급, 문제·성과 불명 | … (표 전체 그대로) |
            | **2 TECH_PROBLEM** | 기술로 문제 해결 | … |
            | **3 TECH_FEATURE** | 기술로 기능 구현 | … |
            | **4 PERF_TUNING** | 성능 개선 | … |
            | **5 FEATURE_ONLY** | 기능만 언급 | … |
            | **6 MUST_ASK** | 중요하지만 불명확 | … |

            depth_score의 개념 — "얼마나 깊이 · 구체적으로 기술했는가?"
            (표 전체 생략)

            ## 우선순위(priority_score) 산정
            ① Tag 가중치  2:+30  4:+25  3:+20  1:+10  5·6:+5
            ② 추가 보정  depth_score×4 (≤+20) + has_how(+5) + good_points≥2(+3)
            ③ 동점  depth_score 큰 순 → keyword 알파벳순
            ④ priority_score ∈ [0-100]

            ---
            ### 출력 스펙(JSON) — **반드시 다음 형식 유지**
            {
              "keywords": [
                {
                  "tagType"       : "TECH_ONLY",
                  "keyword"       : "<원문 키워드>",
                  "detail"        : "<원문 문장>",
                  "depth_score"   : 4,
                  "priority_score": 78
                },
                ...
              ],
              "advanced_points": [ "<심화 논의 포인트>", … ],
              "detected_gaps" : [ "<미진한 부분>", … ]
            }

            ---
            [CLASSIFIED_ITEMS]:
            %s

            ---
            주의사항:
            1. 반드시 유효한 JSON 객체를 반환하세요.
            2. 문자열 값 안의 개행은 \\n으로, 따옴표는 \\"로 이스케이프하세요.
            3. 마크다운 코드블록(```)으로 감싸지 마세요.
            4. 모든 문자열은 반드시 큰따옴표(")로 감싸세요.
            """.formatted(classifiedItemsJson);

        ChatMessage systemMsg = new ChatMessage("system",
                "당신은 이력서 분석 전문가입니다. 주어진 스펙에 따라 JSON 결과를 생성하세요. " +
                "반드시 순수 JSON만 반환하고, 문자열 안의 특수문자는 이스케이프하세요.");
        ChatMessage userMsg   = new ChatMessage("user", fullPrompt);

        ChatRequest requestBody = new ChatRequest(
                model,
                new ChatMessage[]{systemMsg, userMsg},
                4096,  // 토큰 여유 확보
                0.7,
                Map.of("type", "json_object")  // JSON 형식 강제
        );

        /* ---------- 2. OpenAI 호출 ---------- */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        /* ---------- 3. 결과 파싱 ---------- */
        if (response == null || response.choices() == null || response.choices().length == 0) {
            log.error("[ResumeAIService] OpenAI 응답이 비어있습니다");
            throw new RuntimeException("태그 생성에 실패했습니다.");
        }

        String rawResponse = response.choices()[0].message().content().trim();
        log.debug("[ResumeAIService] AI 응답 원문: {}", rawResponse);

        String jsonResponse = null;
        try {
            jsonResponse = cleanJsonResponse(rawResponse);
            log.debug("[ResumeAIService] 정제된 JSON: {}", jsonResponse);

            Map<String, Object> parsed = objectMapper.readValue(jsonResponse, Map.class);

            /* 3-1. keywords 파싱 */
            List<Map<String, Object>> keywordsRaw = (List<Map<String, Object>>) parsed.get("keywords");
            if (keywordsRaw == null) {
                log.error("[ResumeAIService] keywords 필드가 없습니다");
                throw new RuntimeException("태그 생성 응답에 keywords 필드가 없습니다");
            }

            List<ResumeTagResponse.KeywordItem> keywords = new ArrayList<>();
            for (Map<String, Object> kw : keywordsRaw) {
                keywords.add(
                        ResumeTagResponse.KeywordItem.builder()
                                .tagType(TagType.valueOf((String) kw.get("tagType")))
                                .keyword((String) kw.get("keyword"))
                                .detail((String) kw.get("detail"))
                                .depthScore(((Number) kw.get("depth_score")).intValue())
                                .priorityScore(((Number) kw.get("priority_score")).intValue())
                                .build()
                );
            }

            /* 3-2. 보조 정보 파싱 */
            List<String> advancedPoints = (List<String>) parsed.getOrDefault("advanced_points", List.of());
            List<String> detectedGaps   = (List<String>) parsed.getOrDefault("detected_gaps" , List.of());

            log.info("[ResumeAIService] 태그 생성 완료 - 키워드 {}개", keywords.size());
            return ResumeTagResponse.builder()
                    .keywords(keywords)
                    .advancedPoints(advancedPoints)
                    .detectedGaps(detectedGaps)
                    .build();

        } catch (Exception e) {
            log.error("[ResumeAIService] 태그 생성 응답 파싱 실패", e);
            saveFailedResponse(rawResponse, jsonResponse, e);
            throw new RuntimeException("태그 생성 응답 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }
    /* ──────────────────────────────────────────── */
    /**
     * @param keywordItems   generateTags 단계에서 얻은 키워드 목록
     * @return               모든 (keyword × base_question) 쌍에 대한 created_question 리스트
     */
    public TagQuestionResponse generateQuestions(List<ResumeTag> keywordItems) {
        // 엔티티를 DTO로 변환
        List<ResumeTagForQuestionDto> dtoList = keywordItems.stream()
                .map(ResumeTagForQuestionDto::from)
                .toList();

        /* 1️⃣ 입력 배열을 JSON 문자열로 직렬화 */
        String keywordItemsJson;
        try {
            keywordItemsJson = objectMapper.writeValueAsString(dtoList);
        } catch (Exception e) {
            throw new IllegalStateException("KEYWORD_ITEMS 직렬화 실패", e);
        }

        /* 2️⃣ 프롬프트 구성 (요구 사양 전문) */
        String fullPrompt = """
        너는 **시니어 기술 면접관兼프롬프트 엔지니어**다.

        [KEYWORD_ITEMS] 배열을 입력받아 각 항목의 **TagType**에 정의된
        모든 `base_question` 항목을 기반으로 **created_question**(실제 면접 질문)을 생성한다.

        ───────────────────────────────

        ## 입력 스키마
        [KEYWORD_ITEMS] (첫 프롬프트가 준 keywords 배열 그대로)

        /* 요소 예시 */
        {
          "tagType"       : "TECH_ONLY",
          "keyword"       : "Kafka 로그 수집",
          "detail"        : "Kafka Connect로 하루 2 GB 로그를 ...",
          "depth_score"   : 4,
          "priority_score": 78
        }

        > 주의
        > `tagType` 은 TagType(enum) 이름과 정확히 일치한다.
        > 각 TagType-별 base_question 목록은 표를 따른다.

        ───────────────────────────────
        ## TagType ↔ base_question 매핑
        | TagType(enum) | base_question 리스트 |
        | --- | --- |
        | TECH_ONLY | 해결하려던 구체적 문제 또는 가장 해결하기 어려웠던 문제는 무엇인가? |
        | TECH_TO_PROBLEM |  해결 중 맞닥뜨린 주요 난관과 대응 방법은? |
        | TECH_TO_FEATURE |  여러 기술 중 해당 기술이 최적이라고 판단한 이유는? |
        | PERF_TUNING | 선택한 튜닝 전략의 근거는 무엇인가? |
        | FEATURE_ONLY | 가장 어려웠던 기술적 난관은 무엇이고 어떻게 해결했는가? |
        | MUST_ASK | 해당 항목의 배경·동기는?, 프로젝트 내 본인 역할·기여도는?, 가장 어려웠던 결정과 그 근거는 무엇인가? |

        ───────────────────────────────
        ## 생성 지침
        1. keyword(주제)를 문장 안에 자연스럽게 포함할 것.
        2. detail 속 정보를 활용하여 **맥락 있는 질문**으로 다듬을 것.
        3. base_question 의미를 충실히 반영하되 **Yes/No** 답이 불가능하도록 **서술형** 질문으로 변환할 것.
        4. 질문을 구어체로 자연스럽게 만들고 존댓말로 마무리해줘.
        5. 각 base_question마다 **하나의 created_question**을 생성할 것.

        ───────────────────────────────
        ## 출력 스키마 (JSON)
        반드시 다음 형식의 JSON 배열을 반환하세요:
        [
          {
            "keyword"          : "<keyword 원문>",
            "detail"           : "<detail 원문>",
            "base_question"    : "<표의 base_question 원문>",
            "created_question" : "<생성된 실제 질문 문장>"
          },
          ... (모든 키워드 × base_question 조합)
        ]

        > 중요: 반드시 배열([])로 시작하고 끝나야 합니다.
        > 객체({})가 아닌 배열([])을 반환하세요.

        ───────────────────────────────
        ## [KEYWORD_ITEMS]
        %s
        """.formatted(keywordItemsJson);

        /* 3️⃣ OpenAI 요청 */
        ChatMessage systemMsg = new ChatMessage("system",
                "너는 면접 질문 생성기이다. 반드시 JSON 배열([])을 반환하라. 객체({})가 아니다.");
        ChatMessage userMsg = new ChatMessage("user", fullPrompt);

        ChatRequest request = new ChatRequest(
                model,
                new ChatMessage[]{systemMsg, userMsg},
                3200,
                0.7,
                        null                           // ← 루트 배열 반환을 유도 (옵션 제거)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        ChatResponse response = restTemplate.postForObject(
                "https://api.openai.com/v1/chat/completions",
                new HttpEntity<>(request, headers),
                ChatResponse.class
        );

        Objects.requireNonNull(response, "OpenAI 응답 null");
        Objects.requireNonNull(response.choices(), "choices null");
        if (response.choices().length == 0)
            throw new RuntimeException("choices 비어 있음");

        String json = response.choices()[0].message().content().trim();

        /* 4️⃣ 타입-안전 역직렬화 */
        List<QuestionRaw> raws;
        try {
            String cleanedJson = cleanJsonResponse(json);
            raws = objectMapper.readValue(cleanedJson,
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("질문 JSON 파싱 실패 - 원본: {}", json);
            throw new RuntimeException("질문 JSON 파싱 실패: " + e.getMessage(), e);
        }

        /* 5️⃣ DTO 변환 */
        List<TagQuestionResponse.QuestionItem> items = raws.stream()
                .map(r -> TagQuestionResponse.QuestionItem.builder()
                        .keyword(r.keyword())
                        .detail(r.detail())
                        .baseQuestion(r.base_question())
                        .createdQuestion(r.created_question())
                        .build())
                .toList();

        return TagQuestionResponse.builder()
                .questions(items)
                .build();
    }
    record QuestionRaw(
            String keyword,
            String detail,
            String base_question,
            String created_question) {}

    record ChatRequest(
            String model,
            ChatMessage[] messages,
            int max_tokens,
            double temperature,
            Map<String, String> response_format
    ) {}

    record ChatMessage(
            String role,
            String content
    ) {}

    record ChatResponse(
            Choice[] choices
    ) {}

    record Choice(
            int index,
            ChatMessage message,
            String finish_reason
    ) {}


} 