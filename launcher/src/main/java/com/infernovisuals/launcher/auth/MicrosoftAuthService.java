package com.infernovisuals.launcher.auth;

public final class MicrosoftAuthService implements AuthService {
    @Override
    public AuthSession login() {
        throw new UnsupportedOperationException(
                "Microsoft OAuth flow еще не подключен в MVP. Добавь device-code/webview flow для production.");
    }
}
