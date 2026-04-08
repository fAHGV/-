# InfernoVisuals Launcher (Simple)

Минималистичный лаунчер для запуска Minecraft/Fabric профиля.

## Что умеет

- хранит профили в `launcher-data/profiles.json`;
- поддерживает `offline` и заглушку `microsoft` авторизации;
- запускает игру через `ProcessBuilder`;
- валидирует профиль перед запуском и выводит понятные ошибки.

## Быстрый старт (из ZIP)

1. Установи **JDK 21+** (нужны `java`, `javac`, `jpackage`).
2. Распакуй ZIP проекта.
3. Запусти сборку лаунчера:
   - `launcher/scripts/build-launcher.bat` (CMD)
   - `launcher/scripts/build-launcher.ps1` (PowerShell)
4. Готовый exe будет в `launcher/dist`.

### Запуск в 1 клик

- Двойной клик: `launcher/Start-Launcher.bat`
- PowerShell: `launcher/Start-Launcher.ps1`

Если лаунчер ещё не собран, скрипт сам запустит сборку и затем откроет launcher.

## Первый запуск

После первого запуска создаётся `launcher-data/profiles.json`.
Проверь и заполни поля:

- `javaPath`
- `classpath`
- `mainClass`
- `gameDir`
- `assetsDir`
- `versionName`

Проверка без старта игры:

```bash
java -jar infernovisuals-launcher.jar --no-launch
```

## Аргументы запуска

- `--help` — помощь
- `--list` — список профилей
- `--profile <name>` — выбрать профиль
- `--no-launch` — только проверить профиль
