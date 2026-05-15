package com.tfg.rentalplatform.client.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BackendLauncher {

    private static final String HEALTH_URL = "http://localhost:8081/api/items?page=0&size=1";
    private static final String BACKEND_SCRIPT = "run-backend.ps1";

    private BackendLauncher() {
    }

    public static void ensureAvailable() {
        // JavaFX depende de la API REST; si ya responde, no se arranca otro backend.
        if (backendResponds()) {
            return;
        }
        Path backendScript = findProjectFile(BACKEND_SCRIPT);
        if (backendScript == null) {
            System.out.println("No se encontro run-backend.ps1; abre el backend manualmente antes de iniciar sesion.");
            return;
        }
        try {
            Path projectDir = backendScript.getParent();
            Path outLog = projectDir.resolve("backend-run.out.log");
            Path errLog = projectDir.resolve("backend-run.err.log");
            // El script se ejecuta en una consola aparte y carga la configuracion cloud de Aiven.
            new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    backendScript.toString(),
                    "cloud"
            )
                    .directory(projectDir.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(outLog.toFile()))
                    .redirectError(ProcessBuilder.Redirect.appendTo(errLog.toFile()))
                    .start();
            // Se espera a que Spring Boot termine de levantar Tomcat antes de continuar con la app.
            for (int i = 0; i < 150; i++) {
                Thread.sleep(1000);
                if (backendResponds()) {
                    return;
                }
            }
            System.out.println("El backend no respondio a tiempo. Revisa backend-run.err.log.");
        } catch (Exception ex) {
            System.out.println("No se pudo arrancar el backend automaticamente: " + ex.getMessage());
        }
    }

    private static boolean backendResponds() {
        try {
            // /api/items puede devolver 401 si no hay sesion; lo importante es que no sea un fallo 5xx.
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(2))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HEALTH_URL))
                    .timeout(java.time.Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 500;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Path findProjectFile(String fileName) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 5 && current != null; i++) {
            Path candidate = current.resolve(fileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }
}
