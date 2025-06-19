package project.DevView.cat_service.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

    /**
     * 로그인 페이지를 GET으로 보여주는 엔드포인트
     * 브라우저에서 http://localhost:8080/login 으로 접근 시
     * loginPage.html을 SSR로 렌더링
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "loginPage"; // resources/templates/loginPage.html
    }
}
