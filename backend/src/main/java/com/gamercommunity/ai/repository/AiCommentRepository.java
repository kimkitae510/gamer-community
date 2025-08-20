package com.gamercommunity.ai.repository;

import com.gamercommunity.ai.entity.AiComment;
import com.gamercommunity.ai.entity.AiCommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiCommentRepository extends JpaRepository<AiComment, Long> {

    Optional<AiComment> findByIdAndPostIdAndUserIdAndStatus(
            Long id, Long postId, Long userId, AiCommentStatus status);
}
