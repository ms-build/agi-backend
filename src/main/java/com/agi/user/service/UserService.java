package com.agi.user.service;

import com.agi.user.vo.UserVO;

import java.util.List;

/**
 * User Service Interface
 */
public interface UserService {
    
    /**
     * Get all users
     */
    List<UserVO> getAllUsers();
    
    /**
     * Get user by ID
     */
    UserVO getUserById(Long id);
    
    /**
     * Get user by username
     */
    UserVO getUserByUsername(String username);
    
    /**
     * Get user by email
     */
    UserVO getUserByEmail(String email);
    
    /**
     * Get active users
     */
    List<UserVO> getActiveUsers();
    
    /**
     * Create new user
     */
    UserVO createUser(UserVO user);
    
    /**
     * Update user
     */
    UserVO updateUser(Long id, UserVO user);
    
    /**
     * Delete user
     */
    void deleteUser(Long id);
    
    /**
     * Check if username is available
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Check if email is available
     */
    boolean isEmailAvailable(String email);
    
    /**
     * Get total user count
     */
    long getTotalUserCount();
}

