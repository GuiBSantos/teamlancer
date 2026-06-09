package sharktank.teamlancer.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharktank.teamlancer.domain.chat.entity.ChatMessageEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    List<ChatMessageEntity> findAllByProjectIdOrderByCreatedAtAsc(UUID projectId);

    long countByProjectIdAndReadAtIsNullAndSenderIdNot(UUID projectId, UUID excludeSenderId);
}
