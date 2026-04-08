package com.infernovisuals;

import com.infernovisuals.config.ConfigManager;
import com.infernovisuals.config.InfernoConfig;
import com.infernovisuals.ui.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

public final class InfernoVisualsClient implements ClientModInitializer {
    private static InfernoVisualsClient INSTANCE;
    private final NotificationManager notifications = new NotificationManager();
    private final Map<String, String> descriptions = Map.ofEntries(
            Map.entry("watermark", "Watermark — показывает название клиента в левом верхнем углу."),
            Map.entry("potions", "Potions HUD — показывает активные эффекты игрока с уровнем и таймером."),
            Map.entry("cooldowns", "Cooldown HUD — показывает кулдауны первых трех слотов хотбара."),
            Map.entry("target", "Target HUD — отображает имя цели, HP, броню и эффекты."),
            Map.entry("armor", "Armor HUD — показывает броню и прочность предметов."),
            Map.entry("stats", "FPS/TPS/Ping HUD — отображает производительность и задержку."),
            Map.entry("compass", "Compass HUD — минималистичное отображение стороны света."),
            Map.entry("coords", "Coordinates HUD — показывает XYZ, высоту и биом."),
            Map.entry("trajectory", "Trajectory Prediction — прогнозирует траекторию и точку падения снаряда."),
            Map.entry("handview", "Hand View — настройка положения рук, размера предмета и swing анимации."),
            Map.entry("gps", "GPS — стрелка указывает направление к координатам из команды .gps x y z."),
            Map.entry("elytraswap", "ElytraSwap — меняет элитру на нагрудник и обратно по бинд-клавише.")
    );

    private ConfigManager configManager;
    private InfernoConfig config;
    private String configName = "default";
    private KeyBinding elytraSwapKey;
    private KeyBinding clickGuiKey;
    private boolean clickGuiOpen = false;
    private float clickGuiAlpha = 0.0F;
    private boolean clickGuiMouseDown = false;
    private boolean clickGuiRightMouseDown = false;
    private String settingsModule = null;

    private float targetAnim = 0.0F;
    private float targetHealthDisplay = 0.0F;
    private float gpsYawDisplay = 0.0F;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        MinecraftClient client = MinecraftClient.getInstance();
        this.configManager = new ConfigManager(client.runDirectory.toPath());
        this.config = configManager.load(configName);

        this.elytraSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.infernovisuals.elytraswap", config.keybinds.elytraSwapKey, "category.infernovisuals"));
        this.clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.infernovisuals.clickgui", GLFW.GLFW_KEY_RIGHT_SHIFT, "category.infernovisuals"));

        HudRenderCallback.EVENT.register(this::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientSendMessageEvents.ALLOW_CHAT.register(this::onChatMessage);

        notifications.push("InfernoVisuals загружен");
    }

    private void onTick(MinecraftClient client) {
        while (elytraSwapKey.wasPressed()) {
            performElytraSwap(client);
        }
        while (clickGuiKey.wasPressed()) {
            clickGuiOpen = !clickGuiOpen;
            notifications.push("ClickGUI: " + (clickGuiOpen ? "ON" : "OFF"));
        }

        if (clickGuiOpen) {
            handleClickGuiInput(client);
        } else {
            clickGuiMouseDown = false;
            clickGuiRightMouseDown = false;
        }
    }

    private boolean onChatMessage(String message) {
        if (!message.startsWith(".")) {
            return true;
        }

        String[] args = message.substring(1).trim().split("\\s+");
        if (args.length == 0) {
            return false;
        }

        String cmd = args[0].toLowerCase(Locale.ROOT);
        if (cmd.equals("cfg")) {
            handleCfg(args);
            return false;
        }
        if (cmd.equals("gps")) {
            handleGps(args);
            return false;
        }
        if (cmd.equals("marker")) {
            handleMarker(args);
            return false;
        }

        return true;
    }

