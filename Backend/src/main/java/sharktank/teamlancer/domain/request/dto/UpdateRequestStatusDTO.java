package sharktank.teamlancer.domain.request.dto;

import jakarta.validation.constraints.NotNull;
import sharktank.teamlancer.domain.request.entity.RequestStatus;

public record UpdateRequestStatusDTO(

        @NotNull(message = "Status é obrigatório")
        RequestStatus status
) {}