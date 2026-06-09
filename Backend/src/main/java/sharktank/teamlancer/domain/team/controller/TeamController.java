package sharktank.teamlancer.domain.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.team.dto.*;
import sharktank.teamlancer.domain.team.service.TeamService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Listagem, perfil e gestão de equipes")
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Lista equipes")
    public ResponseEntity<Page<TeamSummaryDTO>> listTeams(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "teamScore") Pageable pageable
    ) {
        return ResponseEntity.ok(teamService.listTeams(q, pageable));
    }

    @GetMapping("/featured")
    @Operation(summary = "Top 3 equipes para a homepage")
    public ResponseEntity<List<TeamSummaryDTO>> featured() {
        return ResponseEntity.ok(teamService.featuredTeams());
    }

    @GetMapping("/mine")
    @Operation(summary = "Busca o time do usuário logado (owner ou membro)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TeamResponseDTO> getMyTeam(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(teamService.getMyTeamAsMember(userId));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Perfil completo de uma equipe pelo UUID")
    public ResponseEntity<TeamResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getById(id));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Perfil completo de uma equipe pelo slug")
    public ResponseEntity<TeamResponseDTO> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(teamService.getBySlug(slug));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Membros de uma equipe")
    public ResponseEntity<List<TeamResponseDTO.TeamMemberDTO>> members(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getMembers(id));
    }

    @GetMapping("/{id}/portfolio")
    @Operation(summary = "Portfólio de uma equipe")
    public ResponseEntity<List<TeamResponseDTO.TeamPortfolioDTO>> portfolio(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getPortfolio(id));
    }

    @PostMapping
    @Operation(summary = "Criar equipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TeamResponseDTO> create(
            @Valid @RequestBody CreateTeamDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID ownerId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(dto, ownerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta equipe (somente owner)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID ownerId = resolveUserId(userDetails);
        teamService.deleteTeam(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Membro sai do time voluntariamente",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> leaveTeam(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = resolveUserId(userDetails);
        teamService.leaveTeam(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Adicionar membro à equipe",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID requesterId = resolveUserId(userDetails);
        teamService.addMember(id, dto, requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remover membro da equipe (owner remove outro)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID requesterId = resolveUserId(userDetails);
        teamService.removeMember(id, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}
