package sharktank.teamlancer.domain.team.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateInviteRequestDTO(
        @NotBlank(message = "O email do usuário é obrigatório")
        @Email(message = "Email inválido")
        String email,
        
        @NotBlank(message = "A função na equipe é obrigatória")
        String roleInTeam
) {}