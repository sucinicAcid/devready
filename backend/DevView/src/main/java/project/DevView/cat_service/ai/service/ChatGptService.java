package project.DevView.cat_service.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatGptService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateFollowUpQuestion(String conversationContext, String userAnswer) {
        ChatMessage systemMsg = new ChatMessage("system",
                "당신은 기술면접관입니다. 답변을 보고 꼬리 질문을 생성하세요. 구어체로 답변해주세요.:\n" + conversationContext);
        ChatMessage userMsg = new ChatMessage("user",
                "사용자 답변: " + userAnswer + "\n");

        ChatRequest requestBody = new ChatRequest(
                model,
                new ChatMessage[]{ systemMsg, userMsg },
                100,
                0.8
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            return response.choices()[0].message().content().trim();
        } else {
            return "No follow-up question could be generated.";
        }
    }

    public String getCompletion(String prompt) {
        ChatMessage systemMsg = new ChatMessage("system",
            "Context\n" +
                    "당신은 개발자 기술 면접관입니다.\n" +
                    "주어진 대화 맥락(질문·답변 기록)을 바탕으로 후속 질문을 만들어야 합니다.\n" +
                    "\n" +
                    "Objective\n" +
                    "하나의 심층적인 꼬리 질문을 생성한다. (여러 개 X)\n" +
                    "\n" +
                    "Style\n" +
                    "\n" +
                    "출력 언어: 한국어\n" +
                    "\n" +
                    "표현: 실제 면접 현장에서 쓰는 자연스러운 어휘·문장\n" +
                    "\n" +
                    "Tone\n" +
                    "전문적이고 명확하며 간결하게, 한 문장으로 질문한다.\n" +
                    "\n" +
                    "Audience\n" +
                    "개발자 지원자가 우리가 생성한 질문을 듣는다. \n" +
                    "\n" +
                    "Response\n" +
                    "\n" +
                    "생성된 질문 한 문장을 출력한다.\n" +
                    "\n" +
                    "불필요한 설명·머리말·꼬리말은 포함하지 않는다");
        ChatMessage userMsg = new ChatMessage("user", prompt);

        ChatRequest requestBody = new ChatRequest(
            model,
            new ChatMessage[]{systemMsg, userMsg},
            1000,  // 하나의 질문만 생성하므로 1000 토큰으로 충분
            0.7    // 적절한 창의성
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            String content = response.choices()[0].message().content().trim();
            // 여러 줄의 응답이 있다면 첫 번째 질문만 사용
            return content.split("\n")[0].trim();
        } else {
            throw new RuntimeException("AI 꼬리질문 생성에 실패했습니다.");
        }
    }

    /**
     * 면접 답변을 평가하는 메서드
     * @param chatHistory 대화 기록 (질문과 답변)
     * @return 평가 결과
     */
    public String evaluateAnswer(String chatHistory) {
        ChatMessage systemMsg = new ChatMessage("system", 
            "당신은 자기소개서 기반의 프로젝트 경험 면접을 진행한 기술 면접관입니다. 아래는 지금까지의 질문, 답변 결과입니다.\n\n" +
            "이 데이터를 바탕으로, 이 지원자에 대한 종합 평가를 정중한 어투로 작성하세요.\n" +
            "다음 기준에 따라 이 답변을 1~5점으로 평가하고, 각 항목별로 간단한 이유를 작성하세요.\n" +
            "단, 답변이 명확히 존재하지 않거나 실질적인 내용이 없는 경우 해당 항목에 1점을 부여하고, 그 사유를 명시해 주세요.\n\n" +
            "1. 질문의 의도 파악 : 질문의 핵심을 정확히 이해하고 맞춤으로 답했는가?\n" +
            "2. 답변의 구체성 : 개념이 아닌 실제 경험과 수치를 들어 상세히 설명했는가?\n" +
            "3. 사유와 결과의 일관성 : 문제의 원인, 대응, 결과의 흐름이 일관적인가?\n" +
            "4. 예외 상황에 대한 대응성 : 예상치 못한 상황에 대한 설명이나 대응책이 포함되었는가?\n" +
            "5. 답변/설명의 논리적 구성 : 말의 순서, 구성, 전개 방식이 논리적인가?\n" +
            "---\n" +
            "마지막으로 총점을 계산하세요.\n\n" +
            "총점 (100점 만점) – 위 항목의 평균 점수를 20배하여 계산");
        
        ChatMessage userMsg = new ChatMessage("user", 
            "다음은 면접 대화 기록입니다. 위 기준에 따라 평가해주세요:\n\n" + chatHistory);

        ChatRequest requestBody = new ChatRequest(
            model,
            new ChatMessage[]{systemMsg, userMsg},
            2000,  // 평가는 좀 더 긴 응답이 필요
            0.7    // 적절한 창의성
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            return response.choices()[0].message().content().trim();
        } else {
            throw new RuntimeException("AI 답변 평가 생성에 실패했습니다.");
        }
    }

    /**
     * CS 기술 면접 답변을 평가하는 메서드
     * @param chatHistory 대화 기록 (질문과 답변)
     * @return 평가 결과
     */
    public String evaluateCSAnswer(String chatHistory) {
        ChatMessage systemMsg = new ChatMessage("system", 
            "당신은 컴퓨터공학 기술 면접관입니다. 아래는 지금까지의 질문, 답변 결과입니다.\n\n" +
            "이 데이터를 바탕으로, 이 지원자에 대한 종합 평가를 작성하세요.\n" +
            "다음 기준에 따라 이 답변을 1~5점으로 평가하고, 각 항목별로 간단한 이유를 작성하세요:\n\n" +
            "1. 정확성 : 기술한 내용이 사실과 부합하는가? 핵심 개념을 정확히 설명했는가?\n" +
            "2. 완전성 : 질문에 요구되는 설명이 충분한가? 중요한 개념이 빠지지 않았는가?\n" +
            "3. 표현력 : 명확하고 이해하기 쉽게 설명했는가? 용어 사용이 적절한가?\n" +
            "4. 간결성 : 응답 길이가 말하기 기준 1분 이내로 적절한가?\n\n" +
            "---\n" +
            "또한 아래 항목을 포함해야 합니다:\n\n" +
            "1. 총점 (100점 만점) – 항목별 평균 점수 기반\n" +
            "2. 강점 – 어떤 역량이나 자세가 특히 뛰어났는가?\n" +
            "3. 개선점 – 반복적으로 아쉬웠던 부분은 무엇인가?\n" +
            "4. 추천 준비 방향 – 다음 면접을 위한 구체적인 조언\n" +
            "5. 답변 스타일 및 태도 관련 종합 피드백 – 커뮤니케이션 및 태도 중심 피드백");
        
        ChatMessage userMsg = new ChatMessage("user", 
            "다음은 CS 기술 면접 대화 기록입니다. 위 기준에 따라 평가해주세요:\n\n" + chatHistory);

        ChatRequest requestBody = new ChatRequest(
            model,
            new ChatMessage[]{systemMsg, userMsg},
            2000,  // 평가는 좀 더 긴 응답이 필요
            0.7    // 적절한 창의성
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            return response.choices()[0].message().content().trim();
        } else {
            throw new RuntimeException("AI CS 답변 평가 생성에 실패했습니다.");
        }
    }

    /**
     * 건너뛰기 후 다음 질문을 가다듬는 메서드
     * @param originalQuestion 원래 질문
     * @return 가다듬어진 질문
     */
    public String refineQuestionAfterSkip(String originalQuestion) {
        ChatMessage systemMsg = new ChatMessage("system", 
            "당신은 개발자 기술 면접관입니다. 사용자가 이전 질문에 대해 '모르겠습니다'라고 답변했을 때, 자연스럽게 다음 질문으로 넘어가기 위한 쿠션어를 포함하여 질문을 가다듬어주세요.\n\n" +
            "다음 원칙을 따라주세요:\n" +
            "1. '그러면 다른 관점에서 질문해보겠습니다' 또는 '다른 주제로 넘어가보겠습니다'와 같은 자연스러운 전환 문구를 사용하세요.\n" +
            "2. 전체 문장이 자연스럽게 이어지도록 하세요.\n" +
            "3. 한국어로 작성하되, 정중하고 전문적인 어투를 사용하세요.");
        
        ChatMessage userMsg = new ChatMessage("user", 
            "다음 질문을 가다듬어주세요:\n" + originalQuestion);

        ChatRequest requestBody = new ChatRequest(
            model,
            new ChatMessage[]{systemMsg, userMsg},
            500,  // 짧은 응답이므로 500 토큰으로 충분
            0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            return response.choices()[0].message().content().trim();
        } else {
            // 실패 시 원래 질문 반환
            return originalQuestion;
        }
    }

    /**
     * 질문을 COSTAR 형식으로 사람의 어휘로 변환하는 메서드
     * @param originalQuestion 원래 질문
     * @return 변환된 질문
     */
    public String convertQuestionToCostarFormat(String originalQuestion) {
        ChatMessage systemMsg = new ChatMessage("system", 
            "당신은 개발자 기술 면접관입니다. 주어진 질문을 사람의 어휘로 자연스럽게 변환해주세요.\n\n" +
            "변환 원칙:\n" +
            "1. 기술적 용어는 유지하되, 자연스러운 대화체로 변환\n" +
            "2. 실제 면접에서 사용할 수 있는 친근하면서도 전문적인 어투\n" +
            "3. 구체적인 경험을 묻는 형태로 변환\n" +
            "4. 한 문장으로 자연스럽게 연결\n" +
            "5. 한국어로 작성");
        
        ChatMessage userMsg = new ChatMessage("user", 
            "다음 질문을 사람의 어휘로 변환해주세요:\n" + originalQuestion);

        ChatRequest requestBody = new ChatRequest(
            model,
            new ChatMessage[]{systemMsg, userMsg},
            1000,  // 적절한 길이의 응답
            0.7    // 적절한 창의성
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";
        ChatResponse response = restTemplate.postForObject(url, entity, ChatResponse.class);

        if (response != null && response.choices() != null && response.choices().length > 0) {
            return response.choices()[0].message().content().trim();
        } else {
            // 실패 시 원래 질문 반환
            return originalQuestion;
        }
    }

    // DTO 클래스들
    record ChatRequest(
        String model,
        ChatMessage[] messages,
        int max_tokens,
        double temperature
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