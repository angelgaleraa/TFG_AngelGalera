package com.tfg.rentalplatform.client.realtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RealtimeChatClient {

    private final Supplier<String> tokenSupplier;
    private final Consumer<Map<String, Object>> eventHandler;
    private final Consumer<String> logger;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private WebSocket socket;

    public RealtimeChatClient(Supplier<String> tokenSupplier,
                              Consumer<Map<String, Object>> eventHandler,
                              Consumer<String> logger) {
        this.tokenSupplier = tokenSupplier;
        this.eventHandler = eventHandler;
        this.logger = logger;
    }

    public void connect() {
        String token = tokenSupplier.get();
        if (token == null || token.isBlank()) {
            return;
        }

        disconnect();
        // El token autentica la conexion WebSocket igual que en las peticiones REST.
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8081/ws/chat?token=" + encodedToken), new WebSocket.Listener() {
                    private final StringBuilder buffer = new StringBuilder();

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (last) {
                            // Los mensajes pueden llegar troceados; solo se procesan cuando llega el ultimo fragmento.
                            String payload = buffer.toString();
                            buffer.setLength(0);
                            Platform.runLater(() -> handlePayload(payload));
                        }
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        Platform.runLater(() -> logger.accept("Chat en tiempo real desconectado: " + error.getMessage()));
                    }
                })
                .thenAccept(openSocket -> {
                    socket = openSocket;
                    socket.request(1);
                    Platform.runLater(() -> logger.accept("Chat en tiempo real conectado"));
                })
                .exceptionally(error -> {
                    Platform.runLater(() -> logger.accept("No se pudo conectar el chat en tiempo real: " + error.getMessage()));
                    return null;
                });
    }

    public void disconnect() {
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "logout");
            socket = null;
        }
    }

    private void handlePayload(String payload) {
        try {
            Map<String, Object> event = mapper.readValue(payload, new TypeReference<>() {});
            eventHandler.accept(event);
        } catch (Exception ex) {
            logger.accept("Error en mensaje realtime: " + ex.getMessage());
        }
    }
}
