package project.DevView.cat_service.user.controller;

import project.DevView.cat_service.user.dto.JoinDTO;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.user.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import project.DevView.cat_service.global.dto.response.SuccessResponse;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.global.service.ResponseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JoinController {

    private static final Logger logger = LoggerFactory.getLogger(JoinController.class);
    private final JoinService joinService;

    @PostMapping("/join")
    public SuccessResponse<SingleResult<UserEntity>> joinProcess(@RequestBody JoinDTO joinDto) {
        logger.info("Received join request: {}", joinDto);
        try {
            UserEntity userEntity = joinService.joinProcess(joinDto);
            if (userEntity == null) {
                throw new RuntimeException("이미 존재하는 사용자입니다.");
            }
            logger.info("User registered successfully: {}", userEntity.getUsername());
            return SuccessResponse.ok(ResponseService.getSingleResult(userEntity));
        } catch (Exception e) {
            logger.error("Error during user registration: ", e);
            throw e; // GlobalExceptionHandler가 처리
        }
    }
}