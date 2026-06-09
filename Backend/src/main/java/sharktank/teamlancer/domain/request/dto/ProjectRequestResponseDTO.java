package sharktank.teamlancer.domain.request.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectRequestResponseDTO(
        UUID id,
        UUID clientId,
        String clientName,
        UUID teamId,
        String teamName,
        String teamSlug,
        String projectName,
        String description,
        String budgetRange,
        String deadline,
        String status,
        String projectId,
        LocalDateTime createdAt
) {}