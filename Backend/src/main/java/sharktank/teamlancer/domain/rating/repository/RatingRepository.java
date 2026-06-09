package sharktank.teamlancer.domain.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.rating.entity.RaterType;
import sharktank.teamlancer.domain.rating.entity.RatingEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {

    List<RatingEntity> findAllByProjectId(UUID projectId);

    Optional<RatingEntity> findByProjectIdAndRaterType(UUID projectId, RaterType raterType);

    boolean existsByProjectIdAndRaterType(UUID projectId, RaterType raterType);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM RatingEntity r " +
           "WHERE r.project.team.id = :teamId AND r.raterType = 'CLIENT'")
    Double avgScoreByTeamId(UUID teamId);
}
