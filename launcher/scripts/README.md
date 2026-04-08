# Launcher packaging scripts

## Требования

- JDK 21+ (`java`, `javac`, `jar`, `jpackage` в PATH)
- Интернет (первый запуск скачивает `gson`)

## Windows CMD

```bat
launcher\scripts\build-launcher.bat
```

## Windows PowerShell

```powershell
powershell -ExecutionPolicy Bypass -File launcher\scripts\build-launcher.ps1
```

Скрипты:

1. скачивают `gson` в `launcher/lib/`;
2. компилируют исходники лаунчера;
3. собирают `launcher/build/infernovisuals-launcher.jar`;
4. упаковывают `.exe` через `jpackage` в `launcher/dist/`.

После сборки можно проверить профиль без запуска Minecraft:

```bash
java -jar launcher/build/infernovisuals-launcher.jar --no-launch
```


## One-click запуск

После распаковки ZIP можно просто запустить:

- `launcher/Start-Launcher.bat`
- `launcher/Start-Launcher.ps1`

Скрипт проверит наличие готового лаунчера, при необходимости соберёт его и запустит.
