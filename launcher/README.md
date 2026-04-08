# InfernoVisuals Launcher (Simple)

Упрощённый лаунчер без автообновления — чисто аккуратный стабильный старт.

## Что делает

- хранит профили (`profiles.json`),
- поддерживает режим авторизации (`offline` / `microsoft`),
- запускает Minecraft через `ProcessBuilder`,
- поддерживает параметры окна/сервера/ram/java/classpath/mainClass.

## Важно перед запуском

В `profiles.json` должны быть корректные:

- `javaPath`
- `classpath`
- `mainClass`
- `gameDir`
- `assetsDir`
- `versionName`

## Следующий шаг

- добавить UI (JavaFX)
- подключить реальный Microsoft OAuth flow
- (опционально позже) вернуть автообновление


## Сборка .exe

Готовые скрипты сборки находятся в `launcher/scripts/`:

- `build-launcher.bat`
- `build-launcher.ps1`

Подробнее: `launcher/scripts/README.md`.
