package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.ChangePasswordRequest;
import com.expensetrackaer.app.entity.dto.UpdateProfileRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @Captor private ArgumentCaptor<User> userCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void getProfile_userNotFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_whenEmailChangedAndAlreadyTaken_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User existing = User.builder().id(5L).name("A").email("a@example.com").password("x").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("A2");
        request.setEmail("taken@example.com");

        assertThatThrownBy(() -> userService.updateProfile(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already associated");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_whenEmailUnchanged_skipsDuplicateCheck() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User existing = User.builder().id(5L).name("A").email("a@example.com").password("x").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("A2");
        request.setEmail("A@EXAMPLE.COM");

        UserResponse response = userService.updateProfile(request);

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("A2");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("A@EXAMPLE.COM");
        assertThat(response.getEmail()).isEqualTo("A@EXAMPLE.COM");
    }

    @Test
    void changePassword_whenCurrentDoesNotMatch_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User user = User.builder().id(5L).email("a@example.com").password("HASH").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("newpass1");

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void changePassword_whenNewAndConfirmMismatch_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User user = User.builder().id(5L).email("a@example.com").password("HASH").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("curr", "HASH")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("curr");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("different");

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void changePassword_whenNewSameAsCurrent_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User user = User.builder().id(5L).email("a@example.com").password("HASH").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("curr", "HASH")).thenReturn(true);
        when(passwordEncoder.matches("newpass1", "HASH")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("curr");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("newpass1");

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    void changePassword_success_encodesAndSaves() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User user = User.builder().id(5L).email("a@example.com").password("HASH").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("curr", "HASH")).thenReturn(true);
        when(passwordEncoder.matches("newpass1", "HASH")).thenReturn(false);
        when(passwordEncoder.encode("newpass1")).thenReturn("NEW_HASH");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("curr");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("newpass1");

        userService.changePassword(request);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("NEW_HASH");
    }

    @Test
    void deleteAccount_userNotFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAccount_success_deletesUser() {
        SecurityTestUtils.setAuthenticatedUser(5L, "a@example.com");
        User user = User.builder().id(5L).email("a@example.com").password("x").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        userService.deleteAccount();

        verify(userRepository).delete(user);
    }
}

