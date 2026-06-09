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
import sharktank.teamlancer.domain.team.dto.CreateJoinRequestDTO;
import sharktank.teamlancer.domain.team.dto.JoinRequestResponseDTO;
import sharktank.teamlancer.domain.team.dto.UpdateJoinRequestStatusDTO;
import sharktank.teamlancer.domain.team.service.TeamJoinRequestService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Join Requests", description = "Solicitações de entrada em equipes")
@SecurityRequirement(name = "bearerAuth")
public class JoinRequestController {

    private final TeamJoinRequestService joinRequestService;
    private final UserRepository userRepository;

    @PostMapping("/join-requests")
    @Operation(summary = "Usuário solicita entrada em uma equipe")
    public ResponseEntity<JoinRequestResponseDTO> create(
            @Valid @RequestBody CreateJoinRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(joinRequestService.create(dto, userId));
    }

    @GetMapping("/join-requests/me")
    @Operation(summary = "Listar as solicitações enviadas pelo usuário logado")
    public ResponseEntity<List<JoinRequestResponseDTO>> getMyRequests(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(joinRequestService.getMyRequests(userId));
    }

    @GetMapping("/teams/{teamId}/join-requests")
    @Operation(summary = "Listar solicitações recebidas pela equipe (Somente Dono)")
    public ResponseEntity<List<JoinRequestResponseDTO>> getTeamRequests(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID ownerId = resolveUserId(userDetails);
        return ResponseEntity.ok(joinRequestService.getTeamRequests(teamId, ownerId));
    }

    @PatchMapping("/join-requests/{id}")
    @Operation(summary = "Aceitar, rejeitar (Dono) ou cancelar (Usuário) a solicitação")
    public ResponseEntity<JoinRequestResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJoinRequestStatusDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(joinRequestService.updateStatus(id, dto, callerId));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}