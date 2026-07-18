package net.cf.vintageefurn.client.renderer.beam;


import com.mojang.blaze3d.vertex.PoseStack;

import net.cf.vintageefurn.BeamsConfig;
import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.attachment.BeamPlacementData;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.items.BeamItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class BeamGhostRenderer {

    private BeamGhostRenderer() {}

    private static final float BASE_PITCH = 1.0F;
    private static final float PITCH_STEP = 0.1F;
    private static final float MIN_PITCH = BASE_PITCH;
    private static final float MAX_PITCH = 2.0F;

    private static int lastQuadCount = -1;
    private static float currentPitch = BASE_PITCH;

    private static int computeQuadCount(double length) {
        int fullSegments = (int) Math.floor(length + 1e-4);
        double remainder = length - fullSegments;
        if (remainder < 1e-4) remainder = 0;
        return 2 + (fullSegments * 4) + (remainder > 0 ? 4 : 0);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BeamItem beamItem))
            return;

        BeamPlacementData pending = BeamsAttachments.get(player.getUUID());

        if (pending == null) {
            lastQuadCount = -1;
            currentPitch = BASE_PITCH;
            return;
        }

        if (mc.level == null)
            return;

        HitResult hit = BeamItem.raytraceSkippingAnchors(mc.level, player);
        Vec3 lookTarget = hit.getLocation();

        Vec3 startWorld = pending.toWorldVec3();

        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            int[] uvd = BeamPlacementData.snapHit(
                    bhr.getLocation(),
                    bhr.getBlockPos(),
                    bhr.getDirection()
            );

            lookTarget = BeamPlacementData.snapToWorld(
                    bhr.getBlockPos(),
                    bhr.getDirection(),
                    uvd[0], uvd[1], uvd[2]
            );
        }

        int currentQuadCount = computeQuadCount(startWorld.distanceTo(lookTarget));

        if (lastQuadCount == -1) {
            lastQuadCount = currentQuadCount;
        } else if (currentQuadCount != lastQuadCount) {

            if (currentQuadCount > lastQuadCount) {
                currentPitch = Math.min(MAX_PITCH, currentPitch + PITCH_STEP);
            } else {
                currentPitch = Math.max(MIN_PITCH, currentPitch - PITCH_STEP);
            }

            lastQuadCount = currentQuadCount;

            if (BeamsConfig.BEAM_PITCH_SOUND.get()) {
                mc.level.playLocalSound(
                        lookTarget.x,
                        lookTarget.y,
                        lookTarget.z,
                        SoundEvents.WOOD_PLACE,
                        SoundSource.BLOCKS,
                        0.4F,
                        currentPitch,
                        false
                );
            }
        }

        TextureAtlasSprite sprite = BeamTextures.plankSprite(pending.woodType());
        if (sprite == null)
            return;

        Vec3 renderStart = startWorld;
        Vec3 renderTarget = lookTarget;



        Vec3 cam = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        boolean ghost = BeamsConfig.GHOST_PREVIEW.get();
        RenderType renderType = ghost ? RenderType.translucent() : RenderType.cutoutMipped();
        int alpha = ghost ? 100 : 255;

        BeamPrismRenderer.render(
                poseStack,
                buffers.getBuffer(renderType),
                renderStart,
                renderTarget,
                Vec3.ZERO,
                mc.level,
                sprite,
                255,
                255,
                255,
                alpha
        );

        buffers.endBatch(renderType);
        poseStack.popPose();
    }
}
