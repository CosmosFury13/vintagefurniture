package net.cf.vintageefurn.client.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.network.BeamsNetworking;
import net.cf.vintageefurn.network.beam.BeamBreakRequestPayload;
import net.cf.vintageefurn.targeting.BeamTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class BeamMiningHandler {

    private BeamMiningHandler() {
    }

    private static BlockPos targetPos = null;
    private static int targetLinkId = -1;
    private static float progress = 0.0F;
    private static int ticks = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player == null || level == null || player.isCreative()) {
            reset(level, player);
            return;
        }

        if (BeamsAttachments.has(player.getUUID())) {
            reset(level, player);
            return;
        }

        if (!mc.options.keyAttack.isDown()) {
            reset(level, player);
            return;
        }

        Vec3 rayOrigin = player.getEyePosition(1.0F);
        Vec3 rayLook = player.getViewVector(1.0F);

        // Vanilla reach distance for Forge 1.20.1
        double reach = 4.5D;

        Vec3 rayEnd = rayOrigin.add(rayLook.scale(reach));

        var found = BeamTargeting.findClient(level, rayOrigin, rayEnd);

        if (found.isEmpty()) {
            reset(level, player);
            return;
        }

        BeamTargeting.Result result = found.get();

        if (!result.pos().equals(targetPos) || result.link().id != targetLinkId) {
            reset(level, player);
            targetPos = result.pos();
            targetLinkId = result.link().id;
        }

        BlockState state = level.getBlockState(targetPos);

        if (state.isAir()) {
            reset(level, player);
            return;
        }

        progress += state.getDestroyProgress(player, level, targetPos);

        if (ticks % 4 == 0) {
            SoundType soundType = state.getSoundType();

            mc.getSoundManager().play(new SimpleSoundInstance(
                    soundType.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 8.0F,
                    soundType.getPitch() * 0.5F,
                    SoundInstance.createUnseededRandom(),
                    targetPos
            ));
        }

        ticks++;

        player.swing(InteractionHand.MAIN_HAND);

        level.destroyBlockProgress(
                player.getId(),
                targetPos,
                (int) (progress * 10.0F) - 1
        );

        if (progress >= 1.0F) {
            BeamsNetworking.CHANNEL.sendToServer(new BeamBreakRequestPayload());
            reset(level, player);
        }
    }

    private static void reset(Level level, LocalPlayer player) {
        if (targetPos != null && level != null && player != null) {
            level.destroyBlockProgress(player.getId(), targetPos, -1);
        }

        targetPos = null;
        targetLinkId = -1;
        progress = 0.0F;
        ticks = 0;
    }
}
