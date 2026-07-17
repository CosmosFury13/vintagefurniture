package net.cf.vintageefurn.client.event;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.client.renderer.beam.BeamBlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class BeamResourceReloadHandler {

    private BeamResourceReloadHandler() {
    }

    @SubscribeEvent
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }

        BeamBlockEntityRenderer.invalidateAllCaches();
    }
}
