package com.infernovisuals.launcher.service;

import com.infernovisuals.launcher.model.LauncherManifest;
import com.infernovisuals.launcher.util.Jsons;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ManifestService {
    private final HttpClient http = HttpClient.newHttpClient();

    public LauncherManifest fetch(String manifestUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(manifestUrl)).GET().build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Manifest HTTP error: " + response.statusCode());
            }
            LauncherManifest manifest = Jsons.GSON.fromJson(response.body(), LauncherManifest.class);
            if (manifest == null) {
                throw new RuntimeException("Manifest пустой или некорректный");
            }
            return manifest;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Не удалось загрузить manifest", e);
        }
    }
}
