package com.infernovisuals.launcher.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public final class IntegrityVerifier {
    public boolean isValid(Path file, String expectedSha256) {
        if (!Files.exists(file)) {
            return false;
        }
        return sha256(file).equalsIgnoreCase(expectedSha256);
    }

    public String sha256(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error: " + file, e);
        }
    }

    public void verifyOrThrow(Path file, String expectedSha256) {
        if (!isValid(file, expectedSha256)) {
            throw new RuntimeException("Файл поврежден: " + file.getFileName());
        }
    }
}
