package sharktank.teamlancer.domain.team.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.team.entity.JoinRequestStatus;
import sharktank.teamlancer.domain.team.entity.TeamJoinRequestEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequestEntity, UUID> {

    List<TeamJoinRequestEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<TeamJoinRequestEntity> findAllByTeamIdOrderByCreatedAtDesc(UUID teamId);

    boolean existsByTeamIdAndUserIdAndStatus(UUID teamId, UUID userId, JoinRequestStatus status);
}