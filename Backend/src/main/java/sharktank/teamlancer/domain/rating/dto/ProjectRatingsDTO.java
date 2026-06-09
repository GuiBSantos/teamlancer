package sharktank.teamlancer.domain.rating.dto;

public record ProjectRatingsDTO(
        RatingResponseDTO clientRating,
        RatingResponseDTO teamRating
) {
}
