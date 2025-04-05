package me.cioco.antihit.mixin;

import me.cioco.antihit.FriendManager;

import me.cioco.antihit.command.ChangeNameTagColor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {

    @ModifyArgs(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    protected void renderNameTagColor(Args args) {
        PlayerEntityRenderState entity = (PlayerEntityRenderState) args.get(0);
        Text originalText = (Text) args.get(1);
        boolean isFriend = FriendManager.isFriend(entity);

        Formatting color = ChangeNameTagColor.getNameTagColor();

        if (isFriend) {
            Text modifiedText = Text.translatable("").append(originalText).styled(style -> style.withColor(color));
            args.set(1, modifiedText);
        } else {
            args.set(1, originalText);
        }
    }
}
