package sharktank.teamlancer.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    @Size(min = 2, max = 100)
    private String name;
    private String location;
    private String bio;
    private String avatarColor;
}
