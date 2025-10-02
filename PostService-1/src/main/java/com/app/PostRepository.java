package com.app;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
    Flux<Post> findAllByUserId(Long userId);
    Flux<Post> findAllByIsActiveOrderByCreatedAtDesc(Boolean isActive);
    Flux<Post> findAllByUserIdAndIsActiveOrderByCreatedAtDesc(Long userId, Boolean isActive);
}

