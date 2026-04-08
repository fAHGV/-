package com.infernovisuals.mixin;

import com.infernovisuals.InfernoVisualsClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void inferno$modifyHand(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                    float swingProgress, ItemStack item, float equipProgress,
                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                    int light, CallbackInfo ci) {
        InfernoVisualsClient client = InfernoVisualsClient.getInstance();
        if (client == null || !client.isHandViewEnabled()) {
            return;
        }

        float dir = hand == Hand.MAIN_HAND && player.getMainArm() == Arm.RIGHT ? 1.0f : -1.0f;
        matrices.translate(client.getHandOffsetX() * dir, client.getHandOffsetY(), client.getHandOffsetZ());
        matrices.scale(client.getHandSize(), client.getHandSize(), client.getHandSize());

        float swing = (float) Math.sin(swingProgress * Math.PI) * client.getSwingMultiplier();
        matrices.translate(0.0f, -swing * 0.04f, 0.0f);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(swing * 8.0f * dir));
    }
}
