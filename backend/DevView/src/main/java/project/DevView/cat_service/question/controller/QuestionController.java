package project.DevView.cat_service.question.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.ListResult;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.question.dto.response.QuestionResponseDto;
import project.DevView.cat_service.question.dto.response.QuestionWithStatusDto;
import project.DevView.cat_service.question.dto.request.QuestionCreateRequest;
import project.DevView.cat_service.question.service.QuestionService;
import project.DevView.cat_service.user.dto.CustomUserDetails;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Question", description = "면접 질문 관련 API")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @Hidden
    public SuccessResponse<SingleResult<QuestionResponseDto>> createQuestion(
            @Valid @RequestBody QuestionCreateRequest requestDto
    ) {
        SingleResult<QuestionResponseDto> result = questionService.createQuestion(requestDto);
        return SuccessResponse.ok(result);
    }
    
    @GetMapping
    @Operation(
        summary = "특정 분야의 모든 질문 조회",
        description = "특정 분야(예: OS, DB, Network 등)에 속한 모든 질문을 조회합니다."
    )
    public SuccessResponse<ListResult<QuestionResponseDto>> getAllQuestionsByField(
            @Parameter(description = "분야 이름", example = "OS")
            @RequestParam("field") String fieldName
    ) {
        var result = questionService.getAllQuestionsByField(fieldName);
        return SuccessResponse.ok(result);
    }
    
    @GetMapping("/with-status")
    @Operation(
        summary = "특정 분야의 모든 질문 조회 (답변 여부 포함)",
        description = "특정 분야의 모든 질문을 조회하고, 로그인한 사용자가 각 질문에 답변했는지 여부를 포함합니다."
    )
    public SuccessResponse<ListResult<QuestionWithStatusDto>> getAllQuestionsWithStatusByField(
            @Parameter(description = "분야 이름", example = "OS")
            @RequestParam("field") String fieldName,
            @Parameter(description = "인증된 사용자 정보")
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        var result = questionService.getAllQuestionsWithStatusByField(user.getId(), fieldName);
        return SuccessResponse.ok(result);
    }
}
