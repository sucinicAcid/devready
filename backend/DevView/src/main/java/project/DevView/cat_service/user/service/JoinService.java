package project.DevView.cat_service.user.service;


import project.DevView.cat_service.user.dto.JoinDTO;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Transactional
    public UserEntity joinProcess(JoinDTO joinDTO){
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        Boolean isExist = userRepository.existsByUsername(username);
        if(isExist){
            return null;
        }

        String role = "ROLE_CUSTOMER";

        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole(role);

        return userRepository.save(data);
    }
}
