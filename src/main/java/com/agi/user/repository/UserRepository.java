package com.agi.user.repository;

import com.agi.user.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 */
@Mapper
public interface UserRepository {
    
    /**
     * Find all users
     */
    List<UserVO> findAll();
    
    /**
     * Find user by ID
     */
    Optional<UserVO> findById(@Param("id") Long id);
    
    /**
     * Find user by username
     */
    Optional<UserVO> findByUsername(@Param("username") String username);
    
    /**
     * Find user by email
     */
    Optional<UserVO> findByEmail(@Param("email") String email);
    
    /**
     * Find active users
     */
    List<UserVO> findByIsActive(@Param("isActive") Boolean isActive);
    
    /**
     * Insert new user
     */
    int insert(UserVO user);
    
    /**
     * Update user
     */
    int update(UserVO user);
    
    /**
     * Delete user by ID
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Count total users
     */
    long count();
}

