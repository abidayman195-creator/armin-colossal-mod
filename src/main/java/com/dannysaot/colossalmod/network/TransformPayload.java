package com.dannysaot.colossalmod.network;

import com.dannysaot.colossalmod.ColossalMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent server→client to tell the client to start/stop the transformation effect.
 */
public record TransformPayload(int titanEntityId, boolean transforming) implements CustomPayload {

    public static final Id<TransformPayload> TYPE =
        new Id<>(Identifier.of(ColossalMod.MOD_ID, "transform"));

    public static final PacketCodec<RegistryByteBuf, TransformPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.INTEGER, TransformPayload::titanEntityId,
            PacketCodecs.BOOL,    TransformPayload::transforming,
            TransformPayload::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
