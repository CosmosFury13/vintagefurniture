package net.cf.vintageefurn.client.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.network.BeamsNetworking;
import net.cf.vintageefurn.network.beam.BeamBreakRequestPayload;
import net.cf.vintageefurn.targeting.BeamTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class BeamBreakInputHandler {

    private BeamBreakInputHandler() {
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {

        if (!event.isAttack()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        // Player is currently placing a beam
        if (BeamsAttachments.has(player.getUUID())) {
            return;
        }

        float partialTick = mc.getFrameTime();

        Vec3 rayOrigin = player.getEyePosition(partialTick);
        Vec3 rayLook = player.getViewVector(partialTick);

        // Vanilla reach distance in Forge 1.20.1
        double reach = 4.5D;

        Vec3 rayEnd = rayOrigin.add(rayLook.scale(reach));

        if (BeamTargeting.findClient(mc.level, rayOrigin, rayEnd).isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(true);

        if (player.isCreative()) {
            BeamsNetworking.CHANNEL.sendToServer(new BeamBreakRequestPayload());
        }
    }
}
