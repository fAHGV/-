package com.infernovisuals.launcher;

import com.infernovisuals.launcher.auth.AuthService;
import com.infernovisuals.launcher.auth.AuthSession;
import com.infernovisuals.launcher.auth.MicrosoftAuthService;
import com.infernovisuals.launcher.auth.OfflineAuthService;
import com.infernovisuals.launcher.model.LauncherProfile;
import com.infernovisuals.launcher.service.GameLaunchService;
import com.infernovisuals.launcher.service.ProfileService;
import com.infernovisuals.launcher.service.ProfileValidator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class LauncherMain {
    public static void main(String[] args) {
        Path launcherDir = Path.of("launcher-data");
        ProfileService profileService = new ProfileService(launcherDir);
        GameLaunchService gameLaunch = new GameLaunchService();
        ProfileValidator validator = new ProfileValidator();

        if (hasArg(args, "--help")) {
            printHelp(launcherDir);
            return;
        }

        List<LauncherProfile> allProfiles = profileService.loadProfiles();
        if (allProfiles.isEmpty()) {
            System.out.println("profiles.json пустой. Создаю дефолтный профиль...");
            LauncherProfile defaults = new LauncherProfile();
            profileService.saveProfiles(List.of(defaults));
            allProfiles = List.of(defaults);
        }

        if (hasArg(args, "--list")) {
            printProfiles(allProfiles);
            return;
        }

        LauncherProfile profile = resolveProfile(args, allProfiles);

        System.out.println("=== InfernoVisuals Launcher (Simple) ===");
        System.out.println("Профиль: " + profile.name + " | auth=" + profile.authMode);

        List<String> validationErrors = validator.validate(profile);
        if (!validationErrors.isEmpty()) {
            System.out.println("Профиль заполнен не полностью. Исправь launcher-data/profiles.json:");
            for (String error : validationErrors) {
                System.out.println(" - " + error);
            }
            System.exit(2);
            return;
        }

        if (hasArg(args, "--no-launch")) {
            System.out.println("Проверка профиля успешна (--no-launch).");
            return;
        }

        AuthSession session = createAuthService(profile).login();
        if (!session.online()) {
            System.out.println("OFFLINE mode: доступен локально/на offline-серверах");
        }

        System.out.println("Сессия: " + session.playerName() + " (" + session.uuid() + ")");
        System.out.println("Запуск Minecraft...");
        gameLaunch.launch(profile, session);
    }

    private static LauncherProfile resolveProfile(String[] args, List<LauncherProfile> profiles) {
        String profileName = valueAfter(args, "--profile");
        if (profileName == null || profileName.isBlank()) {
            return profiles.getFirst();
        }

        return profiles.stream()
                .filter(p -> p.name != null && p.name.equalsIgnoreCase(profileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден: " + profileName));
    }

    private static void printProfiles(List<LauncherProfile> profiles) {
        System.out.println("Доступные профили:");
        for (LauncherProfile profile : profiles) {
            System.out.println(" - " + profile.name + " (auth=" + profile.authMode + ")");
        }
        System.out.println("Использование: --profile <NAME>");
    }

    private static boolean hasArg(String[] args, String key) {
        return Arrays.stream(args).anyMatch(key::equalsIgnoreCase);
    }

    private static String valueAfter(String[] args, String key) {
        for (int i = 0; i < args.length - 1; i++) {
            if (key.equalsIgnoreCase(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }

    private static void printHelp(Path launcherDir) {
        System.out.println("InfernoVisuals Launcher");
        System.out.println("Папка данных: " + launcherDir.toAbsolutePath());
        System.out.println("Опции:");
        System.out.println("  --help            показать помощь");
        System.out.println("  --list            показать профили");
        System.out.println("  --profile <name>  выбрать профиль");
        System.out.println("  --no-launch       только проверить профиль, не запускать игру");
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
