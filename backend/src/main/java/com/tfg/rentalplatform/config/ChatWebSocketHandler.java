package com.tfg.rentalplatform.config;

import com.tfg.rentalplatform.entity.User;
import com.tfg.rentalplatform.repository.UserRepository;
import com.tfg.rentalplatform.security.JwtService;
import com.tfg.rentalplatform.service.ChatRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatRealtimeService chatRealtimeService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // El cliente envia el JWT en la URL del WebSocket porque no puede usar cabeceras facilmente.
        String token = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("token");
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing token"));
            return;
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElse(null);
        // Si el token no corresponde a un usuario valido, se rechaza la conexion realtime.
        if (user == null || !jwtService.isTokenValid(token, user.getEmail())) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }
        session.getAttributes().put("userId", user.getId());
        chatRealtimeService.register(user.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long id) {
            chatRealtimeService.unregister(id, session);
        }
    }
}
