package com.infernovisuals.launcher.auth;

public record AuthSession(String playerName, String uuid, String accessToken, String userType, boolean online) {
}
