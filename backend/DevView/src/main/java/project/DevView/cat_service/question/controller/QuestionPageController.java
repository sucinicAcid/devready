package project.DevView.cat_service.question.controller;


import project.DevView.cat_service.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.DevView.cat_service.user.dto.CustomUserDetails;

import java.util.List;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionPageController {

    private final QuestionService questionService;

    /**
     * 1) Question 생성 폼 (SSR)
     *    GET /admin/questions/new
     -> 크롤링으로 인한 api 삭제
     */

    
    /**
     * 2) 모든 질문 목록 조회 (SSR)
     *    GET /questions/all?field=OS
     */
    @GetMapping("/all")
    public String allQuestionsPage(
            @RequestParam("field") String fieldName,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            Model model
    ) {
        // 디버깅 로그 추가
        System.out.println("[QuestionPageController] /questions/all 요청 처리");
        System.out.println("[QuestionPageController] 사용자 인증 상태: " + (user != null ? "인증됨" : "인증되지 않음"));
        
        // 쿠키 디버깅
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("[QuestionPageController] 쿠키 목록:");
            for (Cookie cookie : cookies) {
                System.out.println("  - " + cookie.getName() + ": " + cookie.getValue() + 
                                  " (Path: " + cookie.getPath() + ", Domain: " + cookie.getDomain() + ")");
            }
        } else {
            System.out.println("[QuestionPageController] 쿠키 없음");
        }
        
        // 인증된 사용자인 경우 답변 상태와 함께 조회
        if (user != null) {
            System.out.println("[QuestionPageController] 인증된 사용자 ID: " + user.getId());
            var questions = questionService.getAllQuestionsWithStatusByField(user.getId(), fieldName);
            model.addAttribute("questions", questions.getList());
        } else {
            System.out.println("[QuestionPageController] 인증되지 않은 사용자로 질문 조회");
            // 인증되지 않은 사용자인 경우 답변 상태 없이 조회
            var questions = questionService.getAllQuestionsByField(fieldName);
            model.addAttribute("questions", questions.getList());
        }
        
        model.addAttribute("fieldName", fieldName);
        model.addAttribute("isAuthenticated", user != null);
        return "allQuestions";
    }
}
