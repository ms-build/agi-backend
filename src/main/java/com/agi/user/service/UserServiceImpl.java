package com.agi.user.service;

import com.agi.common.exception.ResourceNotFoundException;
import com.agi.user.repository.UserRepository;
import com.agi.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public List<UserVO> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll();
    }
    
    @Override
    public UserVO getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    @Override
    public UserVO getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    @Override
    public UserVO getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    @Override
    public List<UserVO> getActiveUsers() {
        log.debug("Getting active users");
        return userRepository.findByIsActive(true);
    }
    
    @Override
    @Transactional
    public UserVO createUser(UserVO user) {
        log.debug("Creating new user: {}", user.getUsername());
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Set default values
        user.setCreatedAt(LocalDateTime.now());
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        int result = userRepository.insert(user);
        if (result > 0) {
            log.info("User created successfully: {}", user.getUsername());
            return user;
        } else {
            throw new RuntimeException("Failed to create user");
        }
    }
    
    @Override
    @Transactional
    public UserVO updateUser(Long id, UserVO user) {
        log.debug("Updating user: {}", id);
        
        // Check if user exists
        UserVO existingUser = getUserById(id);
        
        // Validate username uniqueness (if changed)
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Validate email uniqueness (if changed)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Set ID and preserve creation time
        user.setId(id);
        user.setCreatedAt(existingUser.getCreatedAt());
        
        int result = userRepository.update(user);
        if (result > 0) {
            log.info("User updated successfully: {}", id);
            return user;
        } else {
            throw new RuntimeException("Failed to update user");
        }
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user: {}", id);
        
        // Check if user exists
        getUserById(id);
        
        int result = userRepository.deleteById(id);
        if (result > 0) {
            log.info("User deleted successfully: {}", id);
        } else {
            throw new RuntimeException("Failed to delete user");
        }
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        log.debug("Checking username availability: {}", username);
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        log.debug("Checking email availability: {}", email);
        return !userRepository.existsByEmail(email);
    }
    
    @Override
    public long getTotalUserCount() {
        log.debug("Getting total user count");
        return userRepository.count();
    }
}

