package net.cf.vintageefurn.items;

import net.cf.vintageefurn.attachment.BeamPlacementData;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.cf.vintageefurn.blocks.beam.BeamAnchorBlock;
import net.cf.vintageefurn.network.BeamsNetworking;
import net.cf.vintageefurn.network.beam.BeamSyncPayload;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class BeamItem extends Item {

    private final String woodType;

    public BeamItem(Properties properties, String woodType) {
        super(properties);
        this.woodType = woodType;
    }

    public String getWoodType() {
        return woodType;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(ctx.getPlayer() instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }

        BlockHitResult real = raytraceSkippingAnchors(level, sp);
        if (real.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.FAIL;
        }

        BlockPos clickedPos = real.getBlockPos();
        Direction face = real.getDirection();
        Vec3 hitLoc = real.getLocation();

        int[] uvd = BeamPlacementData.snapHit(hitLoc, clickedPos, face);
        BeamPlacementData clicked = BeamPlacementData.of(
                clickedPos,
                face,
                uvd[0],
                uvd[1],
                uvd[2]
        );

        // Replace this with however your Forge capability exposes the pending placement.
        BeamPlacementData existing = BeamsAttachments.get(sp.getUUID());

        if (existing == null) {
            return placeStart(sp, level, ctx, clicked);
        }

        if (!woodType.equals(existing.woodType())) {
            cancelPending(sp);
            return InteractionResult.FAIL;
        }

        return placeEnd(sp, level, ctx, existing, clicked);
    }

    public static BlockHitResult raytraceSkippingAnchors(Level level, Player player) {
        double reach = 5.0D;
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 to = eye.add(look.scale(reach));

        return clipSkippingAnchors(level, eye, to, player);
    }

    private static BlockHitResult clipSkippingAnchors(Level level, Vec3 from, Vec3 to, Player player) {
        Vec3 start = from;
        Vec3 look = to.subtract(from).normalize();
        double reachSq = start.distanceToSqr(to);

        for (int i = 0; i < 16; i++) {
            BlockHitResult result = level.clip(
                    new ClipContext(
                            from,
                            to,
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            player
                    ));

            if (result.getType() != HitResult.Type.BLOCK
                    || !(level.getBlockState(result.getBlockPos()).getBlock() instanceof BeamAnchorBlock)) {
                return result;
            }

            from = exitCellAlongRay(result.getBlockPos(), result.getLocation(), look);

            if (from.distanceToSqr(start) >= reachSq) {
                break;
            }
        }

        return BlockHitResult.miss(
                to,
                Direction.getNearest(look.x, look.y, look.z),
                BlockPos.containing(to)
        );
    }

    private static Vec3 exitCellAlongRay(BlockPos cellPos, Vec3 from, Vec3 dir) {
        double minX = cellPos.getX();
        double minY = cellPos.getY();
        double minZ = cellPos.getZ();

        double maxX = minX + 1;
        double maxY = minY + 1;
        double maxZ = minZ + 1;

        double t = Double.POSITIVE_INFINITY;

        if (dir.x > 1.0e-8)
            t = Math.min(t, (maxX - from.x) / dir.x);
        else if (dir.x < -1.0e-8)
            t = Math.min(t, (minX - from.x) / dir.x);

        if (dir.y > 1.0e-8)
            t = Math.min(t, (maxY - from.y) / dir.y);
        else if (dir.y < -1.0e-8)
            t = Math.min(t, (minY - from.y) / dir.y);

        if (dir.z > 1.0e-8)
            t = Math.min(t, (maxZ - from.z) / dir.z);
        else if (dir.z < -1.0e-8)
            t = Math.min(t, (minZ - from.z) / dir.z);

        if (!Double.isFinite(t) || t < 0) {
            return from.add(dir.scale(0.05));
        }

        return from.add(dir.scale(t + 1.0e-4));
    }

    private static boolean isValidAnchorTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BeamAnchorBlock) {
            return level.getBlockEntity(pos) instanceof BeamBlockEntity;
        }

        return state.isAir() || state.canBeReplaced();
    }
    private static Optional<BlockPos> resolveFreeAnchorPos(Level level, BlockPos idealPos, Direction face) {
        if (isValidAnchorTarget(level, idealPos)) {
            return Optional.of(idealPos);
        }

        Direction t1;
        Direction t2;

        switch (face.getAxis()) {
            case X -> {
                t1 = Direction.SOUTH;
                t2 = Direction.UP;
            }
            case Y -> {
                t1 = Direction.EAST;
                t2 = Direction.SOUTH;
            }
            default -> {
                t1 = Direction.EAST;
                t2 = Direction.UP;
            }
        }

        for (int outLayer = 0; outLayer <= 2; outLayer++) {
            BlockPos layerBase = idealPos.relative(face, outLayer);

            for (int radius = (outLayer == 0 ? 1 : 0); radius <= 2; radius++) {
                for (int a = -radius; a <= radius; a++) {
                    for (int b = -radius; b <= radius; b++) {
                        if (Math.max(Math.abs(a), Math.abs(b)) != radius) {
                            continue;
                        }

                        BlockPos candidate = layerBase.relative(t1, a).relative(t2, b);

                        if (isValidAnchorTarget(level, candidate)) {
                            return Optional.of(candidate);
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private InteractionResult placeStart(
            ServerPlayer sp,
            Level level,
            UseOnContext ctx,
            BeamPlacementData clicked
    ) {
        Optional<BlockPos> resolved =
                resolveFreeAnchorPos(level, clicked.idealAnchorPos(), clicked.face());

        if (resolved.isEmpty()) {
            return InteractionResult.FAIL;
        }

        BlockPos anchorPos = resolved.get();

        if (!(level.getBlockState(anchorPos).getBlock() instanceof BeamAnchorBlock)) {
            level.setBlock(anchorPos, BeamsBlocks.BEAM_ANCHOR.get().defaultBlockState(), 3);
        }

        if (!(level.getBlockEntity(anchorPos) instanceof BeamBlockEntity startBE)) {
            return InteractionResult.FAIL;
        }

        int linkId = startBE.addStartLink(clicked.toWorldVec3(), woodType);
        BeamPlacementData pending =
                clicked.setPlacement(linkId, anchorPos, woodType);

        level.playSound(
                null,
                anchorPos,
                SoundEvents.WOOD_PLACE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        // Replace with your Forge capability implementation
        BeamsAttachments.set(sp.getUUID(), pending);

        BeamsNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                new BeamSyncPayload(Optional.of(pending)));

        return InteractionResult.CONSUME;
    }

    private InteractionResult placeEnd(
            ServerPlayer sp,
            Level level,
            UseOnContext ctx,
            BeamPlacementData start,
            BeamPlacementData clicked
    ) {
        Vec3 startWorld = start.toWorldVec3();
        Vec3 endWorld = clicked.toWorldVec3();

        if (startWorld.distanceTo(endWorld) < 0.1) {
            return InteractionResult.FAIL;
        }

        boolean infinite = sp.getAbilities().instabuild;

        int itemsNeeded = infinite ? 0 : clicked.itemsRequired(startWorld);

        ItemStack stack = ctx.getItemInHand();

        if (!infinite && stack.getCount() < itemsNeeded) {
            int shortfall = itemsNeeded - stack.getCount();

            Item beamItem = net.cf.vintageefurn.registry.BeamsItems.getBeamItem(woodType);
            Component beamName = new ItemStack(beamItem).getHoverName();

            String key = shortfall == 1
                    ? "item.vintagefurn.beam.not_enough"
                    : "item.vintagefurn.beam.not_enough.plural";

            sp.displayClientMessage(
                    Component.translatable(key, shortfall, beamName),
                    true
            );

            return InteractionResult.FAIL;
        }

        BlockPos startAnchorPos = start.anchorBlockPos();

        if (!(level.getBlockEntity(startAnchorPos) instanceof BeamBlockEntity startBE)
                || startBE.findLink(start.linkId()).isEmpty()) {

            BeamsAttachments.set(sp.getUUID(), null);

            BeamsNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new BeamSyncPayload(Optional.empty()));

            return InteractionResult.FAIL;
        }

        Optional<BlockPos> resolvedEnd =
                resolveFreeAnchorPos(level, clicked.idealAnchorPos(), clicked.face());

        if (resolvedEnd.isEmpty()) {
            return InteractionResult.FAIL;
        }

        BlockPos endAnchorPos = resolvedEnd.get();

        if (!(level.getBlockState(endAnchorPos).getBlock() instanceof BeamAnchorBlock)) {
            level.setBlock(endAnchorPos, BeamsBlocks.BEAM_ANCHOR.get().defaultBlockState(), 3);
        }

        if (!(level.getBlockEntity(endAnchorPos) instanceof BeamBlockEntity endBE)) {
            return InteractionResult.FAIL;
        }

        int endLinkId = endBE.addEndLink(
                endWorld,
                startWorld,
                startAnchorPos,
                start.linkId(),
                woodType
        );

        startBE.completeStartLink(
                start.linkId(),
                endWorld,
                endAnchorPos,
                endLinkId
        );

        level.playSound(
                null,
                endAnchorPos,
                SoundEvents.WOOD_PLACE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        if (itemsNeeded > 0) {
            net.cf.vintageefurn.event.BeamHeldItemWatcher.suppressNextCancel(sp);
            stack.shrink(itemsNeeded);
        }

        BeamsAttachments.set(sp.getUUID(), null);

        BeamsNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                new BeamSyncPayload(Optional.empty()));

        return InteractionResult.CONSUME;
    }

    public static boolean cancelPending(ServerPlayer sp) {
        BeamPlacementData pending = BeamsAttachments.get(sp.getUUID());

        if (pending == null) {
            return false;
        }

        BlockPos startPos = pending.anchorBlockPos();

        if (sp.level().getBlockEntity(startPos) instanceof BeamBlockEntity startBE) {
            startBE.removeLink(pending.linkId());

            if (startBE.hasNoLinks()) {
                sp.level().removeBlock(startPos, false);
            }
        }

        BeamsAttachments.clear(sp.getUUID());

        BeamsNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                new BeamSyncPayload(Optional.empty())
        );

        return true;
    }
}
