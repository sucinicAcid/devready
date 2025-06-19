// src/main/java/project/DevView/cat_service/interview/controller/InterviewFlowController.java
package project.DevView.cat_service.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.interview.entity.InterviewMessage;
import project.DevView.cat_service.interview.service.InterviewFlowService;
import project.DevView.cat_service.question.entity.Question;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.global.dto.response.result.ListResult;
import project.DevView.cat_service.global.service.ResponseService;
import project.DevView.cat_service.interview.dto.InterviewMessageDto;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviews")
@Tag(name = "Interview Flow", description = "인터뷰 진행 관련 API")
public class InterviewFlowController {

    private final InterviewFlowService flow;

    @GetMapping("/{interviewId}/next-question")
    @Operation(
        summary = "다음 질문 가져오기",
        description = "현재 인터뷰의 다음 질문을 가져옵니다. 더 이상 질문이 없는 경우 noMore가 true로 반환됩니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "성공적으로 다음 질문을 가져왔습니다.",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = SuccessResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "인터뷰를 찾을 수 없습니다."
            )
        }
    )
    public SuccessResponse<SingleResult<Map<String, Object>>> getNextQuestion(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @AuthenticationPrincipal CustomUserDetails user) {

        Question next = flow.getNextQuestion(user.getId(), interviewId);
        if (next == null) {
            return SuccessResponse.ok(ResponseService.getSingleResult(Map.of("noMore", true)));
        }

        // COSTAR 형식으로 변환된 질문 내용 가져오기
        String convertedContent = flow.getConvertedQuestionContent(next);
        
        flow.createQuestionMessage(interviewId, next, convertedContent);
        Map<String, Object> result = Map.of(
            "questionId", next.getId(),
            "content", convertedContent  // 변환된 질문 사용
        );
        return SuccessResponse.ok(ResponseService.getSingleResult(result));
    }

    @PostMapping("/{interviewId}/answer")
    @Operation(
        summary = "답변 제출",
        description = "현재 질문에 대한 답변을 제출하고, AI가 생성한 후속 질문을 받습니다."
    )
    public SuccessResponse<SingleResult<Map<String, String>>> postAnswer(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @Parameter(description = "답변 내용", example = "{\"content\": \"OS는 컴퓨터 하드웨어와 소프트웨어 자원을 관리하는 시스템 소프트웨어입니다.\"}")
            @RequestBody Map<String,String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        String ans = body.get("content");
        flow.createAnswerMessage(interviewId, ans);
        var follow = flow.createFollowUpQuestion(interviewId, ans);
        return SuccessResponse.ok(ResponseService.getSingleResult(Map.of("followUp", follow.getContent())));
    }

    @GetMapping("/{interviewId}/messages")
    @Hidden
    @Operation(
        summary = "인터뷰 메시지 조회",
        description = "인터뷰의 모든 대화 메시지(질문과 답변)를 시간순으로 조회합니다."
    )
    public SuccessResponse<ListResult<InterviewMessageDto>> getMessages(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @AuthenticationPrincipal CustomUserDetails user) {

        List<InterviewMessage> messages = flow.getMessages(interviewId);
        List<InterviewMessageDto> messageDtos = messages.stream()
            .map(InterviewMessageDto::from)
            .collect(Collectors.toList());
        return SuccessResponse.ok(ResponseService.getListResult(messageDtos));
    }

    @GetMapping("/{interviewId}/evaluation")
    @Operation(
        summary = "인터뷰 메시지 평가",
        description = "지금까지의 질문과 답변을 바탕으로 AI가 지원자를 평가합니다."
    )
    public SuccessResponse<SingleResult<String>> evaluateMessages(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @AuthenticationPrincipal CustomUserDetails user) {
        String evaluation = flow.evaluateMessages(interviewId);
        return SuccessResponse.ok(ResponseService.getSingleResult(evaluation));
    }

    @PostMapping("/{interviewId}/finishQuestion")
    @Operation(
        summary = "현재 질문 종료",
        description = "현재 진행 중인 질문을 종료하고 다음 질문으로 넘어갈 수 있도록 합니다."
    )
    public SuccessResponse<SingleResult<Map<String, Boolean>>> finishCurrentQuestion(
            @Parameter(description = "인터뷰 ID", example = "1")
            @PathVariable Long interviewId,
            @AuthenticationPrincipal CustomUserDetails user) {

        flow.finishCurrentQuestion(interviewId, user.getId());
        return SuccessResponse.ok(ResponseService.getSingleResult(Map.of("success", true)));
    }
}
