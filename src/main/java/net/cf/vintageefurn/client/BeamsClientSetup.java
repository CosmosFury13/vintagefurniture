package net.cf.vintageefurn.client;


import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.client.renderer.beam.BeamBlockEntityRenderer;
import net.cf.vintageefurn.registry.BeamsBlockEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = VintageFurn.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class BeamsClientSetup {

    private BeamsClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BeamsBlockEntities.BEAM_ANCHOR.get(),
                BeamBlockEntityRenderer::new
        );
    }
}