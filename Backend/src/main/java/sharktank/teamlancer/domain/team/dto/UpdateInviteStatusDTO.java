package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.NotNull;
import sharktank.teamlancer.domain.team.entity.InviteStatus;

public record UpdateInviteStatusDTO(
        @NotNull(message = "O status é obrigatório")
        InviteStatus status
) {}