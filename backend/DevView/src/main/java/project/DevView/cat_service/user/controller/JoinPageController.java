package project.DevView.cat_service.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JoinPageController {

    @GetMapping("/joinPage")
    public String showJoinPage() {
        // 그냥 joinPage.html 템플릿을 렌더링
        return "joinPage";
    }
}
