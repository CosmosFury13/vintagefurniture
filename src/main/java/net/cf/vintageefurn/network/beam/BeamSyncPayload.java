package net.cf.vintageefurn.network.beam;

import net.cf.vintageefurn.attachment.BeamPlacementData;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class BeamSyncPayload {

    private final Optional<BeamPlacementData> data;

    public BeamSyncPayload(Optional<BeamPlacementData> data) {
        this.data = data;
    }

    public BeamSyncPayload(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.data = Optional.of(BeamPlacementData.read(buf));
        } else {
            this.data = Optional.empty();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(data.isPresent());

        if (data.isPresent()) {
            BeamPlacementData.write(buf, data.get());
        }
    }

    public Optional<BeamPlacementData> getData() {
        return data;
    }

    public static void handle(BeamSyncPayload message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;

            if (player == null) {
                return;
            }

            if (message.data.isPresent()) {
                BeamsAttachments.set(player.getUUID(), message.data.get());
            } else {
                BeamsAttachments.clear(player.getUUID());
            }
        });

        ctx.setPacketHandled(true);
    }
}