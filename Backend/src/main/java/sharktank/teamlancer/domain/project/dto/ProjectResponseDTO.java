package sharktank.teamlancer.domain.project.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        UUID requestId,
        UUID clientId,
        String clientName,
        UUID teamId,
        String teamName,
        String teamSlug,
        String name,
        String description,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
