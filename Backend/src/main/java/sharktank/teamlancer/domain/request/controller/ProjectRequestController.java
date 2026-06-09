package sharktank.teamlancer.domain.request.controller;

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
import sharktank.teamlancer.domain.request.dto.CreateProjectRequestDTO;
import sharktank.teamlancer.domain.request.dto.ProjectRequestResponseDTO;
import sharktank.teamlancer.domain.request.dto.UpdateRequestStatusDTO;
import sharktank.teamlancer.domain.request.service.ProjectRequestService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "Solicitações de projeto")
@SecurityRequirement(name = "bearerAuth")
public class ProjectRequestController {

    private final ProjectRequestService requestService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Cliente envia solicitação para uma equipe")
    public ResponseEntity<ProjectRequestResponseDTO> create(
            @Valid @RequestBody CreateProjectRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID clientId = resolveUserId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(requestService.create(dto, clientId));
    }

    @GetMapping("/me")
    @Operation(summary = "Minhas solicitações como cliente (dashboard)")
    public ResponseEntity<Page<ProjectRequestResponseDTO>> myRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID clientId = resolveUserId(userDetails);
        return ResponseEntity.ok(requestService.myRequests(clientId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma solicitação (confirmação)")
    public ResponseEntity<ProjectRequestResponseDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(requestService.getById(id, callerId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Equipe aceita ou rejeita; cliente pode cancelar")
    public ResponseEntity<ProjectRequestResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequestStatusDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(requestService.updateStatus(id, dto, callerId));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Solicitações recebidas por uma equipe")
    public ResponseEntity<Page<ProjectRequestResponseDTO>> teamRequests(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(requestService.teamRequests(teamId, callerId, pageable));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}