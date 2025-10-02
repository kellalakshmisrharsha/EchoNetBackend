package com.app;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(Post post) {
        try {
            return postRepository.save(post);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save post: " + e.getMessage(), e);
        }
    }

    public List<Post> getAllPosts() {
        try {
            return postRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch posts: " + e.getMessage(), e);
        }
    }

    public Post getPostById(Long id) {
        try {
            return postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch post with id " + id + ": " + e.getMessage(), e);
        }
    }

    public Optional<Post> findPostById(Long id) {
        try {
            return postRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find post with id " + id + ": " + e.getMessage(), e);
        }
    }

    public void deletePost(Long id) {
        try {
            if (!postRepository.existsById(id)) {
                throw new RuntimeException("Post not found with id: " + id);
            }
            postRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete post with id " + id + ": " + e.getMessage(), e);
        }
    }

    public Post updatePost(Post post) {
        try {
            if (post.getId() == null) {
                throw new RuntimeException("Post ID cannot be null for update");
            }
            if (!postRepository.existsById(post.getId())) {
                throw new RuntimeException("Post not found with id: " + post.getId());
            }
            return postRepository.save(post);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update post: " + e.getMessage(), e);
        }
    }

    public List<Post> getPostsByUserId(Long userId) {
        try {
            // Assuming you have a method in repository to find by user
            // return postRepository.findByUserId(userId);
            // For now, returning all posts - you can implement user-specific logic
            return postRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch posts for user " + userId + ": " + e.getMessage(), e);
        }
    }

    public boolean existsById(Long id) {
        try {
            return postRepository.existsById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if post exists with id " + id + ": " + e.getMessage(), e);
        }
    }

    public long getPostCount() {
        try {
            return postRepository.count();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get post count: " + e.getMessage(), e);
        }
    }
}