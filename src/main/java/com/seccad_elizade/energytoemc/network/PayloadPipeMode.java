package com.seccad_elizade.energytoemc.network;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PayloadPipeMode(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PayloadPipeMode> TYPE = new Type<>(EnergyToEmc.loc("pipe_mode"));

    public static final StreamCodec<FriendlyByteBuf, PayloadPipeMode> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PayloadPipeMode::pos,
            PayloadPipeMode::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final PayloadPipeMode data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (level.isLoaded(data.pos()) && level.getBlockEntity(data.pos()) instanceof EmcPipeBlockEntity pipe) {
                pipe.toggleMode();
            }
        });
    }
}