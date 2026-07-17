package net.cf.vintageefurn.client.renderer.beam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class BeamPrismRenderer {

    private BeamPrismRenderer() {}

    public static void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            Vec3 startLocal,
            Vec3 endLocal,
            Vec3 worldOrigin,
            BlockAndTintGetter level,
            TextureAtlasSprite sprite,
            int r, int g, int b, int a
    ) {
        List<CachedQuad> quads = buildCache(startLocal, endLocal, worldOrigin, level, sprite, r, g, b);
        renderCached(poseStack, consumer, quads, level, a);
    }

    public record CachedQuad(
            Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
            float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3,
            float nx, float ny, float nz,
            int cr, int cg, int cb,
            BlockPos basePos
    ) {}

    public static List<CachedQuad> buildCache(
            Vec3 startLocal,
            Vec3 endLocal,
            Vec3 worldOrigin,
            BlockAndTintGetter level,
            TextureAtlasSprite sprite,
            int r, int g, int b
    ) {
        List<CachedQuad> out = new ArrayList<>();

        double axisLength = endLocal.subtract(startLocal).length();
        Vec3 axisDir = axisLength < 1e-6 ? new Vec3(0, 1, 0) : endLocal.subtract(startLocal).scale(1.0 / axisLength);

        for (BeamGeometry.Quad quad : BeamGeometry.buildQuads(startLocal, endLocal)) {
            Vec3[] p = { quad.p0(), quad.p1(), quad.p2(), quad.p3() };
            float[][] uv = { quad.uv0(), quad.uv1(), quad.uv2(), quad.uv3() };

            Vec3 normal = computeNormal(p);
            float nx = (float) normal.x, ny = (float) normal.y, nz = (float) normal.z;

            float shade = level.getShade(nx, ny, nz, true);
            int cr = clamp255((int) (r * shade));
            int cg = clamp255((int) (g * shade));
            int cb = clamp255((int) (b * shade));

            Vec3 cornerCentroidLocal = p[0].add(p[1]).add(p[2]).add(p[3]).scale(0.25);
            double t = Mth.clamp(
                    cornerCentroidLocal.subtract(startLocal).dot(axisDir),
                    0.0,
                    axisLength
            );
            Vec3 centerlineWorld = worldOrigin.add(startLocal).add(axisDir.scale(t));
            BlockPos basePos = BlockPos.containing(centerlineWorld);

            float u0 = sprite.getU(uv[0][0]), v0 = sprite.getV(uv[0][1]);
            float u1 = sprite.getU(uv[1][0]), v1 = sprite.getV(uv[1][1]);
            float u2 = sprite.getU(uv[2][0]), v2 = sprite.getV(uv[2][1]);
            float u3 = sprite.getU(uv[3][0]), v3 = sprite.getV(uv[3][1]);

            out.add(new CachedQuad(
                    p[0], p[1], p[2], p[3],
                    u0, v0, u1, v1, u2, v2, u3, v3,
                    nx, ny, nz,
                    cr, cg, cb,
                    basePos
            ));
        }
        return out;
    }

    public static void renderCached(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<CachedQuad> quads,
            BlockAndTintGetter level,
            int alpha
    ) {
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose poseEntry = poseStack.last();

        for (CachedQuad q : quads) {
            int packedLight = LevelRenderer.getLightColor(level, resolveLightPos(level, q.basePos()));

            consumer.vertex(pose, (float) q.p0().x, (float) q.p0().y, (float) q.p0().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u0(), q.v0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p1().x, (float) q.p1().y, (float) q.p1().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u1(), q.v1())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p2().x, (float) q.p2().y, (float) q.p2().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u2(), q.v2())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p3().x, (float) q.p3().y, (float) q.p3().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u3(), q.v3())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
        }
    }

    public static void renderCachedWithRealLight(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<CachedQuad> quads,
            Level realLevel,
            Matrix4f lightTransform,
            int alpha
    ) {
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose poseEntry = poseStack.last();

        for (CachedQuad q : quads) {
            BlockPos realPos = toRealWorldPos(lightTransform, q.basePos());
            int packedLight = LevelRenderer.getLightColor(realLevel, realPos);

            consumer.vertex(pose, (float) q.p0().x, (float) q.p0().y, (float) q.p0().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u0(), q.v0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p1().x, (float) q.p1().y, (float) q.p1().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u1(), q.v1())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p2().x, (float) q.p2().y, (float) q.p2().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u2(), q.v2())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
            consumer.vertex(pose, (float) q.p3().x, (float) q.p3().y, (float) q.p3().z)
                    .setColor(q.cr(), q.cg(), q.cb(), alpha)
                    .setUv(q.u3(), q.v3())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(poseEntry, q.nx(), q.ny(), q.nz());
        }
    }

    private static BlockPos toRealWorldPos(Matrix4f lightTransform, BlockPos localPos) {
        Vector4f v = new Vector4f(localPos.getX() + 0.5f, localPos.getY() + 0.5f, localPos.getZ() + 0.5f, 1f);
        v.mul(lightTransform);
        return BlockPos.containing(v.x(), v.y(), v.z());
    }

    private static BlockPos resolveLightPos(BlockAndTintGetter level, BlockPos basePos) {
        if (level.getBlockState(basePos).getLightBlock(level, basePos) < 15) {
            return basePos;
        }
        BlockPos[] neighbors = {
                basePos.north(), basePos.south(), basePos.east(), basePos.west(),
                basePos.above(), basePos.below()
        };
        for (BlockPos candidate : neighbors) {
            if (level.getBlockState(candidate).getLightBlock(level, candidate) < 15) {
                return candidate;
            }
        }
        return basePos;
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static Vec3 computeNormal(Vec3[] p) {
        Vec3 a = p[1].subtract(p[0]);
        Vec3 b = p[2].subtract(p[0]);
        Vec3 n = a.cross(b);
        double len = n.length();
        if (len < 1e-6) return new Vec3(0, 1, 0);
        return n.scale(1.0 / len);
    }
}
