package project.DevView.cat_service.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/home/health/")
    public String healthCheck() {
        return "OK";
    }
} 