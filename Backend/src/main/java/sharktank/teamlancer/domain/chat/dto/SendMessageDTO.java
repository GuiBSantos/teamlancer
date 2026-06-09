package sharktank.teamlancer.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageDTO(
        @NotBlank(message = "Mensagem não pode ser vazia")
        @Size(max = 2000, message = "Mensagem muito longa")
        String content
) {}

