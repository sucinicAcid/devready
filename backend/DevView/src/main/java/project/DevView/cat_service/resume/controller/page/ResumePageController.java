package project.DevView.cat_service.resume.controller.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.resume.dto.ResumeRequest;
import project.DevView.cat_service.resume.dto.ResumeResponse;
import project.DevView.cat_service.resume.dto.ResumeTagResponse;
import project.DevView.cat_service.resume.service.ResumeService;
import project.DevView.cat_service.resume.service.ResumeTagService;
import project.DevView.cat_service.resume.service.TagQuestionService;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/resume-interview")
public class ResumePageController {

    private final ResumeService resumeService;
    private final ResumeTagService resumeTagService;
    private final TagQuestionService tagQuestionService;

    @GetMapping("/upload")
    public String showResumeUpload() {
        return "resumeUpload";
    }

    @PostMapping("/analyze")
    public String startAnalysis(
            @ModelAttribute ResumeRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {
        log.info("이력서 분석 시작 - userId: {}", user.getId());
        
        // 1. 이력서 저장
        ResumeResponse resume = resumeService.createResume(user.getId(), request);
        Long resumeId = resume.getId();
        
        // 2. 태그 및 질문 생성 시작 (비동기)
        resumeTagService.generateTagsAndQuestions(resumeId);
        
        // 3. 생성 상태 확인 페이지로 이동
        return "redirect:/resume-interview/" + resumeId + "/generating";
    }

    @GetMapping("/{resumeId}/generating")
    public String showGeneratingStatus(@PathVariable Long resumeId, Model model) {
        log.info("태그 생성 상태 확인 페이지 - resumeId: {}", resumeId);
        ResumeResponse resume = resumeService.getResume(resumeId);
        model.addAttribute("resume", resume);
        return "generating";
    }

    @GetMapping("/{resumeId}/status")
    @ResponseBody
    public boolean checkGenerationStatus(@PathVariable Long resumeId) {
        return resumeTagService.isGenerationCompleted(resumeId);
    }

    @GetMapping("/{resumeId}/interview")
    public String showInterview(@PathVariable Long resumeId, Model model) {
        log.info("인터뷰 페이지 호출 - resumeId: {}", resumeId);
        ResumeResponse resume = resumeService.getResume(resumeId);
        model.addAttribute("resume", resume);
        return "interview";
    }
} 