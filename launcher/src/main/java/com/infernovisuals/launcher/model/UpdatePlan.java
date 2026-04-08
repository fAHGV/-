package com.infernovisuals.launcher.model;

import java.util.ArrayList;
import java.util.List;

public final class UpdatePlan {
    public final List<LauncherManifest.ManifestFile> toDownload = new ArrayList<>();

    public boolean isEmpty() {
        return toDownload.isEmpty();
    }
}
