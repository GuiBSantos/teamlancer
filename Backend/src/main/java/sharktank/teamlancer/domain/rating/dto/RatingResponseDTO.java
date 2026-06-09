package sharktank.teamlancer.domain.rating.dto;

import sharktank.teamlancer.domain.rating.entity.RaterType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RatingResponseDTO(
        UUID id,
        UUID projectId,
        RaterType raterType,
        Integer score,
        String comment,
        LocalDateTime createdAt
) {}

