package sharktank.teamlancer.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.entity.UserRole;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);
}