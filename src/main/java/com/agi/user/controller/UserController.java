package com.agi.user.controller;

import com.agi.common.response.ApiResponse;
import com.agi.user.service.UserService;
import com.agi.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserVO>>> getAllUsers() {
        log.info("GET /api/users - Getting all users");
        
        List<UserVO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserVO>> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Getting user by ID", id);
        
        UserVO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserVO>> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/users/username/{} - Getting user by username", username);
        
        UserVO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    /**
     * Get active users
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserVO>>> getActiveUsers() {
        log.info("GET /api/users/active - Getting active users");
        
        List<UserVO> users = userService.getActiveUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Active users retrieved successfully"));
    }
    
    /**
     * Create new user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserVO>> createUser(@RequestBody UserVO user) {
        log.info("POST /api/users - Creating new user: {}", user.getUsername());
        
        UserVO createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserVO>> updateUser(
            @PathVariable Long id, 
            @RequestBody UserVO user) {
        log.info("PUT /api/users/{} - Updating user", id);
        
        UserVO updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
    
    /**
     * Check username availability
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@PathVariable String username) {
        log.info("GET /api/users/check-username/{} - Checking username availability", username);
        
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(ApiResponse.success(available, 
                available ? "Username is available" : "Username is not available"));
    }
    
    /**
     * Check email availability
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@PathVariable String email) {
        log.info("GET /api/users/check-email/{} - Checking email availability", email);
        
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(available, 
                available ? "Email is available" : "Email is not available"));
    }
    
    /**
     * Get user count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        log.info("GET /api/users/count - Getting user count");
        
        long count = userService.getTotalUserCount();
        return ResponseEntity.ok(ApiResponse.success(count, "User count retrieved successfully"));
    }
}

