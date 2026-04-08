package com.infernovisuals.launcher;

import com.infernovisuals.launcher.auth.AuthService;
import com.infernovisuals.launcher.auth.AuthSession;
import com.infernovisuals.launcher.auth.MicrosoftAuthService;
import com.infernovisuals.launcher.auth.OfflineAuthService;
import com.infernovisuals.launcher.model.LauncherProfile;
import com.infernovisuals.launcher.service.GameLaunchService;
import com.infernovisuals.launcher.service.ProfileService;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class LauncherMain {
    public static void main(String[] args) {
        Path launcherDir = Path.of("launcher-data");
        ProfileService profiles = new ProfileService(launcherDir);
        GameLaunchService gameLaunch = new GameLaunchService();

        List<LauncherProfile> allProfiles = profiles.loadProfiles();
        LauncherProfile profile = allProfiles.getFirst();

        System.out.println("=== InfernoVisuals Launcher (Simple) ===");
        System.out.println("Профиль: " + profile.name + " | auth=" + profile.authMode);

        AuthSession session = createAuthService(profile).login();
        if (!session.online()) {
            System.out.println("OFFLINE mode: доступен локально/на offline-серверах");
        }

        System.out.println("Сессия: " + session.playerName() + " (" + session.uuid() + ")");
        System.out.println("Запуск Minecraft...");
        gameLaunch.launch(profile, session);
    }

    private static AuthService createAuthService(LauncherProfile profile) {
        String mode = profile.authMode == null ? "offline" : profile.authMode.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "microsoft" -> new MicrosoftAuthService();
            case "offline" -> new OfflineAuthService(profile.offlineNickname);
            default -> throw new IllegalArgumentException("Неизвестный auth mode: " + profile.authMode);
        };
    }
}
