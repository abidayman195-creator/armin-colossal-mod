package com.dannysaot.colossalmod.network;

import com.dannysaot.colossalmod.ColossalMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent client→server when player presses B to transform/revert.
 * action: 0 = transform, 1 = revert
 */
public record TransformRequestPayload(int action) implements CustomPayload {

    public static final Id<TransformRequestPayload> TYPE =
        new Id<>(Identifier.of(ColossalMod.MOD_ID, "transform_request"));

    public static final PacketCodec<RegistryByteBuf, TransformRequestPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.INTEGER, TransformRequestPayload::action,
            TransformRequestPayload::new
        );

    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
