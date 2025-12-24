package com.library.smart_library.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.library.smart_library.controller.AuthController.RegisterRequest;
import com.library.smart_library.controller.AuthController.ResetPasswordRequest;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.UserRepository;
import com.library.smart_library.security.JwtUtil;
import com.library.smart_library.service.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerCreatesUserWithVerificationTokenOnce() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("secret");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = authController.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getVerificationToken()).isNotBlank();
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.getPassword()).isEqualTo("encoded");
        verify(emailService).sendVerificationEmail(eq(saved.getEmail()), eq(saved.getName()), anyString());
    }

    @Test
    void verifyEmailSucceedsWhenTokenValid() {
        User user = new User();
        user.setVerificationToken("token-123");
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        user.setVerified(false);

        when(userRepository.findByVerificationToken("token-123")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = authController.verifyEmail("token-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
        assertThat(user.getVerificationTokenExpiry()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordSucceedsWhenTokenValid() {
        User user = new User();
        user.setResetToken("reset-abc");
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("reset-abc");
        req.setNewPassword("newPass");

        when(userRepository.findByResetToken("reset-abc")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("enc-new");

        ResponseEntity<String> response = authController.resetPassword(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getPassword()).isEqualTo("enc-new");
        assertThat(user.getResetToken()).isNull();
        assertThat(user.getResetTokenExpiry()).isNull();
        verify(userRepository).save(user);
    }
}
