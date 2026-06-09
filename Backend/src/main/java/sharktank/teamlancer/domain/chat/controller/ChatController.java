package sharktank.teamlancer.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sharktank.teamlancer.domain.chat.dto.ChatMessageResponseDTO;
import sharktank.teamlancer.domain.chat.dto.SendMessageDTO;
import sharktank.teamlancer.domain.chat.service.ChatService;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Mensagens entre cliente e equipe por projeto")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Busca todas as mensagens do projeto (e marca como lidas)")
    public ResponseEntity<List<ChatMessageResponseDTO>> getMessages(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(chatService.getMessages(projectId, callerId));
    }

    @PostMapping
    @Operation(summary = "Envia uma mensagem no chat do projeto")
    public ResponseEntity<ChatMessageResponseDTO> send(
            @PathVariable UUID projectId,
            @Valid @RequestBody SendMessageDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.send(projectId, dto, callerId));
    }

    @GetMapping("/unread")
    @Operation(summary = "Conta mensagens não lidas do projeto")
    public ResponseEntity<Map<String, Long>> unread(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID callerId = resolveUserId(userDetails);
        return ResponseEntity.ok(Map.of("count", chatService.countUnread(projectId, callerId)));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"))
                .getId();
    }
}
