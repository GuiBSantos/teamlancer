package sharktank.teamlancer.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.user.dto.AuthResponse;
import sharktank.teamlancer.domain.user.dto.LoginRequest;
import sharktank.teamlancer.domain.user.dto.RefreshRequest;
import sharktank.teamlancer.domain.user.dto.RegisterRequest;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.domain.user.service.AuthService;
import sharktank.teamlancer.shared.exception.BusinessException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro, login e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário (CLIENT ou MEMBER)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login — retorna access_token e refresh_token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renova o access_token usando o refresh_token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessão — invalida o refresh_token")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        userRepository.findByEmail(userDetails.getUsername())
                .ifPresentOrElse(
                        user -> authService.logout(user.getId()),
                        () -> { throw BusinessException.notFound("Usuário não encontrado"); }
                );
        return ResponseEntity.noContent().build();
    }
}