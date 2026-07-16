package net.cf.vintageefurn.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BeamsWoodTypeRegistry {

    public record Entry(String key, Item beamItem, ResourceLocation planksTexture, boolean resolved) {}

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    public static void register(String key, Item beamItem, ResourceLocation planksTexture) {
        Entry existing = ENTRIES.get(key);
        if (existing != null && existing.resolved()) {
            ENTRIES.put(key, new Entry(key, beamItem, existing.planksTexture(), true));
            return;
        }
        ENTRIES.put(key, new Entry(key, beamItem, planksTexture, false));
    }

    public static void registerResolved(String key, Item beamItem, ResourceLocation planksTexture) {
        ENTRIES.put(key, new Entry(key, beamItem, planksTexture, true));
    }

    public static Item getItem(String key) {
        Entry e = ENTRIES.get(key);
        return e == null ? null : e.beamItem();
    }

    public static ResourceLocation getTexture(String key) {
        Entry e = ENTRIES.get(key);
        return e == null ? null : e.planksTexture();
    }

    private BeamsWoodTypeRegistry() {
    }
}
