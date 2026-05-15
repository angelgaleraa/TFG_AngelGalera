package com.tfg.rentalplatform.client.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiClientIntegrationTest {

    private HttpServer server;
    private ApiClient apiClient;
    private String lastAuthorizationHeader;
    private String lastRequestBody;
    private String lastRequestUri;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/auth/login", this::handleLogin);
        server.createContext("/api/items", this::handleItems);
        server.createContext("/api/profile", this::handleProfile);
        server.createContext("/api/error", this::handleError);
        server.start();
        apiClient = new ApiClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postSendsJsonAndParsesLoginResponse() throws Exception {
        Map<String, Object> response = apiClient.post("/api/auth/login", Map.of(
                "email", "ana@test.com",
                "password", "1234"
        ));

        assertEquals("token-test", response.get("token"));
        assertTrue(lastRequestBody.contains("\"email\":\"ana@test.com\""));
        assertTrue(lastRequestBody.contains("\"password\":\"1234\""));
    }

    @Test
    void getListSendsBearerTokenAndParsesItems() throws Exception {
        apiClient.setToken("token-test");

        List<Map<String, Object>> items = apiClient.getList(apiClient.withQuery("/api/items", Map.of(
                "city", "Valencia",
                "query", "camara"
        )));

        assertEquals("Bearer token-test", lastAuthorizationHeader);
        assertTrue(lastRequestUri.startsWith("/api/items?"));
        assertTrue(lastRequestUri.contains("city=Valencia"));
        assertTrue(lastRequestUri.contains("query=camara"));
        assertEquals(1, items.size());
        assertEquals("Camara GoPro", items.getFirst().get("title"));
        assertEquals(15.0, items.getFirst().get("pricePerDay"));
    }

    @Test
    void putSendsBearerTokenAndParsesProfileResponse() throws Exception {
        apiClient.setToken("token-test");

        Map<String, Object> response = apiClient.put("/api/profile", Map.of(
                "name", "Ana",
                "phone", "600000000"
        ));

        assertEquals("Bearer token-test", lastAuthorizationHeader);
        assertEquals("Ana", response.get("name"));
        assertTrue(lastRequestBody.contains("\"phone\":\"600000000\""));
    }

    @Test
    void errorResponsesExposeBackendMessage() {
        IOException exception = assertThrows(IOException.class, () -> apiClient.getMap("/api/error"));

        assertEquals("Tu cuenta ha sido bloqueada", exception.getMessage());
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        lastRequestBody = readBody(exchange);
        writeJson(exchange, 200, """
                {"token":"token-test","user":{"id":1,"name":"Ana","email":"ana@test.com","role":"USER"}}
                """);
    }

    private void handleItems(HttpExchange exchange) throws IOException {
        lastAuthorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");
        lastRequestUri = exchange.getRequestURI().toString();
        writeJson(exchange, 200, """
                [{"id":3,"title":"Camara GoPro","pricePerDay":15.0}]
                """);
    }

    private void handleProfile(HttpExchange exchange) throws IOException {
        lastAuthorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");
        lastRequestBody = readBody(exchange);
        writeJson(exchange, 200, """
                {"id":1,"name":"Ana","phone":"600000000"}
                """);
    }

    private void handleError(HttpExchange exchange) throws IOException {
        writeJson(exchange, 401, """
                {"code":"AUTH_ACCOUNT_BLOCKED","message":"Tu cuenta ha sido bloqueada"}
                """);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.strip().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
