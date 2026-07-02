package com.dannysaot.colossalmod.network;

import com.dannysaot.colossalmod.ColossalMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent client→server when the player presses attack/steam/kick while transformed.
 * type: 0=punch, 1=steam, 2=kick
 */
public record AttackPayload(int type) implements CustomPayload {

    public static final Id<AttackPayload> TYPE =
        new Id<>(Identifier.of(ColossalMod.MOD_ID, "attack"));

    public static final PacketCodec<RegistryByteBuf, AttackPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.INTEGER, AttackPayload::type,
            AttackPayload::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
