package sharktank.teamlancer.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;
import sharktank.teamlancer.domain.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "team_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_team_member",
        columnNames = {"team_id", "user_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "role_in_team", nullable = false, length = 100)
    private String roleInTeam;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}