package com.infernovisuals.launcher.model;

import java.util.ArrayList;
import java.util.List;

public final class LauncherProfile {
    public String name = "Inferno Stable";
    public String gameDir = ".minecraft";
    public String javaPath = "java";

    public String minecraftVersion = "1.21.4";
    public String versionName = "fabric-loader-1.21.4";
    public String loader = "fabric";
    public String mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient";
    public String classpath = "libraries/*;versions/fabric-loader-1.21.4/fabric-loader-1.21.4.jar";

    public String assetsDir = "assets";
    public String assetIndex = "1.21";

    public String authMode = "offline"; // offline | microsoft
    public String offlineNickname = "InfernoPlayer";

    public String manifestUrl = "https://example.com/infernovisuals/manifest.json";

    public int minRamMb = 2048;
    public int maxRamMb = 4096;

    public int width = 1280;
    public int height = 720;
    public boolean fullscreen = false;

    public String serverAddress = "";
    public int serverPort = 25565;

    public List<String> jvmArgs = new ArrayList<>();
}
