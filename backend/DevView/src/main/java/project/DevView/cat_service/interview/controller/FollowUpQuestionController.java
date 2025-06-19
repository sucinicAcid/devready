package project.DevView.cat_service.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.interview.service.FollowUpQuestionService;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.global.service.ResponseService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviews")
@Tag(name = "Follow-up Question", description = "꼬리질문 생성 관련 API")
public class FollowUpQuestionController {

    private final FollowUpQuestionService followUpQuestionService;
    private final ResponseService responseService;

    @PostMapping("/{interviewId}/follow-up")
    public SuccessResponse<SingleResult<Map<String, String>>> generateFollowUpQuestion(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @Parameter(description = "사용자 답변", example = "{\"answer\": \"운영체제는 컴퓨터 하드웨어와 소프트웨어 자원을 관리하는 시스템 소프트웨어입니다.\"}")
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        String userAnswer = body.get("answer");
        String followUpQuestion = followUpQuestionService.generateFollowUpQuestion(interviewId, userAnswer);
        
        return SuccessResponse.ok(responseService.getSingleResult(Map.of("followUpQuestion", followUpQuestion)));
    }
} 