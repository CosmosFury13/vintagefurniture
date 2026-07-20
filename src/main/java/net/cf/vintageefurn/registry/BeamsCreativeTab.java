package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BeamsCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VintageFurn.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BEAMS_TAB =
            TABS.register("beams_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.beams"))
                            .icon(() -> new ItemStack(BeamsItems.getBeamItem("oak")))
                            .displayItems((parameters, output) -> {
                                // Beams
                                for (String wood : BeamsItems.WOOD_TYPES) {
                                    output.accept(BeamsItems.getBeamItem(wood));
                                }

                                // Railings
//                                output.accept(BeamsBlocks.ANDESITE_CUT_GLASS_RAILING.get());
                            })
                            .build()
            );

    private BeamsCreativeTab() {
    }
}
