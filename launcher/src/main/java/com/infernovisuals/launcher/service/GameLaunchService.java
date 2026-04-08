package com.infernovisuals.launcher.service;

import com.infernovisuals.launcher.auth.AuthSession;
import com.infernovisuals.launcher.model.LauncherProfile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GameLaunchService {
    public Process launch(LauncherProfile profile, AuthSession session) {
        List<String> cmd = new ArrayList<>();
        cmd.add(profile.javaPath);
        cmd.add("-Xms" + profile.minRamMb + "M");
        cmd.add("-Xmx" + profile.maxRamMb + "M");
        cmd.addAll(profile.jvmArgs);

        cmd.add("-cp");
        cmd.add(profile.classpath);
        cmd.add(profile.mainClass);

        cmd.add("--username");
        cmd.add(session.playerName());
        cmd.add("--version");
        cmd.add(profile.versionName);
        cmd.add("--gameDir");
        cmd.add(profile.gameDir);
        cmd.add("--assetsDir");
        cmd.add(profile.assetsDir);
        cmd.add("--assetIndex");
        cmd.add(profile.assetIndex);
        cmd.add("--uuid");
        cmd.add(session.uuid());
        cmd.add("--accessToken");
        cmd.add(session.accessToken());
        cmd.add("--userType");
        cmd.add(session.userType());
        cmd.add("--versionType");
        cmd.add("InfernoVisuals");

        if (profile.fullscreen) {
            cmd.add("--fullscreen");
        } else {
            cmd.add("--width");
            cmd.add(String.valueOf(profile.width));
            cmd.add("--height");
            cmd.add(String.valueOf(profile.height));
        }

        if (profile.serverAddress != null && !profile.serverAddress.isBlank()) {
            cmd.add("--server");
            cmd.add(profile.serverAddress);
            cmd.add("--port");
            cmd.add(String.valueOf(profile.serverPort));
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(Path.of(profile.gameDir).toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            Thread logThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[MC] " + line);
                    }
                } catch (Exception ignored) {
                }
            }, "mc-log-reader");
            logThread.setDaemon(true);
            logThread.start();

            return process;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось запустить Minecraft. Проверь javaPath/classpath/mainClass.", e);
        }
    }
}
