package net.cf.vintageefurn.client.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.network.BeamsNetworking;
import net.cf.vintageefurn.network.beam.BeamCancelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class BeamCancelInputHandler {

    private BeamCancelInputHandler() {
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {

        if (!event.isAttack()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // Only cancel if the player is currently placing a beam
        if (!BeamsAttachments.has(player.getUUID())) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);

        BeamsNetworking.CHANNEL.sendToServer(new BeamCancelPayload());
    }
}
