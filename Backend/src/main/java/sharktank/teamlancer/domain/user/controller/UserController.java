package sharktank.teamlancer.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.user.dto.UserResponse;
import sharktank.teamlancer.domain.user.dto.UserUpdateRequest;
import sharktank.teamlancer.domain.user.service.UserService;
import sharktank.teamlancer.shared.exception.BusinessException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento de usuários e perfis")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário logado")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar perfil do usuário logado")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userDetails.getUsername(), request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));
    }
}
