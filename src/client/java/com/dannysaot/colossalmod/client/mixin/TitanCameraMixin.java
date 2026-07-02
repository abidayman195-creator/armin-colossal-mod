package com.dannysaot.colossalmod.client.mixin;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class TitanCameraMixin {

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity,
                                boolean thirdPerson, boolean inverseView,
                                float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof ArminsColossalEntity titan)) return;

        double headY = titan.getY() + titan.getHeight() * 0.85;
        setPos(titan.getX(), headY, titan.getZ());
    }
}