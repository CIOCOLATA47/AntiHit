package me.cioco.antihit.mixin;

import me.cioco.antihit.FriendManager;
import me.cioco.antihit.commands.ChangeNameTagColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(LivingEntityRenderer.class)
public abstract class FriendIndicatorMixin {

    @Unique
    private final Map<LivingEntityRenderState, LivingEntity> entityLookup = new ConcurrentHashMap<>();

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void storeEntityReference(LivingEntity target, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        entityLookup.put(state, target);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("RETURN")
    )
    private void appendFriendLabel(LivingEntityRenderState state, MatrixStack stack,
                                   net.minecraft.client.render.command.OrderedRenderCommandQueue renderQueue,
                                   net.minecraft.client.render.state.CameraRenderState camState,
                                   CallbackInfo ci) {

        if (!(state instanceof PlayerEntityRenderState)) return;

        LivingEntity cached = entityLookup.get(state);
        if (!(cached instanceof PlayerEntity player)) return;
        if (!FriendManager.isFriend(player)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate buffers = client.getBufferBuilders().getEntityVertexConsumers();
        int light = state.light;

        drawFriendIndicator(player, stack, buffers, light);
        buffers.draw();
    }

    @Unique
    private void drawFriendIndicator(PlayerEntity player, MatrixStack stack,
                                     VertexConsumerProvider provider, int lightLevel) {

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer renderer = mc.textRenderer;
        String label = "[Friend]";

        stack.push();

        float verticalOffset = player.getHeight() + 0.7f;
        stack.translate(0, verticalOffset, 0);
        var cam = mc.gameRenderer.getCamera();
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        float scaleFactor = 0.021f;
        stack.scale(-scaleFactor, -scaleFactor, scaleFactor);

        Formatting formatting = ChangeNameTagColor.getNameTagColor();
        Integer colorInt = formatting.getColorValue();
        int labelColor = (colorInt != null) ? colorInt | 0xFF000000 : 0xFFFFFFFF;

        renderer.draw(label, -renderer.getWidth(label) / 2f, 0, labelColor, false, stack.peek().getPositionMatrix(), provider, TextRenderer.TextLayerType.SEE_THROUGH, 0x50000000, lightLevel);

        stack.pop();
    }
}