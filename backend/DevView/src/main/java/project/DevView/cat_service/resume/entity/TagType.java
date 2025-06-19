package project.DevView.cat_service.resume.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum TagType {
    TECH_ONLY("TECH-ONLY", "기술만 언급, 문제·성과 불명", List.of(
        "왜 이 기술을 선택했는가?",
        "해결하려던 구체적 문제 또는 가장 해결하기 어려웠던 문제는 무엇인가?",
        "기존 접근 대비 이 기술의 차별점/장단점은?",
        "기술 적용 시 숨은 제약사항(성능·보안·비용)을 검토했는가?",
        "심화 고려사항(확장성·보안 등)을 평가했는가?"
    )),
    
    TECH_PROBLEM("TECH→PROBLEM", "기술로 문제 해결", List.of(
        "어떤 심화 고려사항(확장성·데이턄 품질·보안 등)을 반영했는가?",
        "해결 과정에서 의사결정 근거(대안 비교·트레이드오프)는?",
        "문제 해결에 해당 기술을 선택한 근거(대안 비교/효과 분석/장점)는?",
        "해결 중 맞닥뜨린 주요 난관과 대응 방법은?",
        "심화 고려사항을 충분히 검토했는가?"
    )),
    
    TECH_FEATURE("TECH→FEATURE", "기술로 기능 구현", List.of(
        "구현 중 맞닥뜨린 주요 난관과 대응 방법은?",
        "기능 요구사항 중 가장 까다로운 항목은 무엇이었고, 어떻게 충족했는가?",
        "해당 기술이 최적이라고 판단한 이유(장·단점, 성능, 비용)는?",
        "테스트 전략/실패·예외 처리는 어떻게 설계했나?",
        "심화 고려사항을 고려했는가?"
    )),
    
    PERF_TUNING("PERF TUNING", "성능 개선", List.of(
        "개선 전후 정량 지표·목표치는?",
        "병목 구간 식별 과정과 사용한 도구·기법은?",
        "선택한 튜닝 전략의 근거는 무엇인가?",
        "심화 고려사항(안정성·비용 등)을 검토했는가?"
    )),
    
    FEATURE_ONLY("FEATURE-ONLY", "기능만 언급", List.of(
        "해당 기능 구현 시 선택한 기술 스택의 결정적 이유는?",
        "가장 어려웠던 기술적 난관은 무엇이고 어떻게 해결했는가?",
        "심화 고려사항(보안·확장성 등)을 평가했는가?"
    )),
    
    MUST_ASK("MUST-ASK", "중요하지만 불명확", List.of(
        "해당 항목의 배경·동기는?",
        "프로젝트 내 본인 역할·기여도는?",
        "가장 어려웠던 결정과 그 근거는 무엇인가?"
    ));

    private final String displayName;
    private final String description;
    private final List<String> baseQuestions;

    public static TagType fromDisplayName(String displayName) {
        for (TagType type : values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tag type: " + displayName);
    }
} 