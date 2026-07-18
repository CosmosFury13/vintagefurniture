package net.cf.vintageefurn.blocks.beam;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.cf.vintageefurn.registry.BeamsBlockEntities;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BeamAnchorBlock extends BaseEntityBlock {

    public static final BooleanProperty IS_START = BooleanProperty.create("is_start");

    private static final VoxelShape FALLBACK_SHAPE = Block.box(6, 6, 6, 10, 10, 10);

    private static final double HIT_HALF_WIDTH = 0.22;

    private static final double HIT_OFFSET = 0.5;

    public BeamAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(IS_START, true));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_START);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return computeHitShape(level, pos);
    }

    private static VoxelShape computeHitShape(BlockGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BeamBlockEntity be) || be.getLinks().isEmpty()) {
            return FALLBACK_SHAPE;
        }

        Vec3 origin = Vec3.atLowerCornerOf(pos);

        VoxelShape shape = Shapes.empty();
        for (BeamBlockEntity.BeamLink link : be.getLinks()) {
            Vec3 center = hitBoxCenter(link);
            if (center == null) continue;

            AABB box = boxAround(center);
            shape = Shapes.or(shape, Shapes.box(
                    box.minX - origin.x, box.minY - origin.y, box.minZ - origin.z,
                    box.maxX - origin.x, box.maxY - origin.y, box.maxZ - origin.z));
        }

        return shape;
    }
    private static @Nullable Vec3 hitBoxCenter(BeamBlockEntity.BeamLink link) {
        Vec3 far = link.otherVec;
        if (far == null) return null;

        Vec3 vertex = link.selfVec;
        Vec3 diff = far.subtract(vertex);
        double len = diff.length();
        if (len < 1.0e-6) return vertex;

        double t = Math.min(HIT_OFFSET, len * 0.5);
        return vertex.add(diff.scale(t / len));
    }

    private static AABB boxAround(Vec3 center) {
        return new AABB(
                center.x - HIT_HALF_WIDTH, center.y - HIT_HALF_WIDTH, center.z - HIT_HALF_WIDTH,
                center.x + HIT_HALF_WIDTH, center.y + HIT_HALF_WIDTH, center.z + HIT_HALF_WIDTH);
    }

    private static final double CAPSULE_RADIUS = 0.28;

    public static Optional<Vec3> testLinkHit(BeamBlockEntity.BeamLink link, Vec3 rayOrigin, Vec3 rayEnd) {
        Vec3 segStart = link.selfVec;
        Vec3 segEnd = link.otherVec;
        if (segStart == null || segEnd == null) return Optional.empty();
        if (segStart.distanceToSqr(segEnd) < 1.0e-8) return Optional.empty();

        Vec3[] closest = closestPointsBetweenSegments(rayOrigin, rayEnd, segStart, segEnd);
        Vec3 onRay = closest[0];
        Vec3 onBeam = closest[1];

        if (onRay.distanceToSqr(onBeam) > CAPSULE_RADIUS * CAPSULE_RADIUS) return Optional.empty();
        return Optional.of(onRay);
    }
    public static @Nullable BeamBlockEntity.BeamLink findTargetedLink(
            BlockPos pos, BeamBlockEntity be, Vec3 rayOrigin, Vec3 rayEnd
    ) {
        BeamBlockEntity.BeamLink best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BeamBlockEntity.BeamLink link : be.getLinks()) {
            Optional<Vec3> hit = testLinkHit(link, rayOrigin, rayEnd);
            if (hit.isEmpty()) continue;

            double distSq = rayOrigin.distanceToSqr(hit.get());
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = link;
            }
        }

        return best;
    }
    private static Vec3[] closestPointsBetweenSegments(Vec3 p1, Vec3 q1, Vec3 p2, Vec3 q2) {
        Vec3 d1 = q1.subtract(p1);
        Vec3 d2 = q2.subtract(p2);
        Vec3 r = p1.subtract(p2);

        double a = d1.dot(d1);
        double e = d2.dot(d2);
        double f = d2.dot(r);

        double s, t;
        final double EPS = 1.0e-10;

        if (a <= EPS && e <= EPS) {
            return new Vec3[]{p1, p2};
        }

        if (a <= EPS) {
            s = 0.0;
            t = clamp01(f / e);
        } else {
            double c = d1.dot(r);

            if (e <= EPS) {
                t = 0.0;
                s = clamp01(-c / a);
            } else {
                double b = d1.dot(d2);
                double denom = a * e - b * b;

                s = denom > EPS ? clamp01((b * f - c * e) / denom) : 0.0;
                t = (b * s + f) / e;

                if (t < 0.0) {
                    t = 0.0;
                    s = clamp01(-c / a);
                } else if (t > 1.0) {
                    t = 1.0;
                    s = clamp01((b - c) / a);
                }
            }
        }

        Vec3 c1 = p1.add(d1.scale(s));
        Vec3 c2 = p2.add(d2.scale(t));
        return new Vec3[]{c1, c2};
    }
    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BeamBlockEntity(BeamsBlockEntities.BEAM_ANCHOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide
                && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof BeamBlockEntity be) {
            destroyRemainingLinks(level, pos, be);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
    private static void destroyRemainingLinks(Level level, BlockPos pos, BeamBlockEntity be) {
        for (BeamBlockEntity.BeamLink link : be.getLinks()) {
            int totalItems = BeamBlockEntity.itemCountFor(level, link);
            Block.popResource(level, pos, new ItemStack(BeamsItems.getBeamItem(link.woodType), totalItems));

            if (link.partnerPos != null
                    && level.getBlockEntity(link.partnerPos) instanceof BeamBlockEntity partnerBE) {

                final BlockPos thisPos = pos;
                final int thisId = link.id;

                partnerBE.findLink(link.partnerLinkId).ifPresent(partnerLink -> {
                    boolean reciprocal = thisPos.equals(partnerLink.partnerPos)
                            && partnerLink.partnerLinkId == thisId;

                    if (reciprocal) {
                        partnerBE.removeLink(link.partnerLinkId);

                        if (partnerBE.hasNoLinks()) {
                            level.removeBlock(link.partnerPos, false);
                        }
                    }
                });
            }
        }
    }
}

