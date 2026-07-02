package com.dannysaot.colossalmod.command;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalNapeEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalTracker;
import com.dannysaot.colossalmod.entity.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

public class BAoTCommand {

    private static final com.mojang.brigadier.suggestion.SuggestionProvider<ServerCommandSource> TITAN_SUGGESTIONS =
        (ctx, builder) -> { builder.suggest("armins_colossal"); return builder.buildFuture(); };

    private static final com.mojang.brigadier.suggestion.SuggestionProvider<ServerCommandSource> BLOODLINE_SUGGESTIONS =
        (ctx, builder) -> { builder.suggest("eldian"); builder.suggest("ackerman"); return builder.buildFuture(); };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("baot")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("shifters")
                    .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("titan", StringArgumentType.word())
                                .suggests(TITAN_SUGGESTIONS)
                                .executes(ctx -> executeShifterSet(ctx,
                                    EntityArgumentType.getPlayer(ctx, "player"),
                                    StringArgumentType.getString(ctx, "titan"))))))
                    .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> executeShifterRemove(ctx,
                                EntityArgumentType.getPlayer(ctx, "player")))))
                    .then(CommandManager.literal("fill")
                        .then(CommandManager.argument("titan", StringArgumentType.word())
                            .suggests(TITAN_SUGGESTIONS)
                            .executes(ctx -> executeShifterFill(ctx,
                                StringArgumentType.getString(ctx, "titan"))))))
                .then(CommandManager.literal("bloodline")
                    .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("bloodline", StringArgumentType.word())
                                .suggests(BLOODLINE_SUGGESTIONS)
                                .executes(ctx -> executeBloodlineSet(ctx,
                                    EntityArgumentType.getPlayer(ctx, "player"),
                                    StringArgumentType.getString(ctx, "bloodline"))))))
                    .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> executeBloodlineRemove(ctx,
                                EntityArgumentType.getPlayer(ctx, "player"))))))
        );
    }

    private static int executeShifterSet(CommandContext<ServerCommandSource> ctx,
                                          ServerPlayerEntity player, String titan) {
        if (ArminsColossalTracker.isTransformed(player.getUuid())) {
            ctx.getSource().sendError(Text.literal(player.getName().getString() + " is already transformed."));
            return 0;
        }
        switch (titan.toLowerCase()) {
            case "armins_colossal" -> transformIntoArminsColossal(player);
            default -> { ctx.getSource().sendError(Text.literal("Unknown titan: " + titan)); return 0; }
        }
        ctx.getSource().sendFeedback(() ->
            Text.literal("§a" + player.getName().getString() + " is transforming into " + titan + "!"), true);
        return 1;
    }

    private static int executeShifterRemove(CommandContext<ServerCommandSource> ctx,
                                             ServerPlayerEntity player) {
        if (!ArminsColossalTracker.isTransformed(player.getUuid())) {
            ctx.getSource().sendError(Text.literal(player.getName().getString() + " is not transformed."));
            return 0;
        }
        revertTransformation(player);
        ctx.getSource().sendFeedback(() ->
            Text.literal("§e" + player.getName().getString() + " has reverted."), true);
        return 1;
    }

    private static int executeShifterFill(CommandContext<ServerCommandSource> ctx, String titan) {
        ctx.getSource().getServer().getPlayerManager().getPlayerList().forEach(p -> {
            if (!ArminsColossalTracker.isTransformed(p.getUuid())) {
                if (titan.equalsIgnoreCase("armins_colossal")) transformIntoArminsColossal(p);
            }
        });
        ctx.getSource().sendFeedback(() ->
            Text.literal("§aTransformed all online players into " + titan + "!"), true);
        return 1;
    }

    private static int executeBloodlineSet(CommandContext<ServerCommandSource> ctx,
                                            ServerPlayerEntity player, String bloodline) {
        ctx.getSource().sendFeedback(() ->
            Text.literal("§7[Bloodline] Set " + player.getName().getString() +
                "'s bloodline to " + bloodline + " (coming soon)."), false);
        return 1;
    }

    private static int executeBloodlineRemove(CommandContext<ServerCommandSource> ctx,
                                               ServerPlayerEntity player) {
        ctx.getSource().sendFeedback(() ->
            Text.literal("§7[Bloodline] Removed bloodline from " +
                player.getName().getString() + " (coming soon)."), false);
        return 1;
    }

    // ---- Helpers ----

    public static void transformIntoArminsColossal(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        // Spawn titan
        ArminsColossalEntity titan = ModEntities.ARMINS_COLOSSAL.create(world);
        if (titan == null) return;
        titan.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);
        titan.setShifterUuid(player.getUuid());
        world.spawnEntity(titan);
        titan.startTransformation();

        // Spawn nape entity at titan's neck
        ArminsColossalNapeEntity nape = ModEntities.ARMINS_COLOSSAL_NAPE.create(world);
        if (nape != null) {
            nape.setParentUuid(titan.getUuid());
            double yawRad = Math.toRadians(titan.getYaw());
            nape.setPos(
                titan.getX() - Math.sin(yawRad) * ArminsColossalNapeEntity.NAPE_OFFSET_BACK,
                titan.getY() + ArminsColossalNapeEntity.NAPE_HEIGHT_OFFSET,
                titan.getZ() + Math.cos(yawRad) * ArminsColossalNapeEntity.NAPE_OFFSET_BACK
            );
            world.spawnEntity(nape);
            ArminsColossalTracker.linkNape(player.getUuid(), nape.getUuid());
        }

        // Make player invisible and mount
        player.setInvisible(true);
        player.setNoGravity(true);
        player.startRiding(titan, true);

        ArminsColossalTracker.assign(player.getUuid());
        ArminsColossalTracker.link(player.getUuid(), titan.getUuid());
    }

    public static void revertTransformation(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        // Kill titan
        UUID titanUuid = ArminsColossalTracker.getTitanUuid(player.getUuid());
        player.stopRiding();
        player.setInvisible(false);
        player.setNoGravity(false);
        if (titanUuid != null) {
            Entity titan = world.getEntity(titanUuid);
            if (titan != null) titan.discard();
        }

        // Kill nape
        UUID napeUuid = ArminsColossalTracker.getNapeUuid(player.getUuid());
        if (napeUuid != null) {
            Entity nape = world.getEntity(napeUuid);
            if (nape != null) nape.discard();
        }

        ArminsColossalTracker.unlink(player.getUuid());
        ArminsColossalTracker.unlinkNape(player.getUuid());
        ArminsColossalTracker.unassign(player.getUuid());
    }
}
