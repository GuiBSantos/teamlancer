package sharktank.teamlancer.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.team.entity.TeamPortfolioEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamPortfolioRepository extends JpaRepository<TeamPortfolioEntity, UUID> {

    List<TeamPortfolioEntity> findAllByTeamIdOrderByCreatedAtDesc(UUID teamId);
}