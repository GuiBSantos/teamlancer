package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        String[] techStack,

        String location
) {}