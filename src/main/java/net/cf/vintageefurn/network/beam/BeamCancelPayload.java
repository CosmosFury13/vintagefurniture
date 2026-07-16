package net.cf.vintageefurn.network.beam;

import net.cf.vintageefurn.items.BeamItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeamCancelPayload {

    public BeamCancelPayload() {
    }

    public BeamCancelPayload(FriendlyByteBuf buf) {
        // No data to read
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to write
    }

    public static void handle(BeamCancelPayload message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                BeamItem.cancelPending(player);
            }
        });

        ctx.setPacketHandled(true);
    }
}
