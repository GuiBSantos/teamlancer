package sharktank.teamlancer.domain.team.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.team.entity.TeamEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {

    Optional<TeamEntity> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    @Query("""
        SELECT t FROM TeamEntity t
        WHERE t.active = true
        AND (
            LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR CAST(t.techStack AS string) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY t.teamScore DESC
        """)
    Page<TeamEntity> searchByQuery(@Param("query") String query, Pageable pageable);

    Page<TeamEntity> findAllByActiveTrueOrderByTeamScoreDesc(Pageable pageable);

    List<TeamEntity> findTop3ByActiveTrueOrderByTeamScoreDesc();

    List<TeamEntity> findAllByOwnerIdAndActiveTrue(UUID ownerId);

}