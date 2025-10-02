package com.app;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final Jwtutil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = new Jwtutil();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Enhanced registration with full user details
    public String register(RegisterRequest request) throws Exception {
        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new Exception("Name is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new Exception("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new Exception("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new Exception("Password is required");
        }

        // Check for existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email already registered");
        }

        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new Exception("Username already taken");
        }

        try {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            
            User user = new User();
            user.setName(request.getName().trim());
            user.setUsername(request.getUsername().trim().toLowerCase());
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPassword(encodedPassword);
            user.setBio(request.getBio());
            user.setLocation(request.getLocation());
            user.setWebsite(request.getWebsite());
            user.setRole("USER");
            user.setIsActive(true);
            
            userRepository.save(user);
            return "User registered successfully!";
        } catch (Exception e) {
            throw new Exception("Error registering user: " + e.getMessage(), e);
        }
    }

    // Backward compatibility - simple registration with email and password
    public String register(String email, String password) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already registered");
        }
        try {
            String encodedPassword = passwordEncoder.encode(password);
            User user = new User(email, encodedPassword, "USER");
            userRepository.save(user);
            return "User registered successfully!";
        } catch (Exception e) {
            throw new Exception("Error registering user: " + e.getMessage(), e);
        }
    }

    public String login(String email, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new Exception("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password for email: " + email);
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new Exception("Account is deactivated");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getUserId());
    }

    public String loginByUsername(String username, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new Exception("User not found with username: " + username);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password for username: " + username);
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new Exception("Account is deactivated");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getUserId());
    }

    public User getUserById(Long userId) throws Exception {
        return userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found with ID: " + userId));
    }

    public User getUserByEmail(String email) throws Exception {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new Exception("User not found with email: " + email));
    }

    public User getUserByUsername(String username) throws Exception {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found with username: " + username));
    }
}
