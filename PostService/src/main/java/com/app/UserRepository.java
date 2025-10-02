package com.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository already provides findById(Long id) method that returns Optional<User>
    // You can add custom query methods here if needed
    Optional<User> findByEmail(String email);
}