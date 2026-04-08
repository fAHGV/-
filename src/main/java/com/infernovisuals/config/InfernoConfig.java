package com.infernovisuals.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InfernoConfig {
    public Hud hud = new Hud();
    public Theme theme = Theme.INFERNO;
    public Animations animations = new Animations();
    public Keybinds keybinds = new Keybinds();
    public Gps gps = new Gps();
    public Hand hand = new Hand();
    public Map<String, Marker> markers = new LinkedHashMap<>();

    public enum Theme {
        DARK,
        LIGHT,
        INFERNO
    }

    public static final class Hud {
        public boolean watermark = true;
        public boolean potionsHud = true;
        public boolean cooldownHud = true;
        public boolean targetHud = true;
        public boolean armorHud = true;
        public boolean statsHud = true;
        public boolean compassHud = true;
        public boolean coordsHud = true;
        public boolean durabilityOverlay = true;
        public boolean trajectoryHud = true;
        public boolean handView = true;
        public int watermarkX = 8;
        public int watermarkY = 8;
        public int leftColumnX = 8;
        public int leftColumnY = 34;
        public int rightColumnX = 0;
        public int rightColumnY = 34;
    }

    public static final class Animations {
        public float hudFadeSpeed = 0.15f;
        public float barLerpSpeed = 0.18f;
        public float gpsLerpSpeed = 0.2f;
    }


    public static final class Hand {
        public float offsetX = 0.0f;
        public float offsetY = 0.0f;
        public float offsetZ = 0.0f;
        public float size = 1.0f;
        public float swingMultiplier = 1.0f;
    }

    public static final class Keybinds {
        public int elytraSwapKey = 71; // G
    }

    public static final class Gps {
        public boolean enabled = false;
        public int x = 0;
        public int y = 64;
        public int z = 0;
    }

    public static final class Marker {
        public String name;
        public int x;
        public int y;
        public int z;

        public Marker() {
        }

        public Marker(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
