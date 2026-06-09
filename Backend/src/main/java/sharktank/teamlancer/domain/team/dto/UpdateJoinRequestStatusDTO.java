package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.NotNull;
import sharktank.teamlancer.domain.team.entity.JoinRequestStatus;

public record UpdateJoinRequestStatusDTO(
        @NotNull(message = "O status é obrigatório")
        JoinRequestStatus status
) {}