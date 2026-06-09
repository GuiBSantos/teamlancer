package sharktank.teamlancer.domain.user.dto;

import lombok.Builder;
import lombok.Data;
import sharktank.teamlancer.domain.user.entity.UserRole;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID userId;
    private String name;
    private String email;
    private UserRole role;
    private String location;
    private String bio;
    private String avatarColor;
}