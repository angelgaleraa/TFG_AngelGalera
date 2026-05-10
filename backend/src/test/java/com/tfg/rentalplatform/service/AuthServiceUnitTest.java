package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.AuthDtos;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ChatMessageRepository;
import com.tfg.rentalplatform.repository.ConversationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ChatRealtimeService chatRealtimeService;

    @InjectMocks private AuthService authService;

    @Test
    void registerStoresNormalizedActiveUserAndReturnsToken() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthDtos.AuthResponse response = authService.register(new AuthDtos.RegisterRequest(
                " Ana ",
                "Ana@Test.COM",
                "1234"
        ));

        assertEquals("token", response.token());
        assertEquals(10L, response.user().id());
        assertEquals("Ana", response.user().name());
        assertEquals("ana@test.com", response.user().email());
        assertEquals(UserRole.USER, response.user().role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void blockedUserCannotLoginAndReceivesSpecificErrorCode() {
        User blocked = new User();
        blocked.setId(8L);
        blocked.setEmail("marta@test.com");
        blocked.setActive(false);
        when(userRepository.findByEmail("marta@test.com")).thenReturn(Optional.of(blocked));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login(new AuthDtos.LoginRequest(
                " Marta@Test.com ",
                "1234"
        )));

        assertEquals("AUTH_ACCOUNT_BLOCKED", exception.getCode());
        assertTrue(exception.getMessage().contains("bloqueada"));
        verify(authenticationManager, never()).authenticate(any());
    }
}
