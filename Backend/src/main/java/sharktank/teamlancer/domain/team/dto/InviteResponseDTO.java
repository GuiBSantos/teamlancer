package sharktank.teamlancer.domain.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InviteResponseDTO(
        UUID id,
        UUID teamId,
        String teamName,
        String teamSlug,
        UUID invitedUserId,
        String invitedUserName,
        String invitedUserEmail,
        String roleInTeam,
        String status,
        LocalDateTime createdAt
) {}