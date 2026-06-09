package sharktank.teamlancer.domain.team.dto;

import java.util.List;
import java.util.UUID;

public record TeamResponseDTO(
        UUID id,
        String name,
        String slug,
        String description,
        String[] techStack,
        String location,
        Integer teamScore,
        UUID ownerId,
        String ownerName,
        List<TeamMemberDTO> members,
        List<TeamPortfolioDTO> portfolio
) {
    public record TeamMemberDTO(
            UUID userId,
            String name,
            String avatarUrl,
            String roleInTeam
    ) {}

    public record TeamPortfolioDTO(
            UUID id,
            String title,
            String description,
            String url
    ) {}
}
