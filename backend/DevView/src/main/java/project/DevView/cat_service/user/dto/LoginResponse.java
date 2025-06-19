package project.DevView.cat_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String role;
} 