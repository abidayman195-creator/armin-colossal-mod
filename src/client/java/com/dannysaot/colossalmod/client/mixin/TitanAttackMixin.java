package com.dannysaot.colossalmod.client.mixin;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.network.AttackPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class TitanAttackMixin {

    /**
     * Intercept left-click (attack) while riding Armin's Colossal → send punch packet.
     */
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof ArminsColossalEntity)) return;

        // Send punch
        ClientPlayNetworking.send(new AttackPayload(0));
        ci.cancel(); // prevent normal attack logic
    }

    /**
     * Intercept right-click while riding → send kick packet.
     */
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof ArminsColossalEntity)) return;

        // Send kick
        ClientPlayNetworking.send(new AttackPayload(2));
        ci.cancel();
    }
}
