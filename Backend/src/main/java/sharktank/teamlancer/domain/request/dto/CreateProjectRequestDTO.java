package sharktank.teamlancer.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProjectRequestDTO(

        @NotNull(message = "teamId é obrigatório")
        UUID teamId,

        @NotBlank(message = "Nome do projeto é obrigatório")
        @Size(max = 200)
        String projectName,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        String budgetRange,

        String deadline
) {}