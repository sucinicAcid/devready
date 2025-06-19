package project.DevView.cat_service.interview.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import project.DevView.cat_service.interview.dto.request.InterviewCreateRequestDto;
import project.DevView.cat_service.interview.dto.response.InterviewResponseDto;
import project.DevView.cat_service.interview.service.InterviewService;
import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.user.dto.CustomUserDetails;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class InterviewPageController {

    private final InterviewService interviewService;   // 인터뷰 생성 전담

    /* ──────────────────────────────────
     * 1. 면접 모드 선택
     * ────────────────────────────────── */
    @GetMapping("/interview-mode")
    public String showInterviewModeSelect() {
        return "interviewModeSelect";
    }

    /* ──────────────────────────────────
     * 2. 이력서 업로드(인터뷰 시작 전)
     * ────────────────────────────────── */
    @GetMapping("/resume-interview")
    public String showResumeUpload() {
        return "resumeUpload";
    }

    /* ──────────────────────────────────
     * 3. 이력서 기반 면접 화면
     *    - resumeId 로 새 인터뷰 생성
     *    - interviewId, resumeId 모델 전달
     * ────────────────────────────────── */
    @GetMapping("/resume-interview/{resumeId}")
    public String showResumeInterviewPage(@PathVariable Long resumeId,
                                          @AuthenticationPrincipal CustomUserDetails user,
                                          Model model) {

        /* 3-1) 새 인터뷰 생성 */
        InterviewCreateRequestDto req = new InterviewCreateRequestDto("RESUME", resumeId);  // resumeId 포함
        InterviewResponseDto created = interviewService.createInterview(user.getId(), req)
                .getData();  // SingleResult → DTO
        Long interviewId = created.interviewId();  // record의 accessor 메서드 사용

        /* 3-2) 공통 모델 설정 */
        List<String> fields = Arrays.stream(Field.values()).map(Enum::name).toList();
        model.addAttribute("fields", fields);
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("interviewId", interviewId);

        return "interviewPage";     // templates/interviewPage.html
    }

    /* ──────────────────────────────────
     * 4. 기존 CS 기술 면접 화면
     * ────────────────────────────────── */
    @GetMapping("/interview")
    public String showInterviewPage(Model model) {
        List<String> fields = Arrays.stream(Field.values()).map(Enum::name).toList();
        model.addAttribute("fields", fields);
        return "interviewPage";
    }

    /* ──────────────────────────────────
     * 5. 북마크 모아보기
     * ────────────────────────────────── */
    @GetMapping("/bookmarks")
    public String showBookmarks() {
        return "bookmarkPage";
    }
}
