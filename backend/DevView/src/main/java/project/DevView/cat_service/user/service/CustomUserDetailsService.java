package project.DevView.cat_service.user.service;


import project.DevView.cat_service.user.dto.CustomUserDetails;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntityData = userRepository.findByUsername(username);
        if (userEntityData == null) {
            System.out.println("[CustomUserDetailsService] 사용자를 찾을 수 없음: " + username);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        System.out.println("[CustomUserDetailsService] 사용자 찾음: " + username);
        return new CustomUserDetails(userEntityData);
    }
}
