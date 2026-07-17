package net.cf.vintageefurn.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.cf.vintageefurn.blocks.beam.BeamAnchorBlock;
import net.cf.vintageefurn.compat.sable.SableCompat;
import net.cf.vintageefurn.compat.sable.SableRayCompat;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = VintageFurn.MOD_ID)
public final class BeamBreakHandler {

    private BeamBreakHandler() {}

    private static final long COOLDOWN_MS = 250L;

    private static final Map<UUID, Long> LAST_REMOVAL_MS = new ConcurrentHashMap<>();

    public enum Outcome {
        NOT_APPLICABLE,
        ON_COOLDOWN,
        REMOVED_ONE,
        REMOVED_LAST
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;
        if (!(event.getState().getBlock() instanceof BeamAnchorBlock)) return;

        Outcome outcome = breakOneLinkAt(sp, level, event.getPos());

        switch (outcome) {
            case ON_COOLDOWN:
            case REMOVED_ONE:
            case REMOVED_LAST:
                event.setCanceled(true);
                break;
            case NOT_APPLICABLE:
                break;
        }
    }

    public static Outcome breakOneLinkAt(ServerPlayer sp, Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof BeamAnchorBlock)) {
            return Outcome.NOT_APPLICABLE;
        }

        if (!(level.getBlockEntity(pos) instanceof BeamBlockEntity be) || be.getLinks().isEmpty()) {
            return Outcome.NOT_APPLICABLE;
        }

        long now = System.currentTimeMillis();
        Long last = LAST_REMOVAL_MS.get(sp.getUUID());

        if (last != null && now - last < COOLDOWN_MS) {
            return Outcome.ON_COOLDOWN;
        }

        Vec3 origin = sp.getEyePosition(1.0F);
        Vec3 look = sp.getViewVector(1.0F);
        Vec3 end = origin.add(look.scale(sp.blockInteractionRange()));

        Vec3 localOrigin = origin;
        Vec3 localEnd = end;

        if (SableCompat.isLoaded()) {
            Vec3[] localized = SableRayCompat.localizeFor(level, pos, origin, end);
            if (localized != null) {
                localOrigin = localized[0];
                localEnd = localized[1];
            }
        }

        BeamBlockEntity.BeamLink target =
                BeamAnchorBlock.findTargetedLink(pos, be, localOrigin, localEnd);

        if (target == null) {
            return Outcome.NOT_APPLICABLE;
        }

        level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!sp.getAbilities().instabuild) {
            Item item = BeamsItems.getBeamItem(target.woodType);

            if (item != null) {
                Block.popResource(
                        level,
                        pos,
                        new ItemStack(item, BeamBlockEntity.itemCountFor(level, target))
                );
            }
        }

        if (target.partnerPos != null
                && level.getBlockEntity(target.partnerPos) instanceof BeamBlockEntity partnerBE) {

            partnerBE.findLink(target.partnerLinkId).ifPresent(partnerLink -> {
                boolean reciprocal =
                        pos.equals(partnerLink.partnerPos)
                                && partnerLink.partnerLinkId == target.id;

                if (reciprocal) {
                    partnerBE.removeLink(target.partnerLinkId);

                    if (partnerBE.hasNoLinks() && !target.partnerPos.equals(pos)) {
                        level.removeBlock(target.partnerPos, false);
                    }
                }
            });
        }

        be.removeLink(target.id);
        LAST_REMOVAL_MS.put(sp.getUUID(), now);

        if (be.hasNoLinks()) {
            level.removeBlock(pos, false);
            return Outcome.REMOVED_LAST;
        }

        return Outcome.REMOVED_ONE;
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_REMOVAL_MS.remove(event.getEntity().getUUID());
    }
}
