package com.dannysaot.colossalmod.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks transformation state and titan assignments.
 *
 * assignedPlayers: players who have been given the Armin's Colossal power
 *                  (via /baot shifters set) — they can press B to transform.
 * playerToTitan:   players currently transformed, mapped to their titan entity UUID.
 */
public class ArminsColossalTracker {

    private static final Set<UUID> assignedPlayers = new HashSet<>();
    private static final Map<UUID, UUID> playerToTitan = new HashMap<>();

    // ---- Assignment ----

    public static void assign(UUID playerUuid) {
        assignedPlayers.add(playerUuid);
    }

    public static void unassign(UUID playerUuid) {
        assignedPlayers.remove(playerUuid);
    }

    public static boolean hasAssignedTitan(UUID playerUuid) {
        return assignedPlayers.contains(playerUuid);
    }

    // ---- Active transformation ----

    public static void link(UUID playerUuid, UUID titanUuid) {
        playerToTitan.put(playerUuid, titanUuid);
    }

    public static UUID getTitanUuid(UUID playerUuid) {
        return playerToTitan.get(playerUuid);
    }

    public static boolean isTransformed(UUID playerUuid) {
        return playerToTitan.containsKey(playerUuid);
    }

    public static void unlink(UUID playerUuid) {
        playerToTitan.remove(playerUuid);
    }
}
