package sharktank.teamlancer.domain.rating.controller;

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
import sharktank.teamlancer.domain.rating.dto.CreateRatingDTO;
import sharktank.teamlancer.domain.rating.dto.ProjectRatingsDTO;
import sharktank.teamlancer.domain.rating.dto.RatingResponseDTO;
import sharktank.teamlancer.domain.rating.service.RatingService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Avaliações mútuas após conclusão do projeto")
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Avaliações do projeto (cliente e time)")
    public ResponseEntity<ProjectRatingsDTO> getProjectRatings(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(ratingService.getProjectRatings(projectId, callerId));
    }

    @PostMapping
    @Operation(summary = "Avaliar o projeto — liberado apenas após COMPLETED")
    public ResponseEntity<RatingResponseDTO> rate(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateRatingDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.rate(projectId, dto, callerId));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}
