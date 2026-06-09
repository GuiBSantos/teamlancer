package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateJoinRequestDTO(
        @NotNull(message = "O ID da equipe é obrigatório")
        UUID teamId,
        String message
) {}