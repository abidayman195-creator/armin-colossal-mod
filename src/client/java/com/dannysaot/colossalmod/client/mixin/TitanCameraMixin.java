package com.dannysaot.colossalmod.client.mixin;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class TitanCameraMixin {

    /**
     * When the local player is riding Armin's Colossal, anchor the camera
     * to follow the titan's head bone position instead of the player's body.
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity,
                                boolean thirdPerson, boolean inverseView,
                                float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof ArminsColossalEntity titan)) return;

        // Position camera at titan head level (roughly 85% up the titan's height)
        Camera camera = (Camera)(Object)this;
        double headY = titan.getY() + titan.getHeight() * 0.85;
        camera.setPos(titan.getX(), headY, titan.getZ());
    }
}
