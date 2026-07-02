package com.dannysaot.colossalmod.command;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ArminsColossalTracker;
import com.dannysaot.colossalmod.entity.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * /baot <category> <action> <player> [titan]
 *
 * Categories:
 *   shifters  — titan transformation
 *   bloodline — (placeholder, implemented later)
 *
 * Actions:
 *   set    — apply to player
 *   remove — remove from player
 *   fill   — (placeholder, apply to all online players)
 *
 * Titans (for shifters):
 *   armins_colossal
 *   (more titans can be added here)
 */
public class BAoTCommand {

    private static final SuggestionProvider<ServerCommandSource> CATEGORY_SUGGESTIONS =
        (ctx, builder) -> {
            builder.suggest("shifters");
            builder.suggest("bloodline");
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> ACTION_SUGGESTIONS =
        (ctx, builder) -> {
            builder.suggest("set");
            builder.suggest("remove");
            builder.suggest("fill");
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> TITAN_SUGGESTIONS =
        (ctx, builder) -> {
            builder.suggest("armins_colossal");
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> BLOODLINE_SUGGESTIONS =
        (ctx, builder) -> {
            // Placeholder — bloodlines will be added in a future part
            builder.suggest("eldian");
            builder.suggest("ackerman");
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("baot")
                .requires(src -> src.hasPermissionLevel(2))

                // shifters branch
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

                // bloodline branch (placeholder)
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

    // ---- Shifters ----

    private static int executeShifterSet(CommandContext<ServerCommandSource> ctx,
                                          ServerPlayerEntity player, String titan) {
        if (ArminsColossalTracker.isTransformed(player.getUuid())) {
            ctx.getSource().sendError(Text.literal(
                player.getName().getString() + " is already transformed."));
            return 0;
        }

        switch (titan.toLowerCase()) {
            case "armins_colossal" -> transformIntoArminsColossal(player);
            default -> {
                ctx.getSource().sendError(Text.literal(
                    "Unknown titan: " + titan + ". Available: armins_colossal"));
                return 0;
            }
        }

        ctx.getSource().sendFeedback(() ->
            Text.literal("§a" + player.getName().getString() +
                " is transforming into " + titan + "!"), true);
        return 1;
    }

    private static int executeShifterRemove(CommandContext<ServerCommandSource> ctx,
                                             ServerPlayerEntity player) {
        if (!ArminsColossalTracker.isTransformed(player.getUuid())) {
            ctx.getSource().sendError(Text.literal(
                player.getName().getString() + " is not transformed."));
            return 0;
        }
        revertTransformation(player);
        ctx.getSource().sendFeedback(() ->
            Text.literal("§e" + player.getName().getString() + " has reverted."), true);
        return 1;
    }

    private static int executeShifterFill(CommandContext<ServerCommandSource> ctx, String titan) {
        ctx.getSource().getServer().getPlayerManager().getPlayerList().forEach(p -> {
            if (!ArminsColossalTracker.isTransformed(p.getUuid()) && !ArminsColossalTracker.hasAssignedTitan(p.getUuid())) {
                if (titan.equalsIgnoreCase("armins_colossal")) {
                    transformIntoArminsColossal(p);
                }
            }
        });
        ctx.getSource().sendFeedback(() ->
            Text.literal("§aTransformed all online players into " + titan + "!"), true);
        return 1;
    }

    // ---- Bloodline (placeholder) ----

    private static int executeBloodlineSet(CommandContext<ServerCommandSource> ctx,
                                            ServerPlayerEntity player, String bloodline) {
        // TODO: implement bloodline system in a future part
        ctx.getSource().sendFeedback(() ->
            Text.literal("§7[Bloodline] Set " + player.getName().getString() +
                "'s bloodline to " + bloodline + " (not yet implemented)."), false);
        return 1;
    }

    private static int executeBloodlineRemove(CommandContext<ServerCommandSource> ctx,
                                               ServerPlayerEntity player) {
        // TODO: implement bloodline system in a future part
        ctx.getSource().sendFeedback(() ->
            Text.literal("§7[Bloodline] Removed bloodline from " +
                player.getName().getString() + " (not yet implemented)."), false);
        return 1;
    }

    // ---- Shared helpers ----

    public static void transformIntoArminsColossal(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ArminsColossalEntity titan = ModEntities.ARMINS_COLOSSAL.create(world);
        if (titan == null) return;

        titan.refreshPositionAndAngles(
            player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);
        titan.setShifterUuid(player.getUuid());
        world.spawnEntity(titan);
        titan.startTransformation();

        player.setInvisible(true);
        player.setNoGravity(true);
        player.startRiding(titan, true);
        ArminsColossalTracker.assign(player.getUuid());
        ArminsColossalTracker.link(player.getUuid(), titan.getUuid());
    }

    public static void revertTransformation(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        UUID titanUuid = ArminsColossalTracker.getTitanUuid(player.getUuid());
        player.stopRiding();
        player.setInvisible(false);
        player.setNoGravity(false);
        if (titanUuid != null) {
            Entity titan = world.getEntity(titanUuid);
            if (titan != null) titan.discard();
        }
        ArminsColossalTracker.unlink(player.getUuid());
        ArminsColossalTracker.unassign(player.getUuid());
    }
}
