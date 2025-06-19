package project.DevView.cat_service.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.DevView.cat_service.global.exception.CustomException;
import project.DevView.cat_service.global.exception.ErrorCode;
import project.DevView.cat_service.global.service.ResponseService;
import project.DevView.cat_service.global.dto.response.result.ListResult;
import project.DevView.cat_service.global.dto.response.result.SingleResult;
import project.DevView.cat_service.interview.dto.request.InterviewCreateRequestDto;
import project.DevView.cat_service.interview.dto.response.InterviewResponseDto;
import project.DevView.cat_service.interview.entity.Interview;
import project.DevView.cat_service.interview.mapper.InterviewMapper;
import project.DevView.cat_service.interview.repository.InterviewMessageRepository;
import project.DevView.cat_service.interview.repository.InterviewRepository;
import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.question.repository.QuestionRepository;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.user.repository.UserRepository;
import project.DevView.cat_service.question.repository.UserQuestionHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final InterviewMessageRepository messageRepository;
    private final UserQuestionHistoryRepository historyRepository;
    private final QuestionRepository questionRepository;

    /**
     * 새 인터뷰 시작
     */
    public SingleResult<InterviewResponseDto> createInterview(Long userId,
                                                              InterviewCreateRequestDto req) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        Field field = Field.fromName(req.field());

        Interview iv = InterviewMapper.from(req, user);
        iv.setStartedAt(LocalDateTime.now());
        Interview saved = interviewRepository.save(iv);

        return ResponseService.getSingleResult(InterviewResponseDto.of(saved));
    }

    /**
     * 분야별 과거 인터뷰 목록 조회
     */
    public ListResult<InterviewResponseDto> findByUserAndField(Long userId,
                                                               String fieldName) {
        Field field = Field.fromName(fieldName);
        List<Interview> list = interviewRepository.findByUserIdAndField(userId, field);
        List<InterviewResponseDto> dtos = list.stream()
                .map(InterviewResponseDto::of)
                .collect(Collectors.toList());
        return ResponseService.getListResult(dtos);
    }

    /**
     * 인터뷰 상세 조회
     */
    public SingleResult<InterviewResponseDto> getInterviewDetail(Long interviewId,
                                                                 Long userId) {
        Interview iv = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_EXIST));
        // Long 타입 비교
        if (!iv.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return ResponseService.getSingleResult(InterviewResponseDto.of(iv));
    }

    /**
     * 인터뷰 종료
     */
    public SingleResult<String> finishInterview(Long interviewId, Long userId) {
        Interview iv = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_EXIST));
        if (!iv.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 인터뷰 종료 시간 설정
        iv.setEndedAt(LocalDateTime.now());
        interviewRepository.save(iv);

        return ResponseService.getSingleResult("Interview finished");
    }
}
