package sharktank.teamlancer.domain.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.project.entity.ProjectEntity;
import sharktank.teamlancer.domain.project.entity.ProjectStatus;
import sharktank.teamlancer.domain.project.repository.ProjectRepository;
import sharktank.teamlancer.domain.rating.dto.CreateRatingDTO;
import sharktank.teamlancer.domain.rating.dto.ProjectRatingsDTO;
import sharktank.teamlancer.domain.rating.dto.RatingResponseDTO;
import sharktank.teamlancer.domain.rating.entity.RaterType;
import sharktank.teamlancer.domain.rating.entity.RatingEntity;
import sharktank.teamlancer.domain.rating.repository.RatingRepository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectRatingsDTO getProjectRatings(UUID projectId, UUID callerId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));
        checkAccess(project, callerId);

        RatingResponseDTO clientRating = ratingRepository
                .findByProjectIdAndRaterType(projectId, RaterType.CLIENT)
                .map(this::toDTO).orElse(null);

        RatingResponseDTO teamRating = ratingRepository
                .findByProjectIdAndRaterType(projectId, RaterType.TEAM)
                .map(this::toDTO).orElse(null);

        return new ProjectRatingsDTO(clientRating, teamRating);
    }

    @Transactional
    public RatingResponseDTO rate(UUID projectId, CreateRatingDTO dto, UUID callerId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw BusinessException.badRequest("Avaliações só são permitidas após a conclusão do projeto");
        }

        UserEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        RaterType raterType = resolveRaterType(project, callerId);

        if (ratingRepository.existsByProjectIdAndRaterType(projectId, raterType)) {
            throw BusinessException.conflict("Você já avaliou este projeto");
        }

        RatingEntity rating = RatingEntity.builder()
                .project(project)
                .rater(caller)
                .raterType(raterType)
                .score(dto.score())
                .comment(dto.comment())
                .build();

        ratingRepository.save(rating);
        updateTeamScore(project.getTeam().getId());

        return toDTO(rating);
    }


    private RaterType resolveRaterType(ProjectEntity project, UUID callerId) {
        if (project.getClient().getId().equals(callerId)) return RaterType.CLIENT;
        if (project.getTeam().getOwner().getId().equals(callerId)) return RaterType.TEAM;
        throw BusinessException.forbidden("Sem permissão para avaliar este projeto");
    }

    private void checkAccess(ProjectEntity project, UUID userId) {
        boolean isClient    = project.getClient().getId().equals(userId);
        boolean isTeamOwner = project.getTeam().getOwner().getId().equals(userId);
        if (!isClient && !isTeamOwner) {
            throw BusinessException.forbidden("Sem acesso a este projeto");
        }
    }

    private void updateTeamScore(UUID teamId) {
        Double avg = ratingRepository.avgScoreByTeamId(teamId);
        int newScore = (int) Math.round((avg / 5.0) * 100);
        projectRepository.flush();
    }

    private RatingResponseDTO toDTO(RatingEntity r) {
        return new RatingResponseDTO(
                r.getId(),
                r.getProject().getId(),
                r.getRaterType(),
                r.getScore(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
