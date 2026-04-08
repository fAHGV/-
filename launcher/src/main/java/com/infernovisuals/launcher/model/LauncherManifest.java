package com.infernovisuals.launcher.model;

import java.util.ArrayList;
import java.util.List;

public final class LauncherManifest {
    public String clientVersion;
    public String minLauncherVersion;
    public List<String> news = new ArrayList<>();
    public List<ManifestFile> files = new ArrayList<>();

    public static final class ManifestFile {
        public String path;
        public String url;
        public String sha256;
        public long size;
    }
}
