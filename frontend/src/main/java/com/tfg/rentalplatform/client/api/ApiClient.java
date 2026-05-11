package com.tfg.rentalplatform.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final String baseUrl;
    private String token;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Map<String, Object> post(String path, Map<String, Object> body) throws IOException, InterruptedException {
        return sendForMap("POST", path, body);
    }

    public Map<String, Object> put(String path, Map<String, Object> body) throws IOException, InterruptedException {
        return sendForMap("PUT", path, body);
    }

    public void delete(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException(extractError(response.body()));
        }
    }

    public Map<String, Object> getMap(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseMap(response);
    }

    public List<Map<String, Object>> getList(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException(extractError(response.body()));
        }
        return mapper.readValue(response.body(), new TypeReference<>() {});
    }

    public String withQuery(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }
        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                joiner.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(v, StandardCharsets.UTF_8));
            }
        });
        String query = joiner.toString();
        if (query.isBlank()) {
            return path;
        }
        return path + "?" + query;
    }

    private Map<String, Object> sendForMap(String method, String path, Map<String, Object> body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = baseRequest(path)
                .header("Content-Type", "application/json");
        String json = mapper.writeValueAsString(body == null ? Map.of() : body);
        if ("POST".equals(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(json));
        } else {
            builder.PUT(HttpRequest.BodyPublishers.ofString(json));
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return parseMap(response);
    }

    private HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + path));
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private Map<String, Object> parseMap(HttpResponse<String> response) throws IOException {
        if (response.body() == null || response.body().isBlank()) {
            if (response.statusCode() >= 400) {
                throw new IOException("Error HTTP " + response.statusCode());
            }
            return Map.of();
        }
        Map<String, Object> map = mapper.readValue(response.body(), new TypeReference<>() {});
        if (response.statusCode() >= 400) {
            throw new IOException(String.valueOf(map.getOrDefault("message", "Error HTTP " + response.statusCode())));
        }
        return map;
    }

    private String extractError(String body) {
        try {
            Map<String, Object> map = mapper.readValue(body, new TypeReference<>() {});
            return String.valueOf(map.getOrDefault("message", body));
        } catch (Exception e) {
            return body;
        }
    }
}
