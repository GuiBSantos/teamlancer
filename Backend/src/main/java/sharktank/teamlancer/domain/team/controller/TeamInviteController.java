package sharktank.teamlancer.domain.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.team.dto.CreateInviteRequestDTO;
import sharktank.teamlancer.domain.team.dto.InviteResponseDTO;
import sharktank.teamlancer.domain.team.dto.UpdateInviteStatusDTO;
import sharktank.teamlancer.domain.team.service.TeamInviteService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Team Invites", description = "Convites para entrar em equipes")
@SecurityRequirement(name = "bearerAuth")
public class TeamInviteController {

    private final TeamInviteService inviteService;
    private final UserRepository userRepository;

    @PostMapping("/teams/{teamId}/invites")
    @Operation(summary = "Dono da equipe envia um convite para um usuário por email")
    public ResponseEntity<InviteResponseDTO> create(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateInviteRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID ownerId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteService.create(teamId, dto, ownerId));
    }

    @GetMapping("/invites/me")
    @Operation(summary = "Listar os convites recebidos pelo usuário logado")
    public ResponseEntity<List<InviteResponseDTO>> getMyInvites(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(inviteService.getMyInvites(userId));
    }

    @GetMapping("/teams/{teamId}/invites")
    @Operation(summary = "Listar os convites enviados pela equipe (Somente Dono)")
    public ResponseEntity<List<InviteResponseDTO>> getTeamInvites(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID ownerId = resolveUserId(userDetails);
        return ResponseEntity.ok(inviteService.getTeamInvites(teamId, ownerId));
    }

    @PatchMapping("/invites/{id}")
    @Operation(summary = "Aceitar, rejeitar (Usuário) ou cancelar (Dono) o convite")
    public ResponseEntity<InviteResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInviteStatusDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(inviteService.updateStatus(id, dto, callerId));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}