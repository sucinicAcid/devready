package project.DevView.cat_service.user.repository;

import project.DevView.cat_service.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByUsername(String username);

    UserEntity findByUsername(String username);
}
