package sharktank.teamlancer.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharktank.teamlancer.domain.chat.dto.ChatMessageResponseDTO;
import sharktank.teamlancer.domain.chat.dto.SendMessageDTO;
import sharktank.teamlancer.domain.chat.entity.ChatMessageEntity;
import sharktank.teamlancer.domain.chat.repository.ChatMessageRepository;
import sharktank.teamlancer.domain.project.entity.ProjectEntity;
import sharktank.teamlancer.domain.project.repository.ProjectRepository;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ChatMessageResponseDTO> getMessages(UUID projectId, UUID callerId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));

        checkAccess(project, callerId);

        chatRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .filter(m -> !m.getSender().getId().equals(callerId) && m.getReadAt() == null)
                .forEach(m -> {
                    m.setReadAt(LocalDateTime.now());
                    chatRepository.save(m);
                });

        return chatRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ChatMessageResponseDTO send(UUID projectId, SendMessageDTO dto, UUID senderId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> BusinessException.notFound("Projeto não encontrado"));

        checkAccess(project, senderId);

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> BusinessException.notFound("Usuário não encontrado"));

        ChatMessageEntity msg = ChatMessageEntity.builder()
                .project(project)
                .sender(sender)
                .content(dto.content().trim())
                .build();

        return toDTO(chatRepository.save(msg));
    }

    public long countUnread(UUID projectId, UUID callerId) {
        return chatRepository.countByProjectIdAndReadAtIsNullAndSenderIdNot(projectId, callerId);
    }


    private void checkAccess(ProjectEntity project, UUID userId) {
        boolean isClient   = project.getClient().getId().equals(userId);
        boolean isTeamOwner = project.getTeam().getOwner().getId().equals(userId);
        if (!isClient && !isTeamOwner) {
            throw BusinessException.forbidden("Sem acesso a este projeto");
        }
    }

    private ChatMessageResponseDTO toDTO(ChatMessageEntity m) {
        return new ChatMessageResponseDTO(
                m.getId(),
                m.getSender().getId(),
                m.getSender().getName(),
                m.getContent(),
                m.getReadAt() != null,
                m.getCreatedAt()
        );
    }
}
