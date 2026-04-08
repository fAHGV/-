package com.infernovisuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path root;

    public ConfigManager(Path gameDir) {
        this.root = gameDir.resolve("config").resolve("infernovisuals");
    }

    public InfernoConfig load(String name) {
        Path path = configPath(name);
        if (!Files.exists(path)) {
            return new InfernoConfig();
        }

        try {
            String json = Files.readString(path);
            InfernoConfig config = GSON.fromJson(json, InfernoConfig.class);
            return config == null ? new InfernoConfig() : config;
        } catch (IOException ex) {
            return new InfernoConfig();
        }
    }

    public boolean save(String name, InfernoConfig config) {
        try {
            Files.createDirectories(root);
            Files.writeString(configPath(name), GSON.toJson(config));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean createIfMissing(String name, InfernoConfig config) {
        Path path = configPath(name);
        if (Files.exists(path)) {
            return false;
        }
        return save(name, config);
    }

    private Path configPath(String name) {
        return root.resolve(name + ".json");
    }
}
