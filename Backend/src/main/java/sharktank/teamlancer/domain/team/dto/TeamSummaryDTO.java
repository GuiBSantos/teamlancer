package sharktank.teamlancer.domain.team.dto;

import java.util.UUID;

public record TeamSummaryDTO(
        UUID id,
        String name,
        String slug,
        String[] techStack,
        String location,
        Integer teamScore
) {}