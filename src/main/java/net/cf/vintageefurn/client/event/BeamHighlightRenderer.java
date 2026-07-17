package net.cf.vintageefurn.client.event;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.cf.vintageefurn.blocks.beam.BeamAnchorBlock;
import net.cf.vintageefurn.client.renderer.beam.BeamGeometry;
import net.cf.vintageefurn.compat.sable.SableCompat;
import net.cf.vintageefurn.compat.sable.SableRayCompat;
import net.cf.vintageefurn.targeting.BeamTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public static final class beamhighlightrenderer {

    private beamhighlightrenderer() {}

    private static final int OUTLINE_R = 0;
    private static final int OUTLINE_G = 0;
    private static final int OUTLINE_B = 0;
    private static final int OUTLINE_A = 102;

    private static final float OUTLINE_SCALE = 1.001f;

    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;

        if (level == null || player == null) {
            return;
        }

        BlockPos pos = event.getTarget().getBlockPos();

        if (level.getBlockState(pos).getBlock() instanceof BeamAnchorBlock) {
            event.setCanceled(true);
            return;
        }

        if (beamTargetedByOwnRaycast(level, player, mc.getFrameTime())) {
            event.setCanceled(true);
        }
    }

    private static boolean beamTargetedByOwnRaycast(Level level, Player player, float partialTick) {
        Vec3 rayOrigin = player.getEyePosition(partialTick);
        Vec3 rayLook = player.getViewVector(partialTick);

        // Vanilla reach in Forge 1.20.1
        double reach = 4.5D;

        Vec3 rayEnd = rayOrigin.add(rayLook.scale(reach));

        return BeamTargeting.findClient(level, rayOrigin, rayEnd).isPresent();
    }

    @SubscribeEvent
    public static void onRenderGuiPre(net.minecraftforge.client.event.RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;

        if (level == null || player == null) {
            return;
        }

        float partialTick = mc.getFrameTime();

        Vec3 rayOrigin = player.getEyePosition(partialTick);
        Vec3 rayLook = player.getViewVector(partialTick);

        double reach = 4.5D;

        Vec3 rayEnd = rayOrigin.add(rayLook.scale(reach));

        Optional<BeamTargeting.Result> found =
                BeamTargeting.findClient(level, rayOrigin, rayEnd);

        if (found.isEmpty()) {
            return;
        }

        Direction face = Direction.getNearest(
                -rayLook.x,
                -rayLook.y,
                -rayLook.z
        );

        mc.hitResult = new BlockHitResult(
                found.get().hitPoint(),
                face,
                found.get().pos(),
                false
        );
    }
}
@SubscribeEvent
public static void onRenderLevelStage(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
        return;
    }

    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;

    if (level == null || mc.player == null) {
        return;
    }

    if (mc.options.hideGui || mc.player.isSpectator()) {
        return;
    }

    float partialTick = event.getPartialTick();

    Vec3 rayOrigin = mc.player.getEyePosition(partialTick);
    Vec3 rayLook = mc.player.getViewVector(partialTick);

    double reach = 4.5D;

    Vec3 rayEnd = rayOrigin.add(rayLook.scale(reach));

    Optional<BeamTargeting.Result> found =
            BeamTargeting.findClient(level, rayOrigin, rayEnd);

    if (found.isEmpty()) {
        return;
    }

    BeamBlockEntity.BeamLink targeted = found.get().link();

    if (targeted.otherVec == null) {
        return;
    }

    Vec3 start = targeted.isStart ? targeted.selfVec : targeted.otherVec;
    Vec3 end = targeted.isStart ? targeted.otherVec : targeted.selfVec;

    if (SableCompat.isLoaded()) {
        BlockPos beamPos = found.get().pos();
        start = SableRayCompat.toRenderSpace(level, beamPos, partialTick, start);
        end = SableRayCompat.toRenderSpace(level, beamPos, partialTick, end);
    }

    BeamGeometry.Corners corners =
            BeamGeometry.computeCorners(start, end, OUTLINE_SCALE);

    if (corners == null) {
        return;
    }

    Vec3 cam = event.getCamera().getPosition();

    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    poseStack.translate(-cam.x, -cam.y, -cam.z);

    RenderType renderType = RenderType.lines();
    MultiBufferSource.BufferSource buffers =
            mc.renderBuffers().bufferSource();

    VertexConsumer consumer = buffers.getBuffer(renderType);

    renderOutlineBox(poseStack, consumer, corners);

    buffers.endBatch(renderType);

    poseStack.popPose();
}

private static void renderOutlineBox(
        PoseStack poseStack,
        VertexConsumer consumer,
        BeamGeometry.Corners c
) {
    PoseStack.Pose pose = poseStack.last();

    edge(consumer, pose, c.startBL(), c.startBR());
    edge(consumer, pose, c.startBR(), c.startTR());
    edge(consumer, pose, c.startTR(), c.startTL());
    edge(consumer, pose, c.startTL(), c.startBL());

    edge(consumer, pose, c.endBL(), c.endBR());
    edge(consumer, pose, c.endBR(), c.endTR());
    edge(consumer, pose, c.endTR(), c.endTL());
    edge(consumer, pose, c.endTL(), c.endBL());

    edge(consumer, pose, c.startBL(), c.endBL());
    edge(consumer, pose, c.startBR(), c.endBR());
    edge(consumer, pose, c.startTR(), c.endTR());
    edge(consumer, pose, c.startTL(), c.endTL());
}

private static void edge(
        VertexConsumer consumer,
        PoseStack.Pose pose,
        Vec3 a,
        Vec3 b
) {
    float dx = (float) (b.x - a.x);
    float dy = (float) (b.y - a.y);
    float dz = (float) (b.z - a.z);

    float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

    if (len > 1.0e-6F) {
        dx /= len;
        dy /= len;
        dz /= len;
    }

    consumer.vertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
            .color(OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A)
            .normal(pose.normal(), dx, dy, dz)
            .endVertex();

    consumer.vertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
            .color(OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A)
            .normal(pose.normal(), dx, dy, dz)
            .endVertex();
}

