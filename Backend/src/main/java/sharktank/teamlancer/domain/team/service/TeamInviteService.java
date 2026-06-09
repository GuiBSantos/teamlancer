package sharktank.teamlancer.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.team.dto.CreateInviteRequestDTO;
import sharktank.teamlancer.domain.team.dto.InviteResponseDTO;
import sharktank.teamlancer.domain.team.dto.UpdateInviteStatusDTO;
import sharktank.teamlancer.domain.team.entity.InviteStatus;
import sharktank.teamlancer.domain.team.entity.TeamEntity;
import sharktank.teamlancer.domain.team.entity.TeamInviteEntity;
import sharktank.teamlancer.domain.team.entity.TeamMemberEntity;
import sharktank.teamlancer.domain.team.repository.TeamInviteRepository;
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
public class TeamInviteService {

    private final TeamInviteRepository inviteRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public InviteResponseDTO create(UUID teamId, CreateInviteRequestDTO dto, UUID ownerId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode convidar membros");
        }

        UserEntity invitee = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> BusinessException.notFound("Nenhum usuário encontrado com esse email"));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invitee.getId())) {
            throw BusinessException.conflict("Este usuário já é membro da equipe");
        }

        boolean alreadyPending = inviteRepository.existsByTeamIdAndUserIdAndStatus(
                teamId, invitee.getId(), InviteStatus.PENDING);

        if (alreadyPending) {
            throw BusinessException.conflict("Já existe um convite pendente para este usuário");
        }

        TeamInviteEntity invite = TeamInviteEntity.builder()
                .team(team)
                .user(invitee)
                .roleInTeam(dto.roleInTeam())
                .build();

        inviteRepository.save(invite);

        UserEntity owner = userRepository.findById(ownerId).orElseThrow();
        auditLogRepository.save(AuditLogEntity.builder()
                .user(owner)
                .action("TEAM_INVITE_SENT")
                .entityType("TEAM_INVITE")
                .entityId(invite.getId())
                .metadata(Map.of("invitee", invitee.getEmail()))
                .build());

        return toDTO(invite);
    }

    @Transactional(readOnly = true)
    public List<InviteResponseDTO> getMyInvites(UUID userId) {
        return inviteRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<InviteResponseDTO> getTeamInvites(UUID teamId, UUID ownerId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode ver os convites");
        }

        return inviteRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public InviteResponseDTO updateStatus(UUID inviteId, UpdateInviteStatusDTO dto, UUID callerId) {
        TeamInviteEntity invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> BusinessException.notFound("Convite não encontrado"));

        boolean isTeamOwner = invite.getTeam().getOwner().getId().equals(callerId);
        boolean isInvitee = invite.getUser().getId().equals(callerId);

        if (dto.status() == InviteStatus.CANCELLED && !isTeamOwner) {
            throw BusinessException.forbidden("Somente o dono da equipe pode cancelar o convite");
        }
        if ((dto.status() == InviteStatus.ACCEPTED || dto.status() == InviteStatus.REJECTED) && !isInvitee) {
            throw BusinessException.forbidden("Somente o convidado pode aceitar ou rejeitar o convite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw BusinessException.badRequest("Este convite já foi " + invite.getStatus());
        }

        String oldStatus = invite.getStatus().name();
        invite.setStatus(dto.status());
        inviteRepository.save(invite);

        if (dto.status() == InviteStatus.ACCEPTED) {
            TeamMemberEntity newMember = TeamMemberEntity.builder()
                    .team(invite.getTeam())
                    .user(invite.getUser())
                    .roleInTeam(invite.getRoleInTeam() != null ? invite.getRoleInTeam() : "Membro")
                    .build();
            teamMemberRepository.save(newMember);
        }

        UserEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        auditLogRepository.save(AuditLogEntity.builder()
                .user(caller)
                .action("TEAM_INVITE_" + dto.status().name())
                .entityType("TEAM_INVITE")
                .entityId(invite.getId())
                .metadata(Map.of("old_status", oldStatus, "new_status", dto.status().name()))
                .build());

        return toDTO(invite);
    }

    private InviteResponseDTO toDTO(TeamInviteEntity entity) {
        return new InviteResponseDTO(
                entity.getId(),
                entity.getTeam().getId(),
                entity.getTeam().getName(),
                entity.getTeam().getSlug(),
                entity.getUser().getId(),
                entity.getUser().getName(),
                entity.getUser().getEmail(),
                entity.getRoleInTeam(),
                entity.getStatus().name(),
                entity.getCreatedAt()
        );
    }
}