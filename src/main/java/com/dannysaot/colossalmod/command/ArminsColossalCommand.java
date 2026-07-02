package com.dannysaot.colossalmod.command;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalTracker;
import com.dannysaot.colossalmod.entity.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

public class ArminsColossalCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("arminscolossal")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ArminsColossalCommand::executeSet)))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ArminsColossalCommand::executeRemove)))
        );
    }

    private static int executeSet(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
            ServerWorld world = player.getServerWorld();

            if (ArminsColossalTracker.isTransformed(player.getUuid())) {
                ctx.getSource().sendError(Text.literal(
                    player.getName().getString() + " is already transformed."));
                return 0;
            }

            // Spawn titan
            ArminsColossalEntity titan = ModEntities.ARMINS_COLOSSAL.create(world);
            if (titan == null) return 0;

            titan.refreshPositionAndAngles(
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), 0);
            titan.setShifterUuid(player.getUuid());

            world.spawnEntity(titan);

            // Start transformation effect
            titan.startTransformation();

            // Make player invisible and mount after brief delay (transformation ticks)
            world.getServer().execute(() -> {
                player.setInvisible(true);
                player.setNoGravity(true);
                player.startRiding(titan, true);
                ArminsColossalTracker.link(player.getUuid(), titan.getUuid());
            });

            ctx.getSource().sendFeedback(() ->
                Text.literal("§a" + player.getName().getString() +
                    " is transforming into Armin's Colossal!"), true);
            return 1;

        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
            ServerWorld world = player.getServerWorld();

            if (!ArminsColossalTracker.isTransformed(player.getUuid())) {
                ctx.getSource().sendError(Text.literal(
                    player.getName().getString() + " is not transformed."));
                return 0;
            }

            UUID titanUuid = ArminsColossalTracker.getTitanUuid(player.getUuid());
            player.stopRiding();
            player.setInvisible(false);
            player.setNoGravity(false);

            if (titanUuid != null) {
                Entity titan = world.getEntity(titanUuid);
                if (titan != null) titan.discard();
            }

            ArminsColossalTracker.unlink(player.getUuid());

            ctx.getSource().sendFeedback(() ->
                Text.literal("§e" + player.getName().getString() +
                    " has reverted from Armin's Colossal."), true);
            return 1;

        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
