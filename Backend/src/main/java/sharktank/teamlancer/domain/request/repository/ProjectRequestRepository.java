package sharktank.teamlancer.domain.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.request.entity.ProjectRequestEntity;
import sharktank.teamlancer.domain.request.entity.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequestEntity, UUID> {

    @Query("SELECT pr FROM ProjectRequestEntity pr JOIN FETCH pr.client JOIN FETCH pr.team WHERE pr.client.id = :clientId ORDER BY pr.createdAt DESC")
    Page<ProjectRequestEntity> findAllByClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);

    @Query("SELECT pr FROM ProjectRequestEntity pr JOIN FETCH pr.client JOIN FETCH pr.team WHERE pr.team.id = :teamId ORDER BY pr.createdAt DESC")
    Page<ProjectRequestEntity> findAllByTeamIdOrderByCreatedAtDesc(UUID teamId, Pageable pageable);

    @Query("SELECT pr FROM ProjectRequestEntity pr JOIN FETCH pr.client JOIN FETCH pr.team WHERE pr.id = :id")
    Optional<ProjectRequestEntity> findById(UUID id);

    List<ProjectRequestEntity> findAllByTeamIdAndStatus(UUID teamId, RequestStatus status);

    boolean existsByClientIdAndTeamIdAndStatus(UUID clientId, UUID teamId, RequestStatus status);
}