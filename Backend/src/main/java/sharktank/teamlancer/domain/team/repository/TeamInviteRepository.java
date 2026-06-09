package sharktank.teamlancer.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.team.entity.InviteStatus;
import sharktank.teamlancer.domain.team.entity.TeamInviteEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamInviteRepository extends JpaRepository<TeamInviteEntity, UUID> {

    List<TeamInviteEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<TeamInviteEntity> findAllByTeamIdOrderByCreatedAtDesc(UUID teamId);

    boolean existsByTeamIdAndUserIdAndStatus(UUID teamId, UUID userId, InviteStatus status);
}