    private void handleCfg(String[] args) {
        if (args.length < 2) {
            notifications.push(".cfg help — список команд");
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "load" -> {
                config = configManager.load(configName);
                notifications.push("Конфиг загружен: " + configName);
            }
            case "save" -> {
                boolean ok = configManager.save(configName, config);
                notifications.push(ok ? "Конфиг сохранен" : "Ошибка сохранения конфига");
            }
            case "create" -> {
                boolean ok = configManager.createIfMissing(configName, config);
                notifications.push(ok ? "Создан новый конфиг" : "Конфиг уже существует");
            }
            case "preview" -> {
                config.hud.watermark = !config.hud.watermark;
                notifications.push("Config Preview: " + (config.hud.watermark ? "ON" : "OFF"));
            }
            case "help" -> {
                notifications.push(".cfg load|save|create|help");
                for (String value : descriptions.values()) {
                    notifications.push(value);
                }
            }
            default -> notifications.push("Неизвестная .cfg команда");
        }
    }

    private void handleGps(String[] args) {
        if (args.length < 4) {
            notifications.push("Использование: .gps x y z");
            return;
        }

        try {
            config.gps.x = Integer.parseInt(args[1]);
            config.gps.y = Integer.parseInt(args[2]);
            config.gps.z = Integer.parseInt(args[3]);
            config.gps.enabled = true;
            notifications.push("GPS точка: " + config.gps.x + " " + config.gps.y + " " + config.gps.z);
        } catch (NumberFormatException ex) {
            notifications.push("GPS: координаты должны быть числами");
        }
    }

    private void handleMarker(String[] args) {
        if (args.length < 5) {
            notifications.push("Использование: .marker name x y z");
            return;
        }

        try {
            String name = args[1];
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            config.markers.put(name.toLowerCase(Locale.ROOT), new InfernoConfig.Marker(name, x, y, z));
            notifications.push("Маркер сохранен: " + name);
        } catch (NumberFormatException ex) {
            notifications.push("Marker: координаты должны быть числами");
        }
    }

    private void performElytraSwap(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        int elytraSlot = findItemInInventory(player, Items.ELYTRA);
        int chestplateSlot = findFirstChestplate(player);

        if (chest.isOf(Items.ELYTRA) && chestplateSlot != -1) {
            swapArmor(player, chestplateSlot);
            notifications.push("ElytraSwap: экипирован нагрудник");
            return;
        }

        if (!chest.isOf(Items.ELYTRA) && elytraSlot != -1) {
            swapArmor(player, elytraSlot);
            notifications.push("ElytraSwap: экипирована элитра");
            return;
        }

        notifications.push("ElytraSwap: предмет не найден");
    }

