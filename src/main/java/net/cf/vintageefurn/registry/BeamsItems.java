package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.items.BeamItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BeamsItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VintageFurn.MOD_ID);

    public static final String[] WOOD_TYPES = {
            "oak",
            "spruce",
            "birch",
            "jungle",
            "acacia",
            "dark_oak",
            "mangrove",
            "cherry",
            "crimson",
            "warped",
            "bamboo"
    };

    private static final Map<String, RegistryObject<BeamItem>> BEAM_ITEMS = new LinkedHashMap<>();

    static {
        for (String wood : WOOD_TYPES) {
            RegistryObject<BeamItem> item = ITEMS.register(
                    "beam_" + wood,
                    () -> new BeamItem(new Item.Properties().stacksTo(64), wood)
            );

            BEAM_ITEMS.put(wood, item);
        }
    }

    public static Item getBeamItem(String woodType) {
        RegistryObject<BeamItem> item = BEAM_ITEMS.get(woodType);

        if (item != null) {
            return item.get();
        }

        Item modded = BeamsWoodTypeRegistry.getItem(woodType);
        if (modded != null) {
            return modded;
        }

        return BEAM_ITEMS.get("oak").get();
    }

    public static Item getOakBeamItem() {
        return BEAM_ITEMS.get("oak").get();
    }
    public static Map<String, RegistryObject<BeamItem>> getBeamItems() {
        return BEAM_ITEMS;
    }

    private BeamsItems() {
    }
}
