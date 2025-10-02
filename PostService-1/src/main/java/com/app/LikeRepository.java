package com.app;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LikeRepository extends ReactiveCrudRepository<Like, Long> {
    Flux<Like> findAllByPostId(Long postId);
    Mono<Like> findByPostIdAndUserId(Long postId, Long userId);
}

