package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.items.BeamItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
    public static final String[] STONE_TYPES = {
            "andesite_cut",
            "granite_cut",
            "diorite_cut",
            "limestone_cut",
            "asurine_cut",
            "ochrum_cut",
            "crimsite_cut",
            "veridium_cut",
            "stone",
            "mud_bricks",
            "deepslate_tiles"
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
    public static Item getPlankBlock(String woodType) {
        return switch (woodType) {
            case "oak" -> Items.OAK_PLANKS;
            case "spruce" -> Items.SPRUCE_PLANKS;
            case "birch" -> Items.BIRCH_PLANKS;
            case "jungle" -> Items.JUNGLE_PLANKS;
            case "acacia" -> Items.ACACIA_PLANKS;
            case "dark_oak" -> Items.DARK_OAK_PLANKS;
            case "mangrove" -> Items.MANGROVE_PLANKS;
            case "cherry" -> Items.CHERRY_PLANKS;
            case "crimson" -> Items.CRIMSON_PLANKS;
            case "warped" -> Items.WARPED_PLANKS;
            case "bamboo" -> Items.BAMBOO_PLANKS;
            default -> throw new IllegalArgumentException("Unknown wood type: " + woodType);
        };
    }

    private BeamsItems() {
    }
}
