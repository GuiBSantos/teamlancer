package sharktank.teamlancer.domain.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.project.entity.ProjectEntity;
import sharktank.teamlancer.domain.project.entity.ProjectStatus;
import sharktank.teamlancer.domain.project.repository.ProjectRepository;
import sharktank.teamlancer.domain.request.dto.CreateProjectRequestDTO;
import sharktank.teamlancer.domain.request.dto.ProjectRequestResponseDTO;
import sharktank.teamlancer.domain.request.dto.UpdateRequestStatusDTO;
import sharktank.teamlancer.domain.request.entity.ProjectRequestEntity;
import sharktank.teamlancer.domain.request.entity.RequestStatus;
import sharktank.teamlancer.domain.request.repository.ProjectRequestRepository;
import sharktank.teamlancer.domain.team.entity.TeamEntity;
import sharktank.teamlancer.domain.team.repository.TeamRepository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.audit.AuditLogEntity;
import sharktank.teamlancer.shared.audit.AuditLogRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectRequestService {

    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public ProjectRequestResponseDTO create(CreateProjectRequestDTO dto, UUID clientId) {
        UserEntity client = userRepository.findById(clientId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        TeamEntity team = teamRepository.findById(dto.teamId())
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getActive()) {
            throw BusinessException.badRequest("Equipe não está ativa");
        }

        boolean jaExiste = requestRepository.existsByClientIdAndTeamIdAndStatus(
                clientId, dto.teamId(), RequestStatus.PENDING);
        if (jaExiste) {
            throw BusinessException.conflict(
                    "Você já tem uma solicitação pendente para esta equipe");
        }

        ProjectRequestEntity request = ProjectRequestEntity.builder()
                .client(client)
                .team(team)
                .projectName(dto.projectName())
                .description(dto.description())
                .budgetRange(dto.budgetRange())
                .deadline(dto.deadline())
                .build();

        requestRepository.save(request);

        auditLogRepository.save(AuditLogEntity.builder()
                .user(client)
                .action("REQUEST_SENT")
                .entityType("PROJECT_REQUEST")
                .entityId(request.getId())
                .metadata(Map.of(
                        "team", team.getName(),
                        "project", dto.projectName()
                ))
                .build());

        return toDTO(request);
    }

    @Transactional(readOnly = true)
    public Page<ProjectRequestResponseDTO> myRequests(UUID clientId, Pageable pageable) {
        return requestRepository
                .findAllByClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProjectRequestResponseDTO getById(UUID requestId, UUID callerId) {
        ProjectRequestEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> BusinessException.notFound("Solicitação não encontrada"));

        boolean isClient = request.getClient().getId().equals(callerId);
        boolean isTeamOwner = request.getTeam().getOwner().getId().equals(callerId);

        if (!isClient && !isTeamOwner) {
            throw BusinessException.forbidden("Sem permissão para ver esta solicitação");
        }

        return toDTO(request);
    }

    @Transactional
    public ProjectRequestResponseDTO updateStatus(UUID requestId,
                                                  UpdateRequestStatusDTO dto,
                                                  UUID callerId) {
        ProjectRequestEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> BusinessException.notFound("Solicitação não encontrada"));

        boolean isTeamOwner = request.getTeam().getOwner().getId().equals(callerId);
        boolean isClient    = request.getClient().getId().equals(callerId);

        if (dto.status() == RequestStatus.CANCELLED && !isClient) {
            throw BusinessException.forbidden("Somente o cliente pode cancelar a solicitação");
        }
        if ((dto.status() == RequestStatus.ACCEPTED || dto.status() == RequestStatus.REJECTED)
                && !isTeamOwner) {
            throw BusinessException.forbidden("Somente o dono da equipe pode aceitar ou rejeitar");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw BusinessException.badRequest(
                    "Só é possível atualizar solicitações com status PENDING");
        }

        String oldStatus = request.getStatus().name();
        request.setStatus(dto.status());
        requestRepository.save(request);

        if (dto.status() == RequestStatus.ACCEPTED) {
            ProjectEntity project = ProjectEntity.builder()
                    .request(request)
                    .client(request.getClient())
                    .team(request.getTeam())
                    .name(request.getProjectName())
                    .description(request.getDescription())
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            projectRepository.save(project);
        }

        UserEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        auditLogRepository.save(AuditLogEntity.builder()
                .user(caller)
                .action("REQUEST_" + dto.status().name())
                .entityType("PROJECT_REQUEST")
                .entityId(request.getId())
                .metadata(Map.of(
                        "old_status", oldStatus,
                        "new_status", dto.status().name()
                ))
                .build());

        return toDTO(request);
    }

    @Transactional(readOnly = true)
    public Page<ProjectRequestResponseDTO> teamRequests(UUID teamId,
                                                        UUID callerId,
                                                        Pageable pageable) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(callerId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode ver as solicitações");
        }

        return requestRepository
                .findAllByTeamIdOrderByCreatedAtDesc(teamId, pageable)
                .map(this::toDTO);
    }


    private ProjectRequestResponseDTO toDTO(ProjectRequestEntity r) {

        String projectId = projectRepository
                .findByRequestId(r.getId())
                .map(project -> project.getId().toString())
                .orElse(null);

        return new ProjectRequestResponseDTO(
                r.getId(),
                r.getClient().getId(),
                r.getClient().getName(),
                r.getTeam().getId(),
                r.getTeam().getName(),
                r.getTeam().getSlug(),
                r.getProjectName(),
                r.getDescription(),
                r.getBudgetRange(),
                r.getDeadline(),
                r.getStatus().name(),
                projectId,
                r.getCreatedAt()
        );
    }
}