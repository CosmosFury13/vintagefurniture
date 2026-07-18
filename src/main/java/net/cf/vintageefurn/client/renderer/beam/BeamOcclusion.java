package net.cf.vintageefurn.client.renderer.beam;

import net.minecraft.world.phys.Vec3;

public final class BeamOcclusion {

    private BeamOcclusion() {}

    public record Obb(Vec3 center, Vec3 axis, Vec3 right, Vec3 up,
                      double halfLength, double hw, double hh) {}

    public static Obb of(Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        Vec3 axis = length > 1e-9 ? delta.scale(1.0 / length) : new Vec3(1, 0, 0);
        Vec3 worldUp = Math.abs(axis.y) > 0.99 ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
        Vec3 right = axis.cross(worldUp).normalize();
        Vec3 up = right.cross(axis).normalize();
        Vec3 center = start.add(end).scale(0.5);
        return new Obb(center, axis, right, up, length / 2.0,
                BeamGeometry.BEAM_W / 2.0, BeamGeometry.BEAM_H / 2.0);
    }

    private static Vec3 toLocal(Obb box, Vec3 worldPoint) {
        Vec3 d = worldPoint.subtract(box.center());
        return new Vec3(d.dot(box.right()), d.dot(box.up()), d.dot(box.axis()));
    }

    public static boolean containsPoint(Obb box, Vec3 worldPoint) {
        Vec3 l = toLocal(box, worldPoint);
        return Math.abs(l.x) <= box.hw() && Math.abs(l.y) <= box.hh() && Math.abs(l.z) <= box.halfLength();
    }

    public static double[] segmentIntersect(Obb box, Vec3 self, Vec3 other) {
        Vec3 p0 = toLocal(box, self);
        Vec3 p1 = toLocal(box, other);
        Vec3 d = p1.subtract(p0);

        double[] pa = {p0.x, p0.y, p0.z};
        double[] da = {d.x, d.y, d.z};
        double[] half = {box.hw(), box.hh(), box.halfLength()};

        double tmin = 0.0, tmax = 1.0;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(da[i]) < 1e-9) {
                if (Math.abs(pa[i]) > half[i]) return null;
            } else {
                double t1 = (-half[i] - pa[i]) / da[i];
                double t2 = (half[i] - pa[i]) / da[i];
                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);
                if (tmin > tmax) return null;
            }
        }
        return new double[]{tmin, tmax};
    }
}
