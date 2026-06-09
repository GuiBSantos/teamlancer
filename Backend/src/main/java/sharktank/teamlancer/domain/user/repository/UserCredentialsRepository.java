package sharktank.teamlancer.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.user.entity.UserCredentialsEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {

    Optional<UserCredentialsEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE UserCredentialsEntity c SET c.refreshTokenHash = null WHERE c.user.id = :userId")
    void clearRefreshToken(UUID userId);
}