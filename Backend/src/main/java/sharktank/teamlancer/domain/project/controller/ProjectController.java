package sharktank.teamlancer.domain.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.project.dto.ProjectResponseDTO;
import sharktank.teamlancer.domain.project.dto.UpdateProjectStatusDTO;
import sharktank.teamlancer.domain.project.entity.ProjectEntity;
import sharktank.teamlancer.domain.project.entity.ProjectStatus;
import sharktank.teamlancer.domain.project.repository.ProjectRepository;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Gerenciamento de projetos ativos")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um projeto")
    public ResponseEntity<ProjectResponseDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));
        checkAccess(project, callerId);
        return ResponseEntity.ok(toDTO(project));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status do projeto — somente dono da equipe")
    public ResponseEntity<ProjectResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectStatusDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));

        if (!project.getTeam().getOwner().getId().equals(callerId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode atualizar o status");
        }

        project.setStatus(dto.status());

        if (dto.status() == ProjectStatus.COMPLETED) {
            project.setFinishedAt(LocalDateTime.now());
        }

        projectRepository.save(project);
        return ResponseEntity.ok(toDTO(project));
    }


    private void checkAccess(ProjectEntity project, UUID userId) {
        boolean isClient    = project.getClient().getId().equals(userId);
        boolean isTeamOwner = project.getTeam().getOwner().getId().equals(userId);
        if (!isClient && !isTeamOwner) {
            throw BusinessException.forbidden("Sem acesso a este projeto");
        }
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }

    private ProjectResponseDTO toDTO(ProjectEntity p) {
        return new ProjectResponseDTO(
                p.getId(),
                p.getRequest().getId(),
                p.getClient().getId(),
                p.getClient().getName(),
                p.getTeam().getId(),
                p.getTeam().getName(),
                p.getTeam().getSlug(),
                p.getName(),
                p.getDescription(),
                p.getStatus().name(),
                p.getStartedAt(),
                p.getFinishedAt(),
                p.getCreatedAt()
        );
    }
}
