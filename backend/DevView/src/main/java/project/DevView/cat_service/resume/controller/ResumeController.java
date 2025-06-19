package project.DevView.cat_service.resume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.ListResult;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.global.service.ResponseService;
import project.DevView.cat_service.resume.dto.ResumeRequest;
import project.DevView.cat_service.resume.dto.ResumeResponse;
import project.DevView.cat_service.resume.dto.ResumeMessageResponse;
import project.DevView.cat_service.resume.entity.ResumeMessage;
import project.DevView.cat_service.resume.service.ResumeService;
import project.DevView.cat_service.resume.service.ResumeMessageService;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "이력서 관련 API")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeMessageService resumeMessageService;

    @PostMapping
    @Operation(
        summary = "이력서 생성",
        description = "새로운 이력서를 생성합니다."
    )
    public SuccessResponse<SingleResult<ResumeResponse>> createResume(
            @Parameter(description = "인증된 사용자 정보")
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "이력서 생성 요청", required = true)
            @RequestBody ResumeRequest request) {
        log.debug("이력서 생성 요청 - 사용자: {}, 내용 길이: {}", user.getUsername(), request.getContent().length());
        
        ResumeResponse resume = resumeService.createResume(user.getId(), request);
        log.debug("이력서 생성 완료 - ID: {}", resume.getId());
        
        return SuccessResponse.ok(ResponseService.getSingleResult(resume));
    }

    @GetMapping("/{resumeId}")
    @Hidden
    public SuccessResponse<SingleResult<ResumeResponse>> getResume(
            @PathVariable Long resumeId) {
        return SuccessResponse.ok(ResponseService.getSingleResult(resumeService.getResume(resumeId)));
    }

    @GetMapping("/user")
    @Hidden
    public SuccessResponse<ListResult<ResumeResponse>> getUserResumes(
            @AuthenticationPrincipal CustomUserDetails user) {
        return SuccessResponse.ok(ResponseService.getListResult(resumeService.getUserResumes(user.getId())));
    }

    @PutMapping("/{resumeId}")
    @Hidden
    public SuccessResponse<SingleResult<ResumeResponse>> updateResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ResumeRequest request) {
        return SuccessResponse.ok(ResponseService.getSingleResult(
            resumeService.updateResume(resumeId, user.getId(), request)));
    }

    @DeleteMapping("/{resumeId}")
    @Hidden
    public SuccessResponse<SingleResult<Void>> deleteResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        resumeService.deleteResume(resumeId, user.getId());
        return SuccessResponse.ok(ResponseService.getSingleResult(null));
    }

    @PostMapping("/{resumeId}/tag-questions/{tagQuestionId}/follow-ups")
    @Operation(
        summary = "이력서 + 태그 질문 기반 꼬리 질문 생성",
        description = "이력서 내용과 태그 질문을 기반으로 답변에 대한 꼬리 질문을 생성하고 저장합니다. 답변도 함께 저장됩니다."
    )
    public SuccessResponse<SingleResult<String>> createFollowUpQuestion(
            @Parameter(description = "이력서 ID", example = "1")
            @PathVariable Long resumeId,
            @Parameter(description = "태그 질문 ID", example = "1")
            @PathVariable Long tagQuestionId,
            @Parameter(description = "답변 내용 (content 또는 answer 키 사용 가능)", example = "{\"content\": \"저는 Java와 Spring을 주로 사용하여 백엔드 개발을 하고 있습니다.\"}")
            @RequestBody Map<String, String> body,
            @Parameter(description = "인증된 사용자 정보")
            @AuthenticationPrincipal CustomUserDetails user) {
        
        log.debug("꼬리 질문 생성 요청 - resumeId: {}, tagQuestionId: {}, body: {}", resumeId, tagQuestionId, body);
        
        // content 또는 answer 키에서 답변 내용을 가져옴
        String answer = body.get("content");
        if (answer == null) {
            answer = body.get("answer");
        }
        
        if (answer == null || answer.trim().isEmpty()) {
            log.error("답변 내용이 비어있습니다. - resumeId: {}, tagQuestionId: {}, body: {}", resumeId, tagQuestionId, body);
            throw new IllegalArgumentException("답변 내용은 필수입니다. (content 또는 answer 키 사용)");
        }
        
        String followUpQuestion = resumeService.createFollowUpQuestion(resumeId, tagQuestionId, answer);
        log.debug("꼬리 질문 생성 완료 - resumeId: {}, tagQuestionId: {}, followUpQuestion: {}", resumeId, tagQuestionId, followUpQuestion);
        
        return SuccessResponse.ok(ResponseService.getSingleResult(followUpQuestion));
    }

    @GetMapping("/{resumeId}/messages")
    @Operation(
        summary = "이력서 메시지 조회",
        description = "이력서의 모든 대화 메시지(질문과 답변)를 시간순으로 조회합니다."
    )
    public SuccessResponse<ListResult<ResumeMessageResponse>> getMessages(
            @Parameter(description = "이력서 ID", example = "1")
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        List<ResumeMessage> messages = resumeService.getMessages(resumeId);
        List<ResumeMessageResponse> messageResponses = messages.stream()
            .map(ResumeMessageResponse::from)
            .collect(Collectors.toList());
        return SuccessResponse.ok(ResponseService.getListResult(messageResponses));
    }

    @GetMapping("/{resumeId}/evaluation")
    @Operation(
        summary = "이력서 기반 면접 평가",
        description = "이력서 기반 면접의 모든 질문과 답변을 바탕으로 AI가 지원자를 평가합니다."
    )
    public SuccessResponse<SingleResult<String>> evaluateMessages(
            @Parameter(description = "이력서 ID", example = "1")
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        String evaluation = resumeService.evaluateMessages(resumeId);
        return SuccessResponse.ok(ResponseService.getSingleResult(evaluation));
    }

    @GetMapping("/{resumeId}/completion-status")
    @Operation(
        summary = "이력서 질문 완료 상태 확인",
        description = "해당 이력서의 모든 질문이 완료되었는지 true/false로 반환합니다."
    )
    public SuccessResponse<SingleResult<Boolean>> checkAllQuestionsCompleted(
            @Parameter(description = "이력서 ID", example = "1")
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        boolean isCompleted = resumeService.isAllQuestionsCompleted(resumeId);
        return SuccessResponse.ok(ResponseService.getSingleResult(isCompleted));
    }
}