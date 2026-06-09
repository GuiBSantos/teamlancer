package sharktank.teamlancer.domain.project.dto;

import jakarta.validation.constraints.NotNull;
import sharktank.teamlancer.domain.project.entity.ProjectStatus;

public record UpdateProjectStatusDTO(
        @NotNull(message = "Status é obrigatório")
        ProjectStatus status
) {}

