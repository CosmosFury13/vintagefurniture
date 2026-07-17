package net.cf.vintageefurn.client.renderer.beam;


import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class BeamGeometry {

    public static final float BEAM_W = 4f / 16f;
    public static final float BEAM_H = 4f / 16f;

    private BeamGeometry() {}

    private static final boolean ROTATE_TEXTURE_90 = true;

    private static float[] uv(float u, float v) {
        return ROTATE_TEXTURE_90 ? new float[]{v, u} : new float[]{u, v};
    }

    private static float[] sideUv(float across, float along, float quarterIndex) {
        float uMin = quarterIndex * 0.25f;
        return uv(uMin + (1f - across) * 0.25f, along);
    }

    public record Quad(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                       float[] uv0, float[] uv1, float[] uv2, float[] uv3) {}

    public record Corners(Vec3 startBL, Vec3 startBR, Vec3 startTR, Vec3 startTL,
                          Vec3 endBL, Vec3 endBR, Vec3 endTR, Vec3 endTL) {}

    public static Corners computeCorners(Vec3 startLocal, Vec3 endLocal, float scale) {
        Vec3 delta = endLocal.subtract(startLocal);
        double length = delta.length();
        if (length < 1e-6) return null;
        Vec3 axis = delta.scale(1.0 / length);

        Vec3 worldUp = Math.abs(axis.y) > 0.99 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 right = axis.cross(worldUp).normalize();
        Vec3 up    = right.cross(axis).normalize();

        float hw = (BEAM_W / 2f) * scale;
        float hh = (BEAM_H / 2f) * scale;

        Vec3 bl = right.scale(-hw).add(up.scale(-hh));
        Vec3 br = right.scale(hw).add(up.scale(-hh));
        Vec3 tr = right.scale(hw).add(up.scale(hh));
        Vec3 tl = right.scale(-hw).add(up.scale(hh));

        return new Corners(
                startLocal.add(bl), startLocal.add(br), startLocal.add(tr), startLocal.add(tl),
                endLocal.add(bl), endLocal.add(br), endLocal.add(tr), endLocal.add(tl)
        );
    }

    public static List<Quad> buildQuads(Vec3 startLocal, Vec3 endLocal) {
        List<Quad> quads = new ArrayList<>();

        Vec3 delta = endLocal.subtract(startLocal);
        double length = delta.length();
        if (length < 1e-6) return quads;
        Vec3 axis = delta.scale(1.0 / length);

        Vec3 worldUp = Math.abs(axis.y) > 0.99 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);

        Vec3 right = axis.cross(worldUp).normalize();
        Vec3 up = right.cross(axis).normalize();

        float hw = BEAM_W / 2f;
        float hh = BEAM_H / 2f;

        Vec3 bl = right.scale(-hw).add(up.scale(-hh));
        Vec3 br = right.scale(hw).add(up.scale(-hh));
        Vec3 tr = right.scale(hw).add(up.scale(hh));
        Vec3 tl = right.scale(-hw).add(up.scale(hh));

        quads.add(new Quad(
                startLocal.add(bl), startLocal.add(br), startLocal.add(tr), startLocal.add(tl),
                uv(0, 0.25f), uv(0.25f, 0.25f), uv(0.25f, 0), uv(0, 0)
        ));
        quads.add(new Quad(
                endLocal.add(tl), endLocal.add(tr), endLocal.add(br), endLocal.add(bl),
                uv(0, 0.25f), uv(0.25f, 0.25f), uv(0.25f, 0), uv(0, 0)
        ));

        int fullSegments = (int) Math.floor(length + 1e-4);
        double remainder = length - fullSegments;
        if (remainder < 1e-4) remainder = 0;

        double t = 0;
        for (int i = 0; i < fullSegments; i++) {
            addSideSegment(quads, startLocal, axis, bl, br, tr, tl, t, t + 1.0, 1.0f);
            t += 1.0;
        }

        if (remainder > 0) {
            addSideSegment(quads, startLocal, axis, bl, br, tr, tl, t, t + remainder, (float) remainder);
        }

        return quads;
    }

    private static void addSideSegment(
            List<Quad> quads,
            Vec3 startLocal,
            Vec3 axis,
            Vec3 bl,
            Vec3 br,
            Vec3 tr,
            Vec3 tl,
            double t0,
            double t1,
            float du
    ) {
        Vec3 p0 = startLocal.add(axis.scale(t0));
        Vec3 p1 = startLocal.add(axis.scale(t1));

        quads.add(new Quad(
                p1.add(bl), p1.add(br), p0.add(br), p0.add(bl),
                sideUv(0, du, 0), sideUv(1, du, 0), sideUv(1, 0, 0), sideUv(0, 0, 0)
        ));

        quads.add(new Quad(
                p0.add(tl), p0.add(tr), p1.add(tr), p1.add(tl),
                sideUv(0, 0, 1), sideUv(1, 0, 1), sideUv(1, du, 1), sideUv(0, du, 1)
        ));

        quads.add(new Quad(
                p0.add(bl), p0.add(tl), p1.add(tl), p1.add(bl),
                sideUv(0, 0, 2), sideUv(1, 0, 2), sideUv(1, du, 2), sideUv(0, du, 2)
        ));

        quads.add(new Quad(
                p1.add(br), p1.add(tr), p0.add(tr), p0.add(br),
                sideUv(0, du, 3), sideUv(1, du, 3), sideUv(1, 0, 3), sideUv(0, 0, 3)
        ));
    }
}
