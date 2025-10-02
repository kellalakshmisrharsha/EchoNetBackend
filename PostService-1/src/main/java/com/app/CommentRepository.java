package com.app;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {
    Flux<Comment> findAllByPostId(Long postId);
    Flux<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);
}
