# Launcher packaging scripts

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
3. собирают `infernovisuals-launcher.jar`;
4. упаковывают `.exe` через `jpackage` в `launcher/dist/`.
