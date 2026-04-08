# InfernoVisuals (Fabric 1.21.4)

InfernoVisuals — минималистичный клиентский HUD-мод в современном стиле для Fabric 1.21.4.

## Основные функции

- `.cfg load|save|create|help` — управление конфигом HUD и функций.
- `.gps x y z` — установка GPS-точки с направляющей стрелкой и дистанцией.
- Trajectory Prediction для лука, арбалета, яйца, снежка, трезубца, эндер-перла и пузырьков.
- `.marker name x y z` — сохранение мини-маркеров координат.
- ElytraSwap по бинд-клавише (по умолчанию `G`).
- ClickGUI в минималистичном стиле, открывается на `RSHIFT`, кликабелен (переключение HUD-модулей + смена темы).
- ПКМ по модулю открывает его настройки (в т.ч. Hand View / Trajectory).
- Hand View: настройка положения рук, размера предмета в руке и swing animation.
- HUD: Watermark, Potions, Cooldowns, Target, Armor, FPS/TPS/Ping, Compass, Coordinates, Durability Overlay, Trajectory Prediction, Notifications.
- Auto Theme: DARK / LIGHT / INFERNO.

## Конфиг

Конфиг хранится в `config/infernovisuals/default.json` и содержит:

- позиции HUD элементов,
- включение/выключение модулей,
- тему и анимации,
- GPS точку,
- бинд ElytraSwap,
- список маркеров.

## Build

```bash
gradle build
```

Jar output path:

```text
build/libs/infernovisuals-1.0.0.jar
```

## Требования

- JDK 21
- Gradle 8.8+

## Примечание

Мод ориентирован на визуальные и QoL-функции HUD без боевой автоматизации.

## Launcher (Simple)

В репозиторий добавлен упрощённый лаунчер в папке `launcher/`:

- профили,
- auth mode (`offline` / `microsoft`),
- аккуратный базовый запуск без автообновления.

Это стабильная простая база, которую можно расширить до `.exe` лаунчера с UI и OAuth.
