package com.infernovisuals.launcher.auth;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class OfflineAuthService implements AuthService {
    private final String nickname;

    public OfflineAuthService(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public AuthSession login() {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + nickname).getBytes(StandardCharsets.UTF_8));
        return new AuthSession(nickname, uuid.toString(), "OFFLINE_TOKEN", "legacy", false);
    }
}
