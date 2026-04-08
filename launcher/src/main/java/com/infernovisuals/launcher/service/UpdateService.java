package com.infernovisuals.launcher.service;

import com.infernovisuals.launcher.model.LauncherManifest;
import com.infernovisuals.launcher.model.UpdatePlan;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class UpdateService {
    private final IntegrityVerifier verifier = new IntegrityVerifier();
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    public UpdatePlan createPlan(Path gameDir, LauncherManifest manifest) {
        UpdatePlan plan = new UpdatePlan();
        for (LauncherManifest.ManifestFile file : manifest.files) {
            Path local = gameDir.resolve(file.path);
            if (!verifier.isValid(local, file.sha256)) {
                plan.toDownload.add(file);
            }
        }
        return plan;
    }

    public void applyPlan(Path gameDir, UpdatePlan plan) {
        if (plan.isEmpty()) {
            return;
        }

        Path tempRoot = gameDir.resolve("updates").resolve("tmp");
        try {
            Files.createDirectories(tempRoot);
            for (LauncherManifest.ManifestFile file : plan.toDownload) {
                Path temp = tempRoot.resolve(file.path.replace('/', '_'));
                download(file.url, temp);
                verifier.verifyOrThrow(temp, file.sha256);

                Path target = gameDir.resolve(file.path);
                Files.createDirectories(target.getParent());
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка применения обновления", e);
        }
    }

    private void download(String url, Path target) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            HttpResponse<Path> response = http.send(request, HttpResponse.BodyHandlers.ofFile(target));
            if (response.statusCode() != 200) {
                throw new RuntimeException("Download HTTP error: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Не удалось скачать файл: " + url, e);
        }
    }
}