    private int findItemInInventory(ClientPlayerEntity player, net.minecraft.item.Item item) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (player.getInventory().getStack(slot).isOf(item)) {
                return slot;
            }
        }
        return -1;
    }

    private int findFirstChestplate(ClientPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(Items.DIAMOND_CHESTPLATE) || stack.isOf(Items.NETHERITE_CHESTPLATE)
                    || stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE)
                    || stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE)) {
                return slot;
            }
        }
        return -1;
    }

    private void swapArmor(ClientPlayerEntity player, int inventorySlot) {
        ItemStack fromInventory = player.getInventory().getStack(inventorySlot).copy();
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST).copy();
        player.getInventory().setStack(inventorySlot, chest);
        player.equipStack(EquipmentSlot.CHEST, fromInventory);
    }

    private void handleClickGuiInput(MinecraftClient client) {
        if (client.currentScreen != null) {
            return;
        }

        boolean leftDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        double mx = client.mouse.getX() * sw / client.getWindow().getWidth();
        double my = client.mouse.getY() * sh / client.getWindow().getHeight();

        int panelW = 300;
        int panelH = 180;
        int x = sw / 2 - panelW / 2;
        int y = sh / 2 - panelH / 2;

        if (leftDown && !clickGuiMouseDown) {
            clickGuiMouseDown = true;
            if (inside(mx, my, x + 10, y + 30, 130, 12)) config.hud.watermark = !config.hud.watermark;
            else if (inside(mx, my, x + 10, y + 44, 130, 12)) config.hud.potionsHud = !config.hud.potionsHud;
            else if (inside(mx, my, x + 10, y + 58, 130, 12)) config.hud.cooldownHud = !config.hud.cooldownHud;
            else if (inside(mx, my, x + 10, y + 72, 130, 12)) config.hud.targetHud = !config.hud.targetHud;
            else if (inside(mx, my, x + 10, y + 86, 130, 12)) config.hud.trajectoryHud = !config.hud.trajectoryHud;
            else if (inside(mx, my, x + 160, y + 30, 130, 12)) config.hud.armorHud = !config.hud.armorHud;
            else if (inside(mx, my, x + 160, y + 44, 130, 12)) config.hud.statsHud = !config.hud.statsHud;
            else if (inside(mx, my, x + 160, y + 58, 130, 12)) config.hud.compassHud = !config.hud.compassHud;
            else if (inside(mx, my, x + 160, y + 72, 130, 12)) config.hud.coordsHud = !config.hud.coordsHud;
            else if (inside(mx, my, x + 160, y + 86, 130, 12)) config.hud.durabilityOverlay = !config.hud.durabilityOverlay;
            else if (inside(mx, my, x + 160, y + 100, 130, 12)) config.hud.handView = !config.hud.handView;
            else if (inside(mx, my, x + 10, y + 112, 140, 12)) cycleTheme();
            else if (inside(mx, my, x + 10, y + 152, 280, 16) && settingsModule != null) {
                adjustSettingByClick(mx, x + 10, y + 152);
            }
            notifications.push("ClickGUI: значение обновлено");
        }

        if (!leftDown) {
            clickGuiMouseDown = false;
        }

        if (rightDown && !clickGuiRightMouseDown) {
            clickGuiRightMouseDown = true;
            if (inside(mx, my, x + 10, y + 86, 130, 12)) settingsModule = "trajectory";
            else if (inside(mx, my, x + 160, y + 100, 130, 12)) settingsModule = "handview";
            else settingsModule = null;
            if (settingsModule != null) {
                notifications.push("Открыты настройки: " + settingsModule);
            }
        }

        if (!rightDown) {
            clickGuiRightMouseDown = false;
        }

    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void cycleTheme() {
        config.theme = switch (config.theme) {
            case INFERNO -> InfernoConfig.Theme.DARK;
            case DARK -> InfernoConfig.Theme.LIGHT;
            case LIGHT -> InfernoConfig.Theme.INFERNO;
        };
    }

    private void adjustSettingByClick(double mx, int x, int y) {
        if (settingsModule == null) {
            return;
        }
        boolean minus = mx < x + 140;
        float step = minus ? -0.05f : 0.05f;

        if (settingsModule.equals("handview")) {
            config.hand.size = MathHelper.clamp(config.hand.size + step, 0.5f, 1.8f);
            config.hand.offsetY = MathHelper.clamp(config.hand.offsetY + (minus ? -0.01f : 0.01f), -0.6f, 0.6f);
            config.hand.swingMultiplier = MathHelper.clamp(config.hand.swingMultiplier + step, 0.2f, 3.0f);
        }
        if (settingsModule.equals("trajectory")) {
            config.animations.gpsLerpSpeed = MathHelper.clamp(config.animations.gpsLerpSpeed + step, 0.05f, 0.8f);
        }
    }

    public static InfernoVisualsClient getInstance() {
        return INSTANCE;
    }

    public boolean isHandViewEnabled() {
        return config != null && config.hud.handView;
    }

    public float getHandOffsetX() {
        return config == null ? 0.0f : config.hand.offsetX;
    }

    public float getHandOffsetY() {
        return config == null ? 0.0f : config.hand.offsetY;
    }

    public float getHandOffsetZ() {
        return config == null ? 0.0f : config.hand.offsetZ;
    }

    public float getHandSize() {
        return config == null ? 1.0f : config.hand.size;
    }

    public float getSwingMultiplier() {
        return config == null ? 1.0f : config.hand.swingMultiplier;
    }

    private void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        applyAutoTheme(player);

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int rightX = config.hud.rightColumnX == 0 ? sw - 150 : config.hud.rightColumnX;

        if (config.hud.watermark) {
            renderWatermark(context, config.hud.watermarkX, config.hud.watermarkY);
        }
        if (config.hud.potionsHud) {
            renderPotionsHud(context, player, config.hud.leftColumnX, config.hud.leftColumnY);
        }
        if (config.hud.cooldownHud) {
            renderCooldownHud(context, player, rightX, config.hud.rightColumnY);
        }

        PlayerEntity target = getCrosshairTarget(client);
        targetAnim = MathHelper.lerp(config.animations.hudFadeSpeed, targetAnim, target != null && config.hud.targetHud ? 1.0F : 0.0F);
        if (targetAnim > 0.02F && target != null && config.hud.targetHud) {
            int x = sw / 2 - 92;
            int y = sh / 2 + 18;
            renderTargetHud(context, target, x, y, targetAnim);
        }

        if (config.hud.armorHud) {
            renderArmorHud(context, player, 8, sh - 34);
        }
        if (config.hud.statsHud) {
            renderStatsHud(context, client, rightX, 8);
        }
        if (config.hud.compassHud) {
            renderCompass(context, player, sw / 2 - 20, 8);
        }
        if (config.hud.coordsHud) {
            renderCoords(context, player, 8, sh - 58);
        }
        if (config.gps.enabled) {
            renderGps(context, player, sw / 2 - 28, sh - 78);
        }
        if (config.hud.trajectoryHud) {
            renderTrajectoryHud(context, player, rightX, 44);
        }

        renderMarkers(context, player, 8, sh - 92);

        clickGuiAlpha = MathHelper.lerp(config.animations.hudFadeSpeed, clickGuiAlpha, clickGuiOpen ? 1.0f : 0.0f);
        if (clickGuiAlpha > 0.02f) {
            renderClickGui(context, sw, sh, clickGuiAlpha);
        }

        notifications.render(context, config.animations.hudFadeSpeed);
    }

    private void renderWatermark(DrawContext context, int x, int y) {
        context.fill(x, y, x + 130, y + 18, bgColor());
        context.fill(x, y + 16, x + 130, y + 18, accentColor());
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("InfernoVisuals").formatted(Formatting.WHITE), x + 6, y + 5, 0xFFF5F5F5, false);
    }

    private void renderPotionsHud(DrawContext context, ClientPlayerEntity player, int x, int y) {
        List<StatusEffectInstance> effects = new ArrayList<>(player.getStatusEffects());
        effects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());

        int height = Math.max(24, 20 + effects.size() * 12);
        context.fill(x, y, x + 150, y + height, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Potions"), x + 6, y + 5, 0xFFECECEC, false);

        int rowY = y + 18;
        float tickRate = MinecraftClient.getInstance().world != null
                ? MinecraftClient.getInstance().world.getTickManager().getTickRate()
                : 20.0F;

        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            String duration = StatusEffectUtil.formatDuration(effect, 1.0F, tickRate).getString();
            int color = effect.getEffectType().value().getCategory() == StatusEffectCategory.BENEFICIAL ? 0xFF78D77D : 0xFFE36A6A;
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(name), x + 6, rowY, color, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(duration), x + 92, rowY, 0xFFE0E0E0, false);
            rowY += 12;
        }
    }

    private void renderCooldownHud(DrawContext context, ClientPlayerEntity player, int x, int y) {
        context.fill(x, y, x + 140, y + 56, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Cooldowns"), x + 6, y + 5, 0xFFECECEC, false);

        int row = y + 18;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            float cooldown = player.getItemCooldownManager().getCooldownProgress(stack.getItem(), 0.0F);
            float smooth = MathHelper.lerp(config.animations.barLerpSpeed, 0.0F, 1.0F - cooldown);
            int width = (int) (100 * smooth);

            context.drawItem(stack, x + 6, row - 4);
            context.drawText(MinecraftClient.getInstance().textRenderer, stack.getName(), x + 26, row, 0xFFE3E3E3, false);
            context.fill(x + 26, row + 10, x + 126, row + 12, 0x55101010);
            context.fill(x + 26, row + 10, x + 26 + width, row + 12, accentColor());
            row += 12;
        }
    }

    private void renderTargetHud(DrawContext context, LivingEntity target, int x, int y, float animation) {
        int width = (int) (184 * animation);
        context.fill(x, y, x + width, y + 54, bgColor());
        if (width < 90) {
            return;
        }

        float hpPercent = MathHelper.clamp(target.getHealth() / Math.max(1.0f, target.getMaxHealth()), 0.0F, 1.0F);
        targetHealthDisplay = MathHelper.lerp(config.animations.barLerpSpeed, targetHealthDisplay, hpPercent);

        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(target.getName().getString()), x + 8, y + 8, 0xFFFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("HP: " + String.format(Locale.US, "%.1f", target.getHealth())), x + 8, y + 20, 0xFFE0E0E0, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Armor: " + target.getArmor()), x + 88, y + 20, 0xFFBFC7FF, false);

        context.fill(x + 8, y + 36, x + 172, y + 40, 0x66101010);
        context.fill(x + 8, y + 36, x + 8 + (int) (164 * targetHealthDisplay), y + 40, accentColor());

        int ey = y + 44;
        for (StatusEffectInstance effect : target.getStatusEffects()) {
            context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.literal(effect.getEffectType().value().getName().getString()), x + 8, ey, 0xFFD4D4D4, false);
            ey += 10;
            if (ey > y + 52) {
                break;
            }
        }
    }

    private void renderArmorHud(DrawContext context, ClientPlayerEntity player, int x, int y) {
        context.fill(x, y, x + 118, y + 20, bgColor());
        int dx = x + 6;
        for (ItemStack stack : player.getArmorItems()) {
            context.drawItem(stack, dx, y + 2);
            if (config.hud.durabilityOverlay && stack.isDamageable()) {
                int left = stack.getMaxDamage() - stack.getDamage();
                int color = left > stack.getMaxDamage() * 0.5 ? 0xFF78D77D : (left > stack.getMaxDamage() * 0.25 ? 0xFFE5D16A : 0xFFE36A6A);
                context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(String.valueOf(left)), dx, y + 12, color, false);
            }
            dx += 20;
        }
    }

    private void renderStatsHud(DrawContext context, MinecraftClient client, int x, int y) {
        int fps = client.getCurrentFps();
        int ping = client.getNetworkHandler() != null && client.player != null && client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()) != null
                ? client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()).getLatency() : 0;
        int tps = client.world != null ? (int) client.world.getTickManager().getTickRate() : 20;

        context.fill(x, y, x + 142, y + 32, bgColor());
        context.drawText(client.textRenderer, Text.literal("FPS: " + fps), x + 6, y + 4, qualityColor(fps, 120, 60), false);
        context.drawText(client.textRenderer, Text.literal("TPS: " + tps), x + 6, y + 14, qualityColor(tps, 19, 16), false);
        context.drawText(client.textRenderer, Text.literal("Ping: " + ping), x + 72, y + 14, qualityColor(200 - ping, 140, 80), false);
    }

    private void renderCompass(DrawContext context, ClientPlayerEntity player, int x, int y) {
        String dir = directionFromYaw(player.getYaw());
        context.fill(x, y, x + 44, y + 16, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(dir), x + 16, y + 4, accentColor(), false);
    }

    private void renderCoords(DrawContext context, ClientPlayerEntity player, int x, int y) {
        String biome = MinecraftClient.getInstance().world != null
                ? MinecraftClient.getInstance().world.getBiome(player.getBlockPos()).getKey().map(key -> key.getValue().toString()).orElse("unknown")
                : "unknown";
        context.fill(x, y, x + 210, y + 22, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal(String.format(Locale.US, "XYZ: %d %d %d", (int) player.getX(), (int) player.getY(), (int) player.getZ())),
                x + 6, y + 4, 0xFFE9E9E9, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Biome: " + biome), x + 6, y + 14, 0xFFBFBFBF, false);
    }

    private void renderGps(DrawContext context, ClientPlayerEntity player, int x, int y) {
        double dx = config.gps.x + 0.5 - player.getX();
        double dz = config.gps.z + 0.5 - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float diff = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        gpsYawDisplay = MathHelper.lerp(config.animations.gpsLerpSpeed, gpsYawDisplay, diff);

        String arrow = arrowFromAngle(gpsYawDisplay);
        context.fill(x, y, x + 96, y + 26, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal("GPS " + arrow + " " + (int) distance + "m"), x + 6, y + 5, accentColor(), false);
    }

    private void renderMarkers(DrawContext context, ClientPlayerEntity player, int x, int y) {
        if (config.markers.isEmpty()) {
            return;
        }

        int row = y;
        int rendered = 0;
        for (InfernoConfig.Marker marker : config.markers.values()) {
            if (rendered >= 3) {
                break;
            }
            double distance = Math.sqrt(Math.pow(marker.x - player.getX(), 2) + Math.pow(marker.z - player.getZ(), 2));
            context.fill(x, row, x + 188, row + 11, bgColor());
            context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.literal("• " + marker.name + " " + (int) distance + "m"), x + 4, row + 2, 0xFFDFDFDF, false);
            row += 12;
            rendered++;
        }
    }

    private void renderTrajectoryHud(DrawContext context, ClientPlayerEntity player, int x, int y) {
        TrajectoryPrediction prediction = predictTrajectory(player);
        if (prediction == null) {
            return;
        }

        context.fill(x, y, x + 142, y + 34, bgColor());
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal("Trajectory: " + prediction.type), x + 6, y + 4, 0xFFEAEAEA, false);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal(String.format(Locale.US, "Impact %.0f %.0f %.0f", prediction.impact.x, prediction.impact.y, prediction.impact.z)),
                x + 6, y + 14, 0xFFD4D4D4, false);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal(String.format(Locale.US, "Time: %.2fs", prediction.timeTicks / 20.0f)),
                x + 6, y + 24, accentColor(), false);
    }

    private TrajectoryPrediction predictTrajectory(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        Item item = stack.getItem();

        ProjectileProfile profile = projectileProfile(player, stack);
        if (profile == null) {
            return null;
        }

        Vec3d pos = player.getEyePos();
        Vec3d velocity = player.getRotationVec(1.0f).multiply(profile.speed);
        Vec3d prev = pos;

        for (int tick = 0; tick < 120; tick++) {
            Vec3d next = pos.add(velocity);
            BlockHitResult hit = player.getWorld().raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player));

            if (hit.getType() != HitResult.Type.MISS) {
                return new TrajectoryPrediction(item.getName().getString(), hit.getPos(), tick);
            }

            if (next.y < player.getWorld().getBottomY()) {
                return new TrajectoryPrediction(item.getName().getString(), next, tick);
            }

            prev = pos;
            pos = next;
            velocity = velocity.multiply(profile.drag);
            velocity = velocity.add(0.0, -profile.gravity, 0.0);
        }

        return new TrajectoryPrediction(item.getName().getString(), prev, 120);
    }

    private ProjectileProfile projectileProfile(ClientPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof BowItem) {
            int useTicks = stack.getMaxUseTime(player) - player.getItemUseTimeLeft();
            float power = BowItem.getPullProgress(useTicks);
            if (power <= 0.0f) {
                power = 1.0f;
            }
            return new ProjectileProfile(3.0f * power, 0.05f, 0.99f);
        }

        if (item instanceof CrossbowItem) {
            return new ProjectileProfile(3.15f, 0.05f, 0.99f);
        }

        if (item == Items.TRIDENT) {
            return new ProjectileProfile(2.5f, 0.05f, 0.99f);
        }

        if (item == Items.SNOWBALL || item == Items.EGG || item == Items.ENDER_PEARL) {
            return new ProjectileProfile(1.5f, 0.03f, 0.99f);
        }

        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.EXPERIENCE_BOTTLE) {
            return new ProjectileProfile(0.5f, 0.05f, 0.99f);
        }

        return null;
    }

    private record ProjectileProfile(float speed, float gravity, float drag) {
    }

    private record TrajectoryPrediction(String type, Vec3d impact, int timeTicks) {
    }

    private void renderClickGui(DrawContext context, int sw, int sh, float alpha) {
        int panelW = 300;
        int panelH = 180;
        int x = sw / 2 - panelW / 2;
        int y = sh / 2 - panelH / 2;

        int overlayA = (int) (alpha * 90.0f);
        int panelA = (int) (alpha * 200.0f);

        context.fill(0, 0, sw, sh, (overlayA << 24));
        context.fill(x, y, x + panelW, y + panelH, (panelA << 24) | 0x0F0F0F);
        context.fill(x, y, x + panelW, y + 2, accentColor());

        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, Text.literal("InfernoVisuals ClickGUI"), x + 10, y + 8, 0xFFF2F2F2, false);
        context.drawText(client.textRenderer, Text.literal("Открытие: RSHIFT"), x + panelW - 98, y + 8, 0xFFBDBDBD, false);

        drawGuiToggle(context, x + 10, y + 30, "Watermark", config.hud.watermark);
        drawGuiToggle(context, x + 10, y + 44, "Potions HUD", config.hud.potionsHud);
        drawGuiToggle(context, x + 10, y + 58, "Cooldown HUD", config.hud.cooldownHud);
        drawGuiToggle(context, x + 10, y + 72, "Target HUD", config.hud.targetHud);
        drawGuiToggle(context, x + 10, y + 86, "Trajectory", config.hud.trajectoryHud);

        drawGuiToggle(context, x + 160, y + 30, "Armor HUD", config.hud.armorHud);
        drawGuiToggle(context, x + 160, y + 44, "Stats HUD", config.hud.statsHud);
        drawGuiToggle(context, x + 160, y + 58, "Compass HUD", config.hud.compassHud);
        drawGuiToggle(context, x + 160, y + 72, "Coords HUD", config.hud.coordsHud);
        drawGuiToggle(context, x + 160, y + 86, "Durability", config.hud.durabilityOverlay);
        drawGuiToggle(context, x + 160, y + 100, "Hand View", config.hud.handView);

        int hx = (int) (client.mouse.getX() * sw / client.getWindow().getWidth());
        int hy = (int) (client.mouse.getY() * sh / client.getWindow().getHeight());
        drawHover(context, hx, hy, x + 10, y + 86, 130, 12);
        drawHover(context, hx, hy, x + 160, y + 100, 130, 12);

        context.drawText(client.textRenderer, Text.literal("Theme: " + config.theme.name() + " (клик)"), x + 10, y + 114, accentColor(), false);
        context.drawText(client.textRenderer, Text.literal(".cfg save для сохранения"), x + 10, y + 130, 0xFFD0D0D0, false);
        context.drawText(client.textRenderer, Text.literal("ЛКМ: toggle | ПКМ: настройки модуля"), x + 10, y + 144, 0xFFAAAAAA, false);

        if (settingsModule != null) {
            context.fill(x + 10, y + 152, x + 290, y + 168, 0x66161616);
            String text = settingsModule.equals("handview")
                    ? String.format(Locale.US, "Hand size %.2f | offsetY %.2f | swing %.2f  (клик слева/справа -/+)", config.hand.size, config.hand.offsetY, config.hand.swingMultiplier)
                    : String.format(Locale.US, "Trajectory smooth %.2f (клик слева/справа -/+)", config.animations.gpsLerpSpeed);
            context.drawText(client.textRenderer, Text.literal(text), x + 14, y + 156, 0xFFE2E2E2, false);
        }
    }

    private void drawHover(DrawContext context, int mx, int my, int x, int y, int w, int h) {
        if (inside(mx, my, x, y, w, h)) {
            context.fill(x - 2, y - 1, x + w, y + h - 1, 0x331FFFFFFF);
        }
    }

    private void drawGuiToggle(DrawContext context, int x, int y, String label, boolean value) {
        int color = value ? accentColor() : 0xFF777777;
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal((value ? "● " : "○ ") + label), x, y, color, false);
    }

    private int bgColor() {
        return switch (config.theme) {
            case LIGHT -> 0xA8ECECEC;
            case DARK -> 0xA8151515;
            case INFERNO -> 0xA8101010;
        };
    }

    private int accentColor() {
        return switch (config.theme) {
            case LIGHT -> 0xFF4DA3FF;
            case DARK -> 0xFF66CC88;
            case INFERNO -> 0xFFFF3045;
        };
    }

    private void applyAutoTheme(ClientPlayerEntity player) {
        if (player.getWorld().isNight()) {
            config.theme = InfernoConfig.Theme.INFERNO;
        } else if (player.getHealth() < 10.0f) {
            config.theme = InfernoConfig.Theme.DARK;
        }
    }

    private int qualityColor(int value, int good, int warn) {
        if (value >= good) {
            return 0xFF77D67B;
        }
        if (value >= warn) {
            return 0xFFE8CF72;
        }
        return 0xFFE16B6B;
    }

    private String directionFromYaw(float yaw) {
        float normalized = MathHelper.wrapDegrees(yaw);
        if (normalized >= -45 && normalized < 45) return "S";
        if (normalized >= 45 && normalized < 135) return "W";
        if (normalized >= -135 && normalized < -45) return "E";
        return "N";
    }

    private String arrowFromAngle(float diff) {
        if (diff > -22.5F && diff <= 22.5F) return "↑";
        if (diff > 22.5F && diff <= 67.5F) return "↖";
        if (diff > 67.5F && diff <= 112.5F) return "←";
        if (diff > 112.5F && diff <= 157.5F) return "↙";
        if (diff > -67.5F && diff <= -22.5F) return "↗";
        if (diff > -112.5F && diff <= -67.5F) return "→";
        if (diff > -157.5F && diff <= -112.5F) return "↘";
        return "↓";
    }

    private PlayerEntity getCrosshairTarget(MinecraftClient client) {
        return client.targetedEntity instanceof PlayerEntity playerEntity ? playerEntity : null;
    }
}
