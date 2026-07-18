package net.cf.vintageefurn.blockentities;
import net.cf.vintageefurn.registry.BeamsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class BeamBlockEntity extends BlockEntity {

    private final List<BeamLink> links = new ArrayList<>();
    private int nextId = 0;

    private BlockPos capturedAnchorPos = null;

    public BeamBlockEntity(BlockPos pos, BlockState state) {
        super(BeamsBlockEntities.BEAM_ANCHOR.get(), pos, state);
    }

    private static final java.util.Set<BeamBlockEntity> CLIENT_INSTANCES =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    public static Iterable<BeamBlockEntity> clientInstances() {
        return CLIENT_INSTANCES;
    }
    private static final java.util.Set<BeamBlockEntity> SERVER_INSTANCES =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    public static List<BeamBlockEntity> serverInstances(Level level) {
        List<BeamBlockEntity> result = new ArrayList<>();
        for (BeamBlockEntity be : SERVER_INSTANCES) {
            if (be.getLevel() == level) {
                result.add(be);
            }
        }
        return result;
    }
    private static final AtomicLong TOPOLOGY_GEN = new AtomicLong();

    public static long topologyGeneration() {
        return TOPOLOGY_GEN.get();
    }

    private static void bumpTopology() {
        TOPOLOGY_GEN.incrementAndGet();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null) {
            if (level.isClientSide) {
                CLIENT_INSTANCES.add(this);
            } else {
                SERVER_INSTANCES.add(this);
            }
            bumpTopology();
        }
    }
    @Override
    public void setRemoved() {
        super.setRemoved();
        boolean changed = CLIENT_INSTANCES.remove(this);
        changed |= SERVER_INSTANCES.remove(this);
        if (changed) {
            bumpTopology();
        }
    }

    public static final class BeamLink {
        public final int id;
        public final boolean isStart;
        public Vec3 selfVec;
        public @Nullable Vec3 otherVec;
        public @Nullable BlockPos partnerPos;
        public int partnerLinkId;
        public String woodType;

        public long placedAt;

        BeamLink(
                int id,
                boolean isStart,
                Vec3 selfVec,
                @Nullable Vec3 otherVec,
                @Nullable BlockPos partnerPos,
                int partnerLinkId,
                String woodType,
                long placedAt
        ) {
            this.id = id;
            this.isStart = isStart;
            this.selfVec = selfVec;
            this.otherVec = otherVec;
            this.partnerPos = partnerPos;
            this.partnerLinkId = partnerLinkId;
            this.woodType = woodType;
            this.placedAt = placedAt;
        }
    }

    public int addStartLink(Vec3 selfVec, String woodType) {
        int id = nextId++;
        links.add(new BeamLink(id, true, selfVec, null, null, -1, woodType, 0L));
        setChanged();
        syncToClient();
        onTopologyChanged();
        return id;
    }

    public int addEndLink(
            Vec3 selfVec,
            Vec3 otherVec,
            BlockPos partnerPos,
            int partnerLinkId,
            String woodType
    ) {
        int id = nextId++;
        links.add(new BeamLink(id, false, selfVec, otherVec, partnerPos, partnerLinkId, woodType, 0L));
        setChanged();
        syncToClient();
        onTopologyChanged();
        return id;
    }
    public void completeStartLink(int linkId, Vec3 otherVec, BlockPos partnerPos, int partnerLinkId) {
        findLink(linkId).ifPresent(link -> {
            link.otherVec = otherVec;
            link.partnerPos = partnerPos;
            link.partnerLinkId = partnerLinkId;
            link.placedAt = System.currentTimeMillis();
        });
        setChanged();
        syncToClient();
        onTopologyChanged();
    }
    public @Nullable BeamLink removeLink(int linkId) {
        BeamLink removed = null;
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).id == linkId) {
                removed = links.remove(i);
                break;
            }
        }
        if (removed != null) {
            setChanged();
            syncToClient();
            onTopologyChanged();
        }
        return removed;
    }
    public Optional<BeamLink> findLink(int linkId) {
        return links.stream().filter(l -> l.id == linkId).findFirst();
    }

    public List<BeamLink> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public boolean hasNoLinks() {
        return links.isEmpty();
    }
    public void translateLinks(Vec3 vecDelta, BlockPos posDelta) {
        for (BeamLink link : links) {
            link.selfVec = link.selfVec.add(vecDelta);
            if (link.otherVec != null) link.otherVec = link.otherVec.add(vecDelta);
            if (link.partnerPos != null) link.partnerPos = link.partnerPos.offset(posDelta);
        }
        onTopologyChanged();
        setChanged();
        syncToClient();
    }
    public static int itemCountFor(Level level, BeamLink link) {
        if (link.otherVec == null) return 1;
        double dist = link.selfVec.distanceTo(link.otherVec);
        return net.cf.vintageefurn.attachment.BeamPlacementData.costForLength(dist);
    }
    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    private AABB cachedBounds = null;

    private void onTopologyChanged() {
        cachedBounds = null;
        bumpTopology();
    }
    private static final double BOUNDS_PADDING = 0.3;

    public AABB getCachedRenderBounds() {
        if (cachedBounds == null) {
            cachedBounds = computeBounds();
        }
        return cachedBounds;
    }
    private AABB computeBounds() {
        AABB box = null;
        for (BeamLink link : links) {
            if (!link.isStart || link.otherVec == null) continue;
            AABB linkBox = new AABB(link.selfVec, link.otherVec).inflate(BOUNDS_PADDING);
            box = (box == null) ? linkBox : box.minmax(linkBox);
        }
        if (box == null) {
            box = new AABB(worldPosition).inflate(0.5 + BOUNDS_PADDING);
        }
        return box;
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("nextId", nextId);

        ListTag list = new ListTag();
        for (BeamLink link : links) {
            CompoundTag lt = new CompoundTag();
            lt.putInt("id", link.id);
            lt.putBoolean("isStart", link.isStart);
            writeVec3(lt, "self", link.selfVec);

            if (link.otherVec != null)
                writeVec3(lt, "other", link.otherVec);

            if (link.partnerPos != null)
                lt.put("partner", NbtUtils.writeBlockPos(link.partnerPos));

            lt.putInt("partnerLinkId", link.partnerLinkId);
            lt.putString("wood", link.woodType);
            lt.putLong("placedAt", link.placedAt);

            list.add(lt);
        }

        tag.put("links", list);
        tag.put("capturedAnchorPos", NbtUtils.writeBlockPos(worldPosition));
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        links.clear();
        nextId = tag.getInt("nextId");

        if (tag.contains("links")) {
            for (Tag t : tag.getList("links", Tag.TAG_COMPOUND)) {
                CompoundTag lt = (CompoundTag) t;

                int id = lt.getInt("id");
                boolean isStart = lt.getBoolean("isStart");
                Vec3 self = readVec3(lt, "self", Vec3.ZERO);
                Vec3 other = lt.contains("other") ? readVec3(lt, "other", null) : null;
                BlockPos partner = lt.contains("partner")
                        ? NbtUtils.readBlockPos(lt.getCompound("partner"))
                        : null;
                int partnerLinkId = lt.getInt("partnerLinkId");
                String wood = lt.getString("wood");
                if (wood.isEmpty()) wood = "oak";
                long placedAt = lt.contains("placedAt") ? lt.getLong("placedAt") : 0L;

                links.add(new BeamLink(
                        id,
                        isStart,
                        self,
                        other,
                        partner,
                        partnerLinkId,
                        wood,
                        placedAt
                ));
            }
        }
        capturedAnchorPos = tag.contains("capturedAnchorPos")
                ? NbtUtils.readBlockPos(tag.getCompound("capturedAnchorPos"))
                : null;

        cachedBounds = null;

        if (level != null && level.isClientSide) {
            bumpTopology();
        }
    }
    public void reconcilePositionAfterExternalMove() {
        if (capturedAnchorPos == null || capturedAnchorPos.equals(worldPosition))
            return;

        Vec3 vecDelta = Vec3.atLowerCornerOf(worldPosition)
                .subtract(Vec3.atLowerCornerOf(capturedAnchorPos));
        BlockPos posDelta = worldPosition.subtract(capturedAnchorPos);

        translateLinks(vecDelta, posDelta);
        capturedAnchorPos = worldPosition;
    }
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    private static void writeVec3(CompoundTag tag, String key, Vec3 v) {
        CompoundTag sub = new CompoundTag();
        sub.putDouble("x", v.x);
        sub.putDouble("y", v.y);
        sub.putDouble("z", v.z);
        tag.put(key, sub);
    }

    private static @Nullable Vec3 readVec3(CompoundTag tag, String key, @Nullable Vec3 fallback) {
        if (!tag.contains(key)) return fallback;

        CompoundTag sub = tag.getCompound(key);
        return new Vec3(
                sub.getDouble("x"),
                sub.getDouble("y"),
                sub.getDouble("z")
        );
    }
}
