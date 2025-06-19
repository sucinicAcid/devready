package project.DevView.cat_service.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.user.dto.LoginRequestDto;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.global.service.ResponseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "인증 관련 API")
public class LoginController {

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "사용자 로그인을 수행합니다. 로그인 성공 시 JWT 토큰이 쿠키에 저장됩니다."
    )
    public SuccessResponse<SingleResult<String>> login(
        @Parameter(description = "로그인 요청 정보", required = true)
        @RequestBody LoginRequestDto loginRequest,
        @Parameter(description = "로그인 후 리다이렉트할 URL (선택사항)")
        @RequestParam(required = false) String redirect
    ) {
        // 실제 로그인 처리는 LoginFilter에서 수행됨
        // 이 컨트롤러는 스웨거 문서화를 위한 것
        return SuccessResponse.ok(ResponseService.getSingleResult("로그인 성공"));
    }
}
