package sharktank.teamlancer.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.team.dto.*;
import sharktank.teamlancer.domain.team.entity.TeamEntity;
import sharktank.teamlancer.domain.team.entity.TeamMemberEntity;
import sharktank.teamlancer.domain.team.repository.TeamMemberRepository;
import sharktank.teamlancer.domain.team.repository.TeamPortfolioRepository;
import sharktank.teamlancer.domain.team.repository.TeamRepository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.audit.AuditLogEntity;
import sharktank.teamlancer.shared.audit.AuditLogRepository;
import sharktank.teamlancer.shared.exception.BusinessException;
import sharktank.teamlancer.shared.util.SlugUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamPortfolioRepository teamPortfolioRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SlugUtils slugUtils;

    public Page<TeamSummaryDTO> listTeams(String query, Pageable pageable) {
        Page<TeamEntity> page = (query != null && !query.isBlank())
                ? teamRepository.searchByQuery(query.trim(), pageable)
                : teamRepository.findAllByActiveTrueOrderByTeamScoreDesc(pageable);
        return page.map(this::toSummary);
    }

    public List<TeamSummaryDTO> featuredTeams() {
        return teamRepository.findTop3ByActiveTrueOrderByTeamScoreDesc()
                .stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getBySlug(String slug) {
        TeamEntity team = teamRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada: " + slug));
        return toResponse(team);
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getById(UUID id) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));
        return toResponse(team);
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getByOwnerId(UUID ownerId) {
        List<TeamEntity> teams = teamRepository.findAllByOwnerIdAndActiveTrue(ownerId);
        if (teams.isEmpty()) {
            throw BusinessException.notFound("Nenhuma equipe encontrada para este usuário");
        }
        return toResponse(teams.get(0));
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getMyTeamAsMember(UUID userId) {
        List<TeamEntity> owned = teamRepository.findAllByOwnerIdAndActiveTrue(userId);
        if (!owned.isEmpty()) return toResponse(owned.get(0));

        return teamMemberRepository.findAllByUserId(userId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getTeam().getActive()))
                .map(m -> toResponse(m.getTeam()))
                .findFirst()
                .orElseThrow(() -> BusinessException.notFound("Nenhuma equipe encontrada para este usuário"));
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO.TeamMemberDTO> getMembers(UUID teamId) {
        return teamMemberRepository.findAllByTeamId(teamId)
                .stream().map(this::toMemberDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO.TeamPortfolioDTO> getPortfolio(UUID teamId) {
        return teamPortfolioRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId)
                .stream().map(p -> new TeamResponseDTO.TeamPortfolioDTO(
                        p.getId(), p.getTitle(), p.getDescription(), p.getUrl()
                )).toList();
    }

    @Transactional
    public TeamResponseDTO createTeam(CreateTeamDTO dto, UUID ownerId) {
        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        String slug = slugUtils.toUniqueSlug(dto.name(), teamRepository::existsBySlug);

        TeamEntity team = TeamEntity.builder()
                .owner(owner)
                .name(dto.name())
                .slug(slug)
                .description(dto.description())
                .techStack(dto.techStack() != null ? dto.techStack() : new String[0])
                .location(dto.location())
                .build();

        teamRepository.save(team);

        TeamMemberEntity ownerMember = TeamMemberEntity.builder()
                .team(team)
                .user(owner)
                .roleInTeam("Owner")
                .build();
        teamMemberRepository.save(ownerMember);

        auditLogRepository.save(AuditLogEntity.builder()
                .user(owner)
                .action("TEAM_CREATED")
                .entityType("TEAM")
                .entityId(team.getId())
                .metadata(Map.of("name", team.getName(), "slug", slug))
                .build());

        return toResponse(team);
    }

    @Transactional
    public void deleteTeam(UUID teamId, UUID requesterId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(requesterId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode deletá-la");
        }

        team.setActive(false);
        teamRepository.save(team);
    }

    @Transactional
    public void addMember(UUID teamId, AddMemberDTO dto, UUID requesterId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(requesterId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode adicionar membros");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, dto.userId())) {
            throw BusinessException.conflict("Usuário já é membro desta equipe");
        }

        UserEntity newMember = userRepository.findById(dto.userId())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        teamMemberRepository.save(TeamMemberEntity.builder()
                .team(team)
                .user(newMember)
                .roleInTeam(dto.roleInTeam())
                .build());
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, UUID requesterId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (!team.getOwner().getId().equals(requesterId)) {
            throw BusinessException.forbidden("Somente o dono da equipe pode remover membros");
        }

        if (team.getOwner().getId().equals(userId)) {
            throw BusinessException.badRequest("O dono não pode ser removido da equipe");
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Transactional
    public void leaveTeam(UUID teamId, UUID userId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("Equipe não encontrada"));

        if (team.getOwner().getId().equals(userId)) {
            throw BusinessException.badRequest(
                    "O dono não pode sair da equipe. Para encerrar, exclua o time.");
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw BusinessException.badRequest("Você não é membro desta equipe");
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        auditLogRepository.save(AuditLogEntity.builder()
                .user(user)
                .action("TEAM_LEFT")
                .entityType("TEAM")
                .entityId(teamId)
                .metadata(Map.of("team", team.getName()))
                .build());
    }


    private TeamSummaryDTO toSummary(TeamEntity t) {
        return new TeamSummaryDTO(
                t.getId(), t.getName(), t.getSlug(),
                t.getTechStack(), t.getLocation(), t.getTeamScore()
        );
    }

    private TeamResponseDTO toResponse(TeamEntity t) {
        List<TeamResponseDTO.TeamMemberDTO> members = teamMemberRepository
                .findAllByTeamId(t.getId()).stream().map(this::toMemberDTO).toList();

        List<TeamResponseDTO.TeamPortfolioDTO> portfolio = teamPortfolioRepository
                .findAllByTeamIdOrderByCreatedAtDesc(t.getId()).stream()
                .map(p -> new TeamResponseDTO.TeamPortfolioDTO(
                        p.getId(), p.getTitle(), p.getDescription(), p.getUrl()
                )).toList();

        return new TeamResponseDTO(
                t.getId(), t.getName(), t.getSlug(), t.getDescription(),
                t.getTechStack(), t.getLocation(), t.getTeamScore(),
                t.getOwner().getId(), t.getOwner().getName(), members, portfolio
        );
    }

    private TeamResponseDTO.TeamMemberDTO toMemberDTO(TeamMemberEntity m) {
        return new TeamResponseDTO.TeamMemberDTO(
                m.getUser().getId(),
                m.getUser().getName(),
                m.getUser().getAvatarColor(),
                m.getRoleInTeam()
        );
    }
}
