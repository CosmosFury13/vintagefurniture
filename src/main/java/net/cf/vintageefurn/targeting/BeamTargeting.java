package net.cf.vintageefurn.targeting;

import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;

public final class BeamTargeting {

    private BeamTargeting() {
    }

    public record Result(
            BlockPos pos,
            BeamBlockEntity blockEntity,
            BeamBlockEntity.BeamLink link,
            Vec3 hitPoint
    ) {
    }

    private static final double OCCLUSION_EPSILON = 0.01;

    public static Optional<Result> find(
            Level level,
            Vec3 rayOrigin,
            Vec3 rayEnd,
            Iterable<BeamBlockEntity> candidates
    ) {
        Result best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BeamBlockEntity be : candidates) {
            if (be.isRemoved()) continue;

            BlockPos pos = be.getBlockPos();
            Level beLevel = be.getLevel();
            if (beLevel == null) continue;

            Vec3 localOrigin = rayOrigin;
            Vec3 localEnd = rayEnd;


            for (BeamBlockEntity.BeamLink link : be.getLinks()) {
                Optional<Vec3> hit = BeamAnchorBlock.testLinkHit(link, localOrigin, localEnd);
                if (hit.isEmpty()) continue;

                double distSq = localOrigin.distanceToSqr(hit.get());

                if (distSq >= bestDistSq) continue;
                if (isOccluded(beLevel, localOrigin, hit.get())) continue;

                bestDistSq = distSq;
                best = new Result(pos, be, link, hit.get());
            }
        }

        return Optional.ofNullable(best);
    }

    private static boolean isOccluded(Level level, Vec3 from, Vec3 to) {
        ClipContext ctx = new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        );

        BlockHitResult result = level.clip(ctx);

        if (result.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        double solidDistSq = from.distanceToSqr(result.getLocation());
        double targetDistSq = from.distanceToSqr(to);

        return solidDistSq + OCCLUSION_EPSILON < targetDistSq;
    }

    private static Level lastClientLevel;
    private static Vec3 lastClientOrigin;
    private static Vec3 lastClientEnd;
    private static long lastClientTopologyGen = -1;
    private static Optional<Result> lastClientResult = Optional.empty();

    public static Optional<Result> findClient(Level level, Vec3 rayOrigin, Vec3 rayEnd) {
        long topologyGen = BeamBlockEntity.topologyGeneration();

        if (level == lastClientLevel
                && rayOrigin.equals(lastClientOrigin)
                && rayEnd.equals(lastClientEnd)
                && topologyGen == lastClientTopologyGen) {
            return lastClientResult;
        }

        Optional<Result> result = find(
                level,
                rayOrigin,
                rayEnd,
                BeamBlockEntity.clientInstances()
        );

        lastClientLevel = level;
        lastClientOrigin = rayOrigin;
        lastClientEnd = rayEnd;
        lastClientTopologyGen = topologyGen;
        lastClientResult = result;

        return result;
    }

    public static Optional<Result> findServer(Level level, Vec3 rayOrigin, Vec3 rayEnd) {
        return find(
                level,
                rayOrigin,
                rayEnd,
                BeamBlockEntity.serverInstances(level)
        );
    }
}
