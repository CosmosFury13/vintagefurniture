package net.cf.vintageefurn.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record BeamPlacementData(
        BlockPos pos,
        Direction face,
        int u,
        int v,
        int d,
        int linkId,
        BlockPos resolvedAnchorPos,
        String woodType
) {

    private static final int OUTER_DEPTH = 4;

    public static BeamPlacementData of(BlockPos pos, Direction face, int u, int v, int d) {
        return new BeamPlacementData(pos, face, u, v, d, -1, pos.relative(face), "");
    }

    public BeamPlacementData setPlacement(int linkId, BlockPos resolvedAnchorPos, String woodType) {
        return new BeamPlacementData(pos, face, u, v, d, linkId, resolvedAnchorPos, woodType);
    }

    public static final Codec<BeamPlacementData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BeamPlacementData::pos),
            Direction.CODEC.fieldOf("face").forGetter(BeamPlacementData::face),
            Codec.INT.fieldOf("u").forGetter(BeamPlacementData::u),
            Codec.INT.fieldOf("v").forGetter(BeamPlacementData::v),
            Codec.INT.optionalFieldOf("d", OUTER_DEPTH).forGetter(BeamPlacementData::d),
            Codec.INT.fieldOf("linkId").forGetter(BeamPlacementData::linkId),
            BlockPos.CODEC.fieldOf("resolvedAnchorPos").forGetter(BeamPlacementData::resolvedAnchorPos),
            Codec.STRING.fieldOf("woodType").forGetter(BeamPlacementData::woodType)
    ).apply(inst, BeamPlacementData::new));

    public static void write(FriendlyByteBuf buf, BeamPlacementData data) {
        buf.writeBlockPos(data.pos());
        buf.writeEnum(data.face());

        int packed = (data.u() << 8) | (data.v() << 4) | data.d();
        buf.writeVarInt(packed);

        buf.writeInt(data.linkId());
        buf.writeBlockPos(data.resolvedAnchorPos());
        buf.writeUtf(data.woodType());
    }

    public static BeamPlacementData read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction face = buf.readEnum(Direction.class);

        int packed = buf.readVarInt();
        int u = (packed >> 8) & 0xFF;
        int v = (packed >> 4) & 0xF;
        int d = packed & 0xF;

        int linkId = buf.readInt();
        BlockPos anchor = buf.readBlockPos();
        String wood = buf.readUtf();

        return new BeamPlacementData(pos, face, u, v, d, linkId, anchor, wood);
    }

    public Vec3 toWorldVec3() {
        return snapToWorld(pos, face, u, v, d);
    }

    public BlockPos idealAnchorPos() {
        return pos.relative(face);
    }

    public BlockPos anchorBlockPos() {
        return resolvedAnchorPos;
    }

    public static Vec3 snapToWorld(BlockPos pos, Direction face, int u, int v, int d) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        double du = (u - 2) / 4.0;
        double dv = (v - 2) / 4.0;
        double dd = (d - 2) / 4.0;

        return switch (face.getAxis()) {
            case Y -> new Vec3(cx + du, cy + dd, cz + dv);
            case Z -> new Vec3(cx + du, cy + dv, cz + dd);
            case X -> new Vec3(cx + dd, cy + dv, cz + du);
        };
    }

    public static int[] snapHit(Vec3 hitLocation, BlockPos pos, Direction face) {
        double lx = hitLocation.x - pos.getX() - 0.5;
        double ly = hitLocation.y - pos.getY() - 0.5;
        double lz = hitLocation.z - pos.getZ() - 0.5;

        double rawU, rawV, rawD;

        switch (face) {
            case UP, DOWN -> {
                rawU = lx;
                rawV = lz;
                rawD = ly;
            }
            case NORTH, SOUTH -> {
                rawU = lx;
                rawV = ly;
                rawD = lz;
            }
            default -> {
                rawU = lz;
                rawV = ly;
                rawD = lx;
            }
        }

        int u = (int)Math.round(rawU * 4 + 2);
        int v = (int)Math.round(rawV * 4 + 2);
        int d = (int)Math.round(rawD * 4 + 2);

        return new int[]{
                Mth.clamp(u, 0, 4),
                Mth.clamp(v, 0, 4),
                Mth.clamp(d, 0, 4)
        };
    }

    public int itemsRequired(Vec3 targetWorld) {
        double dist = toWorldVec3().distanceTo(targetWorld);
        return costForLength(dist);
    }

    public static int costForLength(double distance) {
        return Math.max(1, (int)Math.ceil(distance));
    }
}
