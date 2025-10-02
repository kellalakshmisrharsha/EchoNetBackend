package com.app;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

// Add these imports for JWT handling
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private LikeCommentService likeCommentService;

    private final PostService postService;
    private final MediaService mediaService;
    
    // Add your JWT secret key - this should match your auth service
    private final String JWT_SECRET = "mYs3cR3tK3yForJWTs1234567890abcdef";
    private final SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    public PostController(PostService postService, MediaService mediaService) {
        this.postService = postService;
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> createPost(
            @RequestPart("title") Mono<String> title,
            @RequestPart("content") Mono<String> content,
            @RequestPart(value = "file", required = false) Mono<FilePart> file,
            @RequestPart(value = "type", required = false) Mono<String> type) {
        
        return Mono.zip(title, content, type.defaultIfEmpty(""))
            .flatMap(tuple -> {
                String titleStr = tuple.getT1();
                String contentStr = tuple.getT2();
                String typeStr = tuple.getT3();
                
                Post post = new Post();
                post.setTitle(titleStr);
                post.setContent(contentStr);
                
                return Mono.fromCallable(() -> postService.savePost(post))
                    .flatMap(savedPost -> 
                        file.flatMap(f -> 
                            mediaService.saveMedia(savedPost, f, typeStr)
                                .flatMap(savedMedia -> {
                                    savedPost.setMedia(savedMedia);
                                    return Mono.fromCallable(() -> postService.savePost(savedPost));
                                })
                        ).switchIfEmpty(Mono.just(savedPost))
                    )
                    .map(p -> ResponseEntity.ok(Map.of("message", "Post created successfully", "postId", p.getId().toString())));
            })
            .onErrorResume(e -> {
                System.err.println("Error creating post: " + e.getMessage());
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create post: " + e.getMessage())));
            });
    }

    @GetMapping
    public Mono<ResponseEntity<List<Post>>> getAllPosts() {
        return Mono.fromCallable(() ->
            ResponseEntity.ok(postService.getAllPosts())
        ).onErrorResume(e -> {
            System.err.println("Error fetching posts: " + e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.SC_SERVER_ERROR).build());
        });
    }

    @GetMapping("/{postId}")
    public Mono<ResponseEntity<? extends Object>> getPostById(@PathVariable Long postId) {
        return Mono.fromCallable(() -> {
            try {
                Post post = postService.getPostById(postId);
                return ResponseEntity.ok(post);
            } catch (Exception e) {
                return ResponseEntity.notFound().build();
            }
        }).onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    // Serve both images and videos
    @GetMapping("/{postId}/media")
    public Mono<ResponseEntity<? extends Object>> getPostMedia(@PathVariable Long postId) {
        return Mono.fromCallable(() -> {
            try {
                Post post = postService.getPostById(postId);
                if (post.getMedia() == null) {
                    return ResponseEntity.notFound().build();
                }
                byte[] mediaBytes = mediaService.getMediaBytes(post.getMedia());
                String contentType = post.getMedia().getContentType();
                if (mediaBytes == null || contentType == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(mediaBytes);
            } catch (Exception e) {
                return ResponseEntity.notFound().build();
            }
        }).onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    // Like endpoints
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromToken(request);
            boolean isLiked = likeCommentService.toggleLike(postId, userId);
            int likesCount = likeCommentService.getLikesCount(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("likesCount", likesCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body("Error toggling like: " + e.getMessage());
        }
    }

    @GetMapping("/{postId}/like-status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromToken(request);
            boolean isLiked = likeCommentService.isLikedByUser(postId, userId);
            int likesCount = likeCommentService.getLikesCount(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("likesCount", likesCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body("Error getting like status: " + e.getMessage());
        }
    }

    // Comment endpoints
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, 
                                       @RequestBody Map<String, String> request, 
                                       HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserIdFromToken(httpRequest);
            String content = request.get("content");
            
            Comment comment = likeCommentService.addComment(postId, userId, content);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body("Error adding comment: " + e.getMessage());
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<Comment> comments = likeCommentService.getCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body("Error getting comments: " + e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromToken(request);
            likeCommentService.deleteComment(commentId, userId);
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_SERVER_ERROR)
                    .body("Error deleting comment: " + e.getMessage());
        }
    }

    // JWT Token extraction method
    private Long extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                // Extract user ID from claims - adjust this based on how your JWT is structured
                Object userIdClaim = claims.get("userId");
                
                if (userIdClaim != null) {
                    return Long.valueOf(userIdClaim.toString());
                }
                
                throw new RuntimeException("User ID not found in token");
            } catch (Exception e) {
                throw new RuntimeException("Invalid JWT token: " + e.getMessage());
            }
        }
        throw new RuntimeException("No valid authorization token found");
    }
}