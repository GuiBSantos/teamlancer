package sharktank.teamlancer.domain.user.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String name,
        String email,
        String role,
        String avatarColor,
        String accessToken,
        String refreshToken
) {}