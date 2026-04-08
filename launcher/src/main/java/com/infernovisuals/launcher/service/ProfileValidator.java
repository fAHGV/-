package com.infernovisuals.launcher.service;

import com.infernovisuals.launcher.model.LauncherProfile;

import java.util.ArrayList;
import java.util.List;

public final class ProfileValidator {
    public List<String> validate(LauncherProfile profile) {
        List<String> errors = new ArrayList<>();

        if (isBlank(profile.name)) {
            errors.add("profile.name пустой");
        }
        if (isBlank(profile.javaPath)) {
            errors.add("profile.javaPath пустой");
        }
        if (isBlank(profile.classpath)) {
            errors.add("profile.classpath пустой");
        }
        if (isBlank(profile.mainClass)) {
            errors.add("profile.mainClass пустой");
        }
        if (isBlank(profile.gameDir)) {
            errors.add("profile.gameDir пустой");
        }
        if (isBlank(profile.assetsDir)) {
            errors.add("profile.assetsDir пустой");
        }
        if (isBlank(profile.versionName)) {
            errors.add("profile.versionName пустой");
        }
        if (profile.minRamMb <= 0) {
            errors.add("profile.minRamMb должен быть > 0");
        }
        if (profile.maxRamMb < profile.minRamMb) {
            errors.add("profile.maxRamMb должен быть >= minRamMb");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
