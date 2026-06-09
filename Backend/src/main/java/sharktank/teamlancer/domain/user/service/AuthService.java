package sharktank.teamlancer.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.user.dto.AuthResponse;
import sharktank.teamlancer.domain.user.dto.LoginRequest;
import sharktank.teamlancer.domain.user.dto.RefreshRequest;
import sharktank.teamlancer.domain.user.dto.RegisterRequest;
import sharktank.teamlancer.domain.user.entity.UserCredentialsEntity;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.entity.UserRole;
import sharktank.teamlancer.domain.user.repository.UserCredentialsRepository;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.security.JwtService;
import sharktank.teamlancer.shared.audit.AuditLogEntity;
import sharktank.teamlancer.shared.audit.AuditLogRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw BusinessException.conflict("E-mail já cadastrado");
        }

        UserRole role = req.role() != null ? req.role() : UserRole.CLIENT;

        if (role == UserRole.ADMIN) {
            throw BusinessException.forbidden("Não é possível registrar como ADMIN");
        }

        UserEntity user = UserEntity.builder()
                .email(req.email().toLowerCase().trim())
                .name(req.name().trim())
                .role(role)
                .location(req.location())
                .bio(req.bio())
                .build();

        userRepository.save(user);

        UserCredentialsEntity credentials = UserCredentialsEntity.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();

        credentialsRepository.save(credentials);

        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getEmail(), role.name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        credentials.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        credentialsRepository.save(credentials);

        auditLogRepository.save(AuditLogEntity.builder()
                .user(user)
                .action("USER_REGISTER")
                .entityType("USER")
                .entityId(user.getId())
                .metadata(Map.of("role", role.name()))
                .build());

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
                role.name(), user.getAvatarColor(), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        UserCredentialsEntity credentials = credentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> BusinessException.notFound("Credenciais não encontradas"));

        credentials.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        credentials.setLastLogin(LocalDateTime.now());
        credentialsRepository.save(credentials);

        auditLogRepository.save(AuditLogEntity.builder()
                .user(user)
                .action("USER_LOGIN")
                .entityType("USER")
                .entityId(user.getId())
                .build());

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), user.getAvatarColor(), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        if (!jwtService.isValid(req.refreshToken())) {
            throw BusinessException.badRequest("Refresh token inválido ou expirado");
        }

        UUID userId = jwtService.extractUserId(req.refreshToken());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        String newAccessToken  = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        UserCredentialsEntity credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("Credenciais não encontradas"));

        credentials.setRefreshTokenHash(passwordEncoder.encode(newRefreshToken));
        credentialsRepository.save(credentials);

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), user.getAvatarColor(), newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId) {
        credentialsRepository.clearRefreshToken(userId);

        userRepository.findById(userId).ifPresent(user ->
                auditLogRepository.save(AuditLogEntity.builder()
                        .user(user)
                        .action("USER_LOGOUT")
                        .entityType("USER")
                        .entityId(userId)
                        .build())
        );
    }
}