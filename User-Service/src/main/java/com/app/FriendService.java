package com.app;

import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class FriendService {

    private final FriendRepo friendRepo;
    private final UserRepo userRepo;

    public FriendService(FriendRepo friendRepo, UserRepo userRepo) {
        this.friendRepo = friendRepo;
        this.userRepo = userRepo;
    }

    public List<User> getFriendsOfUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Friend> friendsList = friendRepo.findByUser(user);
        return friendsList.stream()
                          .map(Friend::getFriend)
                          .toList();
    }
    public Friend addFriend(Long userId, Long friendId) {
        User user = userRepo.findById(userId).orElseThrow();
        User friend = userRepo.findById(friendId).orElseThrow();

        // Check if friendship already exists
        boolean exists = friendRepo.existsByUserAndFriend(user, friend);
        if (!exists) {
            // Create first direction
            Friend f1 = new Friend();
            f1.setUser(user);
            f1.setFriend(friend);
            friendRepo.save(f1);

            // Create reverse direction for bi-directional friendship
            Friend f2 = new Friend();
            f2.setUser(friend);
            f2.setFriend(user);
            friendRepo.save(f2);

            return f1;
        }

        return null; // or throw exception if already friends
    }


    public void removeFriend(Long userId, Long friendId) {
        User user = userRepo.findById(userId).orElseThrow();
        friendRepo.findByUser(user)
                  .stream()
                  .filter(f -> f.getFriend().getUserId().equals(friendId))
                  .forEach(friendRepo::delete);
    }
    public List<User> getAvailableUsers(Long userId) {
        // Get all users
        List<User> allUsers = userRepo.findAll();

        // Get current friends
        List<User> friends = getFriendsOfUser(userId);

        // Remove self and existing friends
        allUsers.removeIf(user -> user.getId().equals(userId) || friends.contains(user));

        return allUsers;
    }

    public List<User> searchUsers(String query) {
        // Search users by name, username, or email
        return userRepo.findAll().stream()
            .filter(user -> 
                (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) ||
                (user.getUsername() != null && user.getUsername().toLowerCase().contains(query.toLowerCase())) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()))
            )
            .toList();
    }

}
