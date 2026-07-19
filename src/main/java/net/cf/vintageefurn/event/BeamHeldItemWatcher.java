package net.cf.vintageefurn.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.attachment.BeamPlacementData;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.items.BeamItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = VintageFurn.MOD_ID)
public final class BeamHeldItemWatcher {

    private BeamHeldItemWatcher() {
    }

    private static final Set<UUID> SUPPRESS_NEXT_CANCEL =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void suppressNextCancel(ServerPlayer player) {
        SUPPRESS_NEXT_CANCEL.add(player.getUUID());
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.MAINHAND) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        ItemStack to = event.getTo();

        if (SUPPRESS_NEXT_CANCEL.remove(sp.getUUID())) {
            return;
        }

        if (to.getItem() instanceof BeamItem beamItem) {

            BeamPlacementData pending = BeamsAttachments.get(sp.getUUID());

            if (pending != null
                    && !pending.woodType().equals(beamItem.getWoodType())) {
                BeamItem.cancelPending(sp);
            }

            return;
        }

        BeamItem.cancelPending(sp);
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SUPPRESS_NEXT_CANCEL.remove(event.getEntity().getUUID());
    }
}