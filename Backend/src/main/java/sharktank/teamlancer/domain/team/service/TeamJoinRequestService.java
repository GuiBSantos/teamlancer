package sharktank.teamlancer.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.team.dto.CreateJoinRequestDTO;
import sharktank.teamlancer.domain.team.dto.JoinRequestResponseDTO;
import sharktank.teamlancer.domain.team.dto.UpdateJoinRequestStatusDTO;
import sharktank.teamlancer.domain.team.entity.JoinRequestStatus;
import sharktank.teamlancer.domain.team.entity.TeamEntity;
import sharktank.teamlancer.domain.team.entity.TeamJoinRequestEntity;
import sharktank.teamlancer.domain.team.entity.TeamMemberEntity;
import sharktank.teamlancer.domain.team.repository.TeamJoinRequestRepository;
import sharktank.teamlancer.domain.team.repository.TeamMemberRepository;
import sharktank.teamlancer.domain.team.repository.TeamRepository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.audit.AuditLogEntity;
import sharktank.teamlancer.shared.audit.AuditLogRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamJoinRequestService {

    private final TeamJoinRequestRepository joinRequestRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public JoinRequestResponseDTO create(CreateJoinRequestDTO dto, UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        TeamEntity team = teamRepository.findById(dto.teamId())
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getActive()) {
            throw BusinessException.badRequest("Equipe não está ativa");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(dto.teamId(), userId)) {
            throw BusinessException.conflict("Você já é membro desta equipe");
        }

        boolean alreadyPending = joinRequestRepository.existsByTeamIdAndUserIdAndStatus(
                dto.teamId(), userId, JoinRequestStatus.PENDING);
        if (alreadyPending) {
            throw BusinessException.conflict("Você já possui uma solicitação pendente para esta equipe");
        }

        TeamJoinRequestEntity request = TeamJoinRequestEntity.builder()
                .team(team)
                .user(user)
                .message(dto.message())
                .build();

        joinRequestRepository.save(request);

        auditLogRepository.save(AuditLogEntity.builder()
                .user(user)
                .action("JOIN_REQUEST_CREATED")
                .entityType("TEAM_JOIN_REQUEST")
                .entityId(request.getId())
                .metadata(Map.of("teamId", team.getId().toString()))
                .build());

        return toDTO(request);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponseDTO> getMyRequests(UUID userId) {
        return joinRequestRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponseDTO> getTeamRequests(UUID teamId, UUID ownerId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode ver as solicitações");
        }

        return joinRequestRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public JoinRequestResponseDTO updateStatus(UUID requestId, UpdateJoinRequestStatusDTO dto, UUID callerId) {
        TeamJoinRequestEntity request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> BusinessException.notFound("Solicitação não encontrada"));

        boolean isTeamOwner = request.getTeam().getOwner().getId().equals(callerId);
        boolean isUser = request.getUser().getId().equals(callerId);

        if (dto.status() == JoinRequestStatus.CANCELLED && !isUser) {
            throw BusinessException.forbidden("Somente o usuário pode cancelar sua própria solicitação");
        }
        if ((dto.status() == JoinRequestStatus.ACCEPTED || dto.status() == JoinRequestStatus.REJECTED) && !isTeamOwner) {
            throw BusinessException.forbidden("Somente o dono da equipe pode aceitar ou rejeitar a solicitação");
        }

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw BusinessException.badRequest("Esta solicitação já foi " + request.getStatus());
        }

        String oldStatus = request.getStatus().name();
        request.setStatus(dto.status());
        joinRequestRepository.save(request);

        if (dto.status() == JoinRequestStatus.ACCEPTED) {
            TeamMemberEntity newMember = TeamMemberEntity.builder()
                    .team(request.getTeam())
                    .user(request.getUser())
                    .roleInTeam("Membro")
                    .build();
            teamMemberRepository.save(newMember);
        }

        UserEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        auditLogRepository.save(AuditLogEntity.builder()
                .user(caller)
                .action("JOIN_REQUEST_" + dto.status().name())
                .entityType("TEAM_JOIN_REQUEST")
                .entityId(request.getId())
                .metadata(Map.of(
                        "old_status", oldStatus,
                        "new_status", dto.status().name()
                ))
                .build());

        return toDTO(request);
    }

    private JoinRequestResponseDTO toDTO(TeamJoinRequestEntity entity) {
        return new JoinRequestResponseDTO(
                entity.getId(),
                entity.getTeam().getId(),
                entity.getTeam().getName(),
                entity.getUser().getId(),
                entity.getUser().getName(),
                entity.getMessage(),
                entity.getStatus().name(),
                entity.getCreatedAt()
        );
    }
}