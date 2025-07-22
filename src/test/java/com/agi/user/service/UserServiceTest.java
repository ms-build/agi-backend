package com.agi.user.service;

import com.agi.common.exception.ResourceNotFoundException;
import com.agi.user.repository.UserRepository;
import com.agi.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserVO testUser;

    @BeforeEach
    void setUp() {
        testUser = UserVO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .nickname("Tester")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("모든 사용자 조회 성공")
    void getAllUsers_Success() {
        // Given
        List<UserVO> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<UserVO> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).hasSize(1);
        assertThat(actualUsers.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserVO actualUser = userService.getUserById(1L);

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 사용자 조회 시 예외 발생")
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 성공")
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserVO actualUser = userService.getUserByUsername("testuser");

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("새 사용자 생성 성공")
    void createUser_Success() {
        // Given
        UserVO newUser = UserVO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .nickname("NewUser")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.insert(any(UserVO.class))).thenReturn(1);

        // When
        UserVO createdUser = userService.createUser(newUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("newuser");
        assertThat(createdUser.getCreatedAt()).isNotNull();
        assertThat(createdUser.getIsActive()).isTrue();
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).insert(any(UserVO.class));
    }

    @Test
    @DisplayName("중복된 사용자명으로 사용자 생성 시 예외 발생")
    void createUser_DuplicateUsername() {
        // Given
        UserVO newUser = UserVO.builder()
                .username("testuser")
                .email("new@example.com")
                .password("password123")
                .nickname("NewUser")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists: testuser");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).insert(any(UserVO.class));
    }

    @Test
    @DisplayName("사용자명 사용 가능 여부 확인")
    void isUsernameAvailable_Available() {
        // Given
        when(userRepository.existsByUsername("availableuser")).thenReturn(false);

        // When
        boolean available = userService.isUsernameAvailable("availableuser");

        // Then
        assertThat(available).isTrue();
        verify(userRepository).existsByUsername("availableuser");
    }

    @Test
    @DisplayName("사용자명 사용 불가능 여부 확인")
    void isUsernameAvailable_NotAvailable() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean available = userService.isUsernameAvailable("testuser");

        // Then
        assertThat(available).isFalse();
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("활성 사용자 조회 성공")
    void getActiveUsers_Success() {
        // Given
        List<UserVO> activeUsers = Arrays.asList(testUser);
        when(userRepository.findByIsActive(true)).thenReturn(activeUsers);

        // When
        List<UserVO> actualUsers = userService.getActiveUsers();

        // Then
        assertThat(actualUsers).hasSize(1);
        assertThat(actualUsers.get(0).isActive()).isTrue();
        verify(userRepository).findByIsActive(true);
    }

    @Test
    @DisplayName("전체 사용자 수 조회 성공")
    void getTotalUserCount_Success() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = userService.getTotalUserCount();

        // Then
        assertThat(count).isEqualTo(5L);
        verify(userRepository).count();
    }
}

