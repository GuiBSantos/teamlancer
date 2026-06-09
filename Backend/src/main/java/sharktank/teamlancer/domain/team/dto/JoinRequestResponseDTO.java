package sharktank.teamlancer.domain.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record JoinRequestResponseDTO(
        UUID id,
        UUID teamId,
        String teamName,
        UUID userId,
        String userName,
        String message,
        String status,
        LocalDateTime createdAt
) {}