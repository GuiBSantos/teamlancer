package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddMemberDTO(

        @NotNull(message = "userId é obrigatório")
        UUID userId,

        @NotBlank(message = "Papel no time é obrigatório")
        String roleInTeam
) {}