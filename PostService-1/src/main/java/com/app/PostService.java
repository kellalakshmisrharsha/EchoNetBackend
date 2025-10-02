package com.app;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, LikeRepository likeRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    // Posts
    public Mono<Post> createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Flux<Post> getAllPosts() {
        return postRepository.findAllByIsActiveOrderByCreatedAtDesc(true);
    }

    public Flux<Post> getPostsByUser(Long userId) {
        return postRepository.findAllByUserIdAndIsActiveOrderByCreatedAtDesc(userId, true);
    }

    public Mono<Void> deletePost(Long postId, Long userId) {
        return postRepository.findById(postId)
            .filter(post -> post.getUserId().equals(userId))
            .flatMap(post -> {
                post.setIsActive(false);
                post.setUpdatedAt(LocalDateTime.now());
                return postRepository.save(post);
            })
            .then();
    }

    // Likes
    public Mono<Like> addLike(Like like) {
        like.setCreatedAt(LocalDateTime.now());
        // Check if already liked to prevent duplicates
        return likeRepository.findByPostIdAndUserId(like.getPostId(), like.getUserId())
            .switchIfEmpty(likeRepository.save(like));
    }

    public Mono<Void> removeLike(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId)
            .flatMap(likeRepository::delete)
            .then();
    }

    public Flux<Like> getLikes(Long postId) {
        return likeRepository.findAllByPostId(postId);
    }

    public Mono<Boolean> isLikedByUser(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId)
            .map(like -> true)
            .defaultIfEmpty(false);
    }

    // Comments
    public Mono<Comment> addComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public Flux<Comment> getComments(Long postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);
    }
}
