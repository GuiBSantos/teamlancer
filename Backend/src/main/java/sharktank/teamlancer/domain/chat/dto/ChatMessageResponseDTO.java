package sharktank.teamlancer.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponseDTO(
        UUID id,
        UUID senderId,
        String senderName,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
}
