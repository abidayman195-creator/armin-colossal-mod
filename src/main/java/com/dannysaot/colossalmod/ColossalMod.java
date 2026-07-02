package com.dannysaot.colossalmod;

import com.dannysaot.colossalmod.command.ArminsColossalCommand;
import com.dannysaot.colossalmod.command.BAoTCommand;
import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalTracker;
import com.dannysaot.colossalmod.entity.ModEntities;
import com.dannysaot.colossalmod.network.AttackPayload;
import com.dannysaot.colossalmod.network.TransformPayload;
import com.dannysaot.colossalmod.network.TransformRequestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColossalMod implements ModInitializer {

    public static final String MOD_ID = "colossalmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[ColossalMod] Initializing Armin's Colossal...");

        ModEntities.registerModEntities();

        // Register payloads
        PayloadTypeRegistry.playS2C().register(TransformPayload.TYPE, TransformPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AttackPayload.TYPE, AttackPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TransformRequestPayload.TYPE, TransformRequestPayload.CODEC);

        // Handle attack packets (punch/steam/kick from client)
        ServerPlayNetworking.registerGlobalReceiver(AttackPayload.TYPE, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (!ArminsColossalTracker.isTransformed(player.getUuid())) return;
                if (!(player.getVehicle() instanceof ArminsColossalEntity titan)) return;
                titan.performAttack(payload.type());
            });
        });

        // Handle B key transform request from client
        ServerPlayNetworking.registerGlobalReceiver(TransformRequestPayload.TYPE, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (payload.action() == 0) {
                    // Transform — only if player has the titan assigned
                    if (ArminsColossalTracker.hasAssignedTitan(player.getUuid())
                        && !ArminsColossalTracker.isTransformed(player.getUuid())) {
                        BAoTCommand.transformIntoArminsColossal(player);
                    }
                } else {
                    // Revert
                    if (ArminsColossalTracker.isTransformed(player.getUuid())) {
                        BAoTCommand.revertTransformation(player);
                    }
                }
            });
        });

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BAoTCommand.register(dispatcher);
            ArminsColossalCommand.register(dispatcher); // keep old command too
        });

        LOGGER.info("[ColossalMod] Ready.");
    }
}
