package com.dannysaot.colossalmod.mixin;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class TitanMovementMixin {

    /**
     * When the player sends a movement packet and is riding an ArminsColossalEntity,
     * redirect the yaw/pitch to the titan so the titan faces where the player looks.
     */
    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;
        ServerPlayerEntity player = handler.player;

        if (!ArminsColossalTracker.isTransformed(player.getUuid())) return;
        if (!(player.getVehicle() instanceof ArminsColossalEntity titan)) return;

        if (packet.changesLook()) {
            titan.setYaw(packet.getYaw(titan.getYaw()));
            titan.setHeadYaw(packet.getYaw(titan.getHeadYaw()));
            titan.setBodyYaw(packet.getYaw(titan.getBodyYaw()));
        }
    }
}
