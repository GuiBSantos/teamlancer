package sharktank.teamlancer.domain.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.project.entity.ProjectEntity;
import sharktank.teamlancer.domain.project.entity.ProjectStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    Page<ProjectEntity> findAllByClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);

    Page<ProjectEntity> findAllByTeamIdOrderByCreatedAtDesc(UUID teamId, Pageable pageable);

    Page<ProjectEntity> findAllByClientIdAndStatusOrderByCreatedAtDesc(UUID clientId, ProjectStatus status, Pageable pageable);

    Optional<ProjectEntity> findByRequestId(UUID requestId);
}