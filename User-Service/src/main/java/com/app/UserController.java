package com.app;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final FriendService friendService;
    private final UserRepo userRepo;

    public UserController(FriendService friendService, UserRepo userRepo) {
        this.friendService = friendService;
        this.userRepo = userRepo;
    }

    // Get current user profile
    @GetMapping("/profile")
    public Mono<User> getCurrentUserProfile(ServerWebExchange exchange) {
        Long userId = (Long) exchange.getAttribute("userId");
        return Mono.justOrEmpty(userRepo.findById(userId));
    }

    // Get user profile by ID
    @GetMapping("/{userId}")
    public Mono<User> getUserProfile(@PathVariable Long userId) {
        return Mono.justOrEmpty(userRepo.findById(userId));
    }

    // Update user profile
    @PutMapping("/profile")
    public Mono<User> updateProfile(ServerWebExchange exchange, @RequestBody User userUpdate) {
        Long userId = (Long) exchange.getAttribute("userId");
        return Mono.fromCallable(() -> {
            User existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update allowed fields
            if (userUpdate.getName() != null) existingUser.setName(userUpdate.getName());
            if (userUpdate.getBio() != null) existingUser.setBio(userUpdate.getBio());
            if (userUpdate.getLocation() != null) existingUser.setLocation(userUpdate.getLocation());
            if (userUpdate.getWebsite() != null) existingUser.setWebsite(userUpdate.getWebsite());
            if (userUpdate.getProfileImageUrl() != null) existingUser.setProfileImageUrl(userUpdate.getProfileImageUrl());
            
            return userRepo.save(existingUser);
        });
    }

    // Get friends list
    @GetMapping("/friends")
    public Mono<List<User>> getFriends(ServerWebExchange exchange) {
        Long userId = (Long) exchange.getAttribute("userId");
        return Mono.just(friendService.getFriendsOfUser(userId));
    }

    // Get available users (for friend suggestions)
    @GetMapping("/available-users")
    public Mono<List<User>> getAvailableUsers(ServerWebExchange exchange) {
        Long userId = (Long) exchange.getAttribute("userId");
        return Mono.just(friendService.getAvailableUsers(userId));
    }

    // Send friend request / Add friend
    @PostMapping("/friends/{friendId}")
    public Mono<Friend> addFriend(ServerWebExchange exchange, @PathVariable Long friendId) {
        Long userId = (Long) exchange.getAttribute("userId");
        return Mono.just(friendService.addFriend(userId, friendId));
    }

    // Remove friend
    @DeleteMapping("/friends/{friendId}")
    public Mono<Void> removeFriend(ServerWebExchange exchange, @PathVariable Long friendId) {
        Long userId = (Long) exchange.getAttribute("userId");
        friendService.removeFriend(userId, friendId);
        return Mono.empty();
    }

    // Search users
    @GetMapping("/search")
    public Mono<List<User>> searchUsers(@RequestParam String query) {
        return Mono.just(friendService.searchUsers(query));
    }

    // Get all users (for admin or testing)
    @GetMapping
    public Mono<List<User>> getAllUsers() {
        return Mono.just(userRepo.findAll());
    }
}
