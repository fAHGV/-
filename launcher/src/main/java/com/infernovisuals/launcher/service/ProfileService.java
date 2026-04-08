package com.infernovisuals.launcher.service;

import com.google.gson.reflect.TypeToken;
import com.infernovisuals.launcher.model.LauncherProfile;
import com.infernovisuals.launcher.util.Jsons;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ProfileService {
    private static final Type PROFILE_LIST = new TypeToken<List<LauncherProfile>>() {}.getType();
    private final Path profileFile;

    public ProfileService(Path rootDir) {
        this.profileFile = rootDir.resolve("profiles.json");
    }

    public List<LauncherProfile> loadProfiles() {
        if (!Files.exists(profileFile)) {
            List<LauncherProfile> defaults = new ArrayList<>();
            defaults.add(new LauncherProfile());
            saveProfiles(defaults);
            return defaults;
        }

        try {
            String json = Files.readString(profileFile);
            List<LauncherProfile> profiles = Jsons.GSON.fromJson(json, PROFILE_LIST);
            if (profiles == null || profiles.isEmpty()) {
                profiles = new ArrayList<>();
                profiles.add(new LauncherProfile());
            }
            return profiles;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать profiles.json", e);
        }
    }

    public void saveProfiles(List<LauncherProfile> profiles) {
        try {
            Files.createDirectories(profileFile.getParent());
            Files.writeString(profileFile, Jsons.GSON.toJson(profiles));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить profiles.json", e);
        }
    }
}
