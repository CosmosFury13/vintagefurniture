package net.cf.vintageefurn.client.renderer.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class BeamBlockEntityRenderer implements BlockEntityRenderer<BeamBlockEntity> {

    private static final double PUSH_EPSILON = 0.02;

    private static final Map<BeamBlockEntity.BeamLink, LinkCache> RENDER_CACHE = new WeakHashMap<>();

    private static final class LinkCache {
        List<BeamPrismRenderer.CachedQuad> quads;
        boolean needsPushBack;
        long topologyGen = -1;
    }

    public static void invalidateAllCaches() {
        RENDER_CACHE.clear();
    }

    public BeamBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(
            BeamBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (be.getLevel() == null) return;
        if (be.getLinks().isEmpty()) return;

        Vec3 origin = Vec3.atLowerCornerOf(be.getBlockPos());
        long currentTopologyGen = BeamBlockEntity.topologyGeneration();

        Vec3 pushDir = null;

        for (BeamBlockEntity.BeamLink link : be.getLinks()) {
            if (!link.isStart || link.otherVec == null) continue;

            LinkCache cache = RENDER_CACHE.computeIfAbsent(link, k -> new LinkCache());

            boolean needsRebuild = cache.quads == null || cache.topologyGen != currentTopologyGen;

            if (needsRebuild) {
                TextureAtlasSprite sprite = BeamTextures.plankSprite(link.woodType);
                if (sprite == null) continue;

                Vec3 jitter = stableJitter(link.selfVec, link.otherVec, link.woodType);
                Vec3 startLocal = link.selfVec.subtract(origin).add(jitter);
                Vec3 endLocal = link.otherVec.subtract(origin).add(jitter);

                cache.quads = BeamPrismRenderer.buildCache(
                        startLocal, endLocal, origin, be.getLevel(), sprite, 255, 255, 255
                );
                cache.needsPushBack = overlapsNewerBeam(be, link.id, link.selfVec, link.otherVec, link.placedAt);
                cache.topologyGen = currentTopologyGen;
            }

            if (cache.quads.isEmpty()) continue;

            RenderType renderType = RenderType.cutoutMipped();
            var consumer = bufferSource.getBuffer(renderType);

            poseStack.pushPose();
            if (cache.needsPushBack) {
                if (pushDir == null) pushDir = cameraForward().scale(PUSH_EPSILON);
                poseStack.translate(pushDir.x, pushDir.y, pushDir.z);
            }
            BeamPrismRenderer.renderCached(poseStack, consumer, cache.quads, be.getLevel(), 255);
            poseStack.popPose();
        }
    }

    private static Vec3 cameraForward() {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        return new Vec3(look.x(), look.y(), look.z());
    }

    private static final double JITTER_RANGE = 0.006;

    private static Vec3 stableJitter(Vec3 start, Vec3 end, String woodType) {
        long h = Double.doubleToLongBits(start.x) ^ Long.rotateLeft(Double.doubleToLongBits(start.y), 13)
                ^ Long.rotateLeft(Double.doubleToLongBits(start.z), 26)
                ^ Long.rotateLeft(Double.doubleToLongBits(end.x), 7)
                ^ Long.rotateLeft(Double.doubleToLongBits(end.y), 19)
                ^ Long.rotateLeft(Double.doubleToLongBits(end.z), 31)
                ^ ((long) woodType.hashCode() << 32);

        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);

        double jx = (((h & 0xFFFF) / 65535.0) - 0.5) * 2.0 * JITTER_RANGE;
        double jy = ((((h >>> 16) & 0xFFFF) / 65535.0) - 0.5) * 2.0 * JITTER_RANGE;
        double jz = ((((h >>> 32) & 0xFFFF) / 65535.0) - 0.5) * 2.0 * JITTER_RANGE;
        return new Vec3(jx, jy, jz);
    }

    private static boolean overlapsNewerBeam(
            BeamBlockEntity owner, int ownLinkId, Vec3 self, Vec3 other, long placedAt
    ) {
        for (BeamBlockEntity candidate : BeamBlockEntity.clientInstances()) {
            for (BeamBlockEntity.BeamLink link : candidate.getLinks()) {
                if (!link.isStart || link.otherVec == null) continue;
                if (candidate == owner && link.id == ownLinkId) continue;
                if (link.placedAt <= placedAt) continue;

                BeamOcclusion.Obb box = BeamOcclusion.of(link.selfVec, link.otherVec);
                if (BeamOcclusion.segmentIntersect(box, self, other) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(BeamBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }


}