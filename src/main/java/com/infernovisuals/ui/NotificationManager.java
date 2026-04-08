package com.infernovisuals.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    private final List<Notification> notifications = new ArrayList<>();

    public void push(String message) {
        notifications.add(new Notification(message));
    }

    public void render(DrawContext context, float fadeSpeed) {
        MinecraftClient client = MinecraftClient.getInstance();
        int y = context.getScaledWindowHeight() - 24;
        Iterator<Notification> iterator = notifications.iterator();

        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            notification.life--;
            notification.alpha = MathHelper.lerp(fadeSpeed, notification.alpha, notification.life > 20 ? 1.0f : 0.0f);

            if (notification.life <= 0 && notification.alpha < 0.03f) {
                iterator.remove();
                continue;
            }

            int width = client.textRenderer.getWidth(notification.message) + 12;
            int x = context.getScaledWindowWidth() - width - 8;
            int a = (int) (notification.alpha * 160.0f);
            context.fill(x, y - 2, x + width, y + 11, (a << 24) | 0x121212);
            context.drawText(client.textRenderer, Text.literal(notification.message), x + 6, y + 2, 0xFFF0F0F0, false);
            y -= 14;
        }
    }

    private static final class Notification {
        private final String message;
        private int life = 100;
        private float alpha = 0.0f;

        private Notification(String message) {
            this.message = message;
        }
    }
}
