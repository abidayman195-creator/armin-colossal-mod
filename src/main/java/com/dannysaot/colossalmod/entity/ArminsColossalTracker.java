package com.dannysaot.colossalmod.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArminsColossalTracker {

    private static final Set<UUID> assignedPlayers  = new HashSet<>();
    private static final Map<UUID, UUID> playerToTitan = new HashMap<>();
    private static final Map<UUID, UUID> playerToNape  = new HashMap<>();

    public static void assign(UUID p)   { assignedPlayers.add(p); }
    public static void unassign(UUID p) { assignedPlayers.remove(p); }
    public static boolean hasAssignedTitan(UUID p) { return assignedPlayers.contains(p); }

    public static void link(UUID player, UUID titan) { playerToTitan.put(player, titan); }
    public static UUID getTitanUuid(UUID player)     { return playerToTitan.get(player); }
    public static boolean isTransformed(UUID player) { return playerToTitan.containsKey(player); }
    public static void unlink(UUID player)           { playerToTitan.remove(player); }

    public static void linkNape(UUID player, UUID nape) { playerToNape.put(player, nape); }
    public static UUID getNapeUuid(UUID player)         { return playerToNape.get(player); }
    public static void unlinkNape(UUID player)          { playerToNape.remove(player); }
}
