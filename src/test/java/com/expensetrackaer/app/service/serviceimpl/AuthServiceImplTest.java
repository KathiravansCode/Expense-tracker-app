package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.*;
import com.expensetrackaer.app.entity.model.PasswordResetToken;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.PasswordResetTokenRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.security.JwtUtil;
import com.expensetrackaer.app.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService; // Added mock component dependency

    @InjectMocks private AuthServiceImpl authService;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<PasswordResetToken> resetTokenCaptor;

    @Test
    void registerUser_whenEmailExists_throws() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .name("A")
                .email("a@example.com")
                .password("secret123")
                .build();

        when(userRepository.existsByEmail("a@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_success_encodesPassword_andMapsEmail() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .name("A")
                .email("a@example.com")
                .password("secret123")
                .build();

        when(userRepository.existsByEmail("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("ENC(secret123)");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponse response = authService.registerUser(request);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("ENC(secret123)");
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void login_whenBadCredentials_throws() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a@example.com");
        request.setPassword("bad");

        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_success_returnsJwtToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a@example.com");
        request.setPassword("secret123");

        User user = User.builder().id(99L).name("A").email("a@example.com").password("x").build();

        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("a@example.com", 99L)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("a@example.com");
        assertThat(response.getName()).isEqualTo("A");
    }

    @Test
    void forgotPassword_whenUserNotFound_throws() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No account found");
    }

    @Test
    void forgotPassword_success_deletesExisting_sendsMail_andReturnsOmittedResponse() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("kathir@example.com");

        User user = User.builder().id(7L).name("Kathiravan").email("kathir@example.com").password("x").build();
        when(userRepository.findByEmail("kathir@example.com")).thenReturn(Optional.of(user));
        when(resetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = LocalDateTime.now();
        ApiResponse response = authService.forgotPassword(request);
        LocalDateTime after = LocalDateTime.now();

        verify(resetTokenRepository).deleteByUserId(7L);
        verify(resetTokenRepository).save(resetTokenCaptor.capture());

        PasswordResetToken saved = resetTokenCaptor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(7L);
        assertThat(saved.getIsUsed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(before);
        assertThat(saved.getExpiresAt()).isBefore(after.plusMinutes(16));

        // Verifying token payload was passed into the service layer safely
        verify(emailService).sendPasswordResetEmail(eq("kathir@example.com"), anyString(), eq("Kathiravan"));

        // Verifying payload privacy updates
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }

    @Test
    void resetPassword_invalidToken_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad");
        request.setNewPassword("newpass1");

        when(resetTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void resetPassword_alreadyUsed_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("t");
        request.setNewPassword("newpass1");

        PasswordResetToken token = PasswordResetToken.builder()
                .token("t")
                .isUsed(true)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .user(User.builder().id(1L).build())
                .build();

        when(resetTokenRepository.findByToken("t")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void resetPassword_expired_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("t");
        request.setNewPassword("newpass1");

        PasswordResetToken token = PasswordResetToken.builder()
                .token("t")
                .isUsed(false)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .user(User.builder().id(1L).build())
                .build();

        when(resetTokenRepository.findByToken("t")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_success_updatesPassword_andMarksTokenUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("t");
        request.setNewPassword("newpass1");

        User user = User.builder().id(1L).email("a@example.com").password("OLD").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .token("t")
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();

        when(resetTokenRepository.findByToken("t")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass1")).thenReturn("ENC(newpass1)");

        ApiResponse response = authService.resetPassword(request);

        assertThat(response.getSuccess()).isTrue();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("ENC(newpass1)");

        verify(resetTokenRepository).save(resetTokenCaptor.capture());
        assertThat(resetTokenCaptor.getValue().getIsUsed()).isTrue();
    }
}