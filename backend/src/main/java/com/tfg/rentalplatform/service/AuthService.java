package com.tfg.rentalplatform.service;

import com.tfg.rentalplatform.dto.AuthDtos;
import com.tfg.rentalplatform.dto.ChatDtos;
import com.tfg.rentalplatform.entity.ChatMessage;
import com.tfg.rentalplatform.entity.Conversation;
import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.entity.UserRole;
import com.tfg.rentalplatform.exception.ApiException;
import com.tfg.rentalplatform.repository.ChatMessageRepository;
import com.tfg.rentalplatform.repository.ConversationRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.JwtService;
import com.tfg.rentalplatform.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRealtimeService chatRealtimeService;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "AUTH_EMAIL_IN_USE", "El email ya esta registrado");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setActive(true);
        User saved = userRepository.save(user);
        JwtUserPrincipal principal = new JwtUserPrincipal(saved);
        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), AuthDtos.UserResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email)
                .filter(user -> !Boolean.TRUE.equals(user.getActive()))
                .ifPresent(user -> {
                    throw ApiException.unauthorized("AUTH_ACCOUNT_BLOCKED", "Tu cuenta ha sido bloqueada");
                });
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email,
                request.password()
        ));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("AUTH_INVALID_CREDENTIALS", "Credenciales invalidas"));
        JwtUserPrincipal principal = new JwtUserPrincipal(user);
        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), AuthDtos.UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        String email = jwtService.extractUsername(request.token());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("AUTH_INVALID_TOKEN", "Token inválido"));
        JwtUserPrincipal principal = new JwtUserPrincipal(user);
        if (!jwtService.isTokenValid(request.token(), principal)) {
            throw ApiException.unauthorized("AUTH_INVALID_TOKEN", "Token inválido");
        }
        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), AuthDtos.UserResponse.from(user));
    }

    @Transactional
    public AuthDtos.SupportContactResponse contactSupport(AuthDtos.SupportContactRequest request) {
        User user = validateSupportCredentials(request.email(), request.password());
        Conversation conversation = getOrCreateSupportConversation(user);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(user);
        message.setRecipient(conversation.getOwner());
        message.setContent(request.message().trim());
        ChatMessage saved = chatMessageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        chatRealtimeService.sendToUser(conversation.getOwner().getId(), ChatDtos.MessageResponse.from(saved, conversation.getOwner().getId()));
        return new AuthDtos.SupportContactResponse("Mensaje enviado a soporte.");
    }

    @Transactional
    public AuthDtos.SupportLoginResponse supportLogin(AuthDtos.LoginRequest request) {
        User user = validateSupportCredentials(request.email(), request.password());
        Conversation conversation = getOrCreateSupportConversation(user);
        JwtUserPrincipal principal = new JwtUserPrincipal(user);
        return new AuthDtos.SupportLoginResponse(
                jwtService.generateToken(principal),
                conversation.getId(),
                AuthDtos.UserResponse.from(user)
        );
    }

    private User validateSupportCredentials(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("AUTH_INVALID_CREDENTIALS", "Credenciales invalidas"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw ApiException.unauthorized("AUTH_INVALID_CREDENTIALS", "Credenciales invalidas");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw ApiException.unauthorized("AUTH_ACCOUNT_BLOCKED", "Tu cuenta ha sido bloqueada");
        }
        if (user.getRole() == UserRole.ADMIN) {
            throw ApiException.badRequest("SUPPORT_ADMIN_NOT_ALLOWED", "Usa el acceso de administrador para gestionar soporte");
        }
        return user;
    }

    private Conversation getOrCreateSupportConversation(User user) {
        User admin = userRepository.findFirstByRoleAndActiveTrueOrderByIdAsc(UserRole.ADMIN)
                .orElseThrow(() -> ApiException.notFound("ADMIN_NOT_FOUND", "No hay administradores disponibles"));
        return conversationRepository.findByAdminConversationTrueAndOwnerIdAndRenterId(admin.getId(), user.getId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setOwner(admin);
                    created.setRenter(user);
                    created.setAdminConversation(true);
                    created.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(created);
                });
    }
}
