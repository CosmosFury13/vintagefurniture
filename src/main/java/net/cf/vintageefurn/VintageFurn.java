package net.cf.vintageefurn;

import com.mojang.logging.LogUtils;
import net.cf.vintageefurn.attachment.BeamsAttachments;
import net.cf.vintageefurn.compat.everycomp.EveryCompatLoader;
import net.cf.vintageefurn.network.BeamsNetworking;
import net.cf.vintageefurn.registry.BeamsBlockEntities;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.cf.vintageefurn.registry.BeamsCreativeTab;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VintageFurn.MOD_ID)
public class VintageFurn {
    public static final String MOD_ID = "vintagefurn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VintageFurn() {
        // Forge 1.20.1 way of getting the mod event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register your registries
        BeamsBlocks.BLOCKS.register(modEventBus);
        BeamsBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        BeamsItems.ITEMS.register(modEventBus);
        BeamsCreativeTab.TABS.register(modEventBus);

        // If this exists in your project and is already ported to Forge

        // Lifecycle listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Networking registration
        modEventBus.addListener(BeamsNetworking::register);

        // Register this class on the Forge event bus
        MinecraftForge.EVENT_BUS.register(this);

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BeamsConfig.SPEC);

        // Optional compatibility loader
        EveryCompatLoader.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup code
    }

    // Add items to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Example:
        // if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
        //     event.accept(BeamsItems.MY_ITEM.get());
        // }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server starting logic
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Client setup code
        }
    }
}