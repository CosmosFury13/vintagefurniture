package net.cf.vintageefurn.network.beam;

import net.cf.vintageefurn.event.BeamBreakHandler;
import net.cf.vintageefurn.targeting.BeamTargeting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeamBreakRequestPayload {

    public BeamBreakRequestPayload() {
    }

    public BeamBreakRequestPayload(FriendlyByteBuf buf) {
        // No data to read
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to write
    }

    public static void handle(BeamBreakRequestPayload message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) {
                return;
            }

            Level level = sp.level();

            double reach = 4.5D; // Forge 1.20.1 has no blockInteractionRange()
            Vec3 rayOrigin = sp.getEyePosition(1.0F);
            Vec3 rayEnd = rayOrigin.add(sp.getViewVector(1.0F).scale(reach));

            BeamTargeting.findServer(level, rayOrigin, rayEnd)
                    .ifPresent(result ->
                            BeamBreakHandler.breakOneLinkAt(sp, level, result.pos()));
        });

        ctx.setPacketHandled(true);
    }
}