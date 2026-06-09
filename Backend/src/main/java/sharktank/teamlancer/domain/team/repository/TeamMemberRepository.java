package sharktank.teamlancer.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.team.entity.TeamMemberEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMemberEntity, UUID> {

    List<TeamMemberEntity> findAllByTeamId(UUID teamId);

    Optional<TeamMemberEntity> findByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

    List<TeamMemberEntity> findAllByUserId(UUID userId);
}