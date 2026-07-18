package net.cf.vintageefurn.compat.everycomp;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.items.BeamItem;
import net.cf.vintageefurn.registry.BeamsItems;
import net.cf.vintageefurn.registry.BeamsWoodTypeRegistry;
import net.mehvahdjukaar.every_compat.EveryCompat;
import net.mehvahdjukaar.every_compat.api.EveryCompatAPI;
import net.mehvahdjukaar.every_compat.api.ItemOnlyEntrySet;
import net.mehvahdjukaar.every_compat.api.SimpleModule;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

public final class BeamsEveryCompatModule extends SimpleModule {

    private final ItemOnlyEntrySet<WoodType, BeamItem> beamItems;

    public BeamsEveryCompatModule() {

        super(VintageFurn.MOD_ID, "bm", EveryCompat.MOD_ID);

        beamItems = ItemOnlyEntrySet.builder(WoodType.class, "", "beam",
                        () -> (BeamItem) BeamsItems.getOakBeamItem(),
                        () -> VanillaWoodTypes.OAK,
                        woodType -> new BeamItem(new Item.Properties().stacksTo(64), woodType.getId().toString()))

                .setTab(() -> net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB
                        .get(net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS))

                .addTag(new ResourceLocation(VintageFurn.MOD_ID, "beams"),
                        net.minecraft.core.registries.Registries.ITEM)

                .addRecipe(new ResourceLocation(VintageFurn.MOD_ID, "beam_oak"))

                .build();
        this.addEntry(beamItems);
    }

    @Override
    public boolean isEntryAlreadyRegistered(String entrySetId, ResourceLocation blockId, BlockType blockType, net.minecraft.core.Registry<?> registry) {
        if (blockType.isVanilla()) return true;
        return super.isEntryAlreadyRegistered(entrySetId, blockId, blockType, registry);
    }

    @Override
    public void addTranslations(net.mehvahdjukaar.every_compat.dynamicpack.ClientDynamicResourcesHandler handler,
                                AfterLanguageLoadEvent lang) {
        for (var entry : beamItems.items.entrySet()) {
            WoodType woodType = entry.getKey();
            BeamItem item = entry.getValue();
            lang.addEntry(item.getDescriptionId(), woodType.getReadableName() + " Beam");
        }
    }

    @Override
    public void onModSetup() {
        super.onModSetup();
        for (var entry : beamItems.items.entrySet()) {
            WoodType woodType = entry.getKey();
            BeamItem item = entry.getValue();

            ResourceLocation planksId = BuiltInRegistries.BLOCK.getKey(woodType.planks);
            ResourceLocation planksTexture = new ResourceLocation(
                    planksId.getNamespace(), "block/" + planksId.getPath());

            BeamsWoodTypeRegistry.register(woodType.getId().toString(), item, planksTexture);
        }
    }

    @Override
    public void addDynamicClientResources(Consumer<ResourceGenTask> executor) {
        executor.accept((manager, sink) -> {
            for (var entry : beamItems.items.entrySet()) {
                WoodType woodType = entry.getKey();
                BeamItem item = entry.getValue();

                ResourceLocation texture = resolvePlanksTexture(manager, woodType);

                BeamsWoodTypeRegistry.registerResolved(woodType.getId().toString(), item, texture);

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                ResourceLocation modelId = new ResourceLocation(
                        itemId.getNamespace(), itemId.getPath());

                sink.addItemModel(modelId, beamItemModel(texture));
            }
        });
    }

    private static ResourceLocation resolvePlanksTexture(ResourceManager manager, WoodType woodType) {
        ResourceLocation planksId = BuiltInRegistries.BLOCK.getKey(woodType.planks);
        try {
            return RPUtils.findFirstBlockTextureLocation(manager, woodType.planks);
        } catch (FileNotFoundException e) {
            return new ResourceLocation(
                    planksId.getNamespace(), "block/" + planksId.getPath());
        }
    }

    private static JsonElement beamItemModel(ResourceLocation planksTexture) {
        String json = """
                {
                  "format_version": "1.20.1",
                  "credit": "Made with Blockbench",
                  "parent": "block/block",
                  "textures": { "1": "%s" },
                  "elements": [
                    {
                      "from": [6, 6, 0],
                      "to": [10, 10, 16],
                      "rotation": {"angle": 0, "axis": "y", "origin": [6, 6, 0]},
                      "faces": {
                        "north": {"uv": [0, 0, 4, 4], "texture": "#1"},
                        "east": {"uv": [0, 0, 16, 4], "texture": "#1"},
                        "south": {"uv": [0, 0, 4, 4], "texture": "#1"},
                        "west": {"uv": [0, 0, 16, 4], "texture": "#1"},
                        "up": {"uv": [0, 0, 16, 4], "rotation": 90, "texture": "#1"},
                        "down": {"uv": [0, 0, 16, 4], "rotation": 90, "texture": "#1"}
                      }
                    }
                  ],
                  "display": {
                    "gui": {
                      "rotation": [30, 135, 0],
                      "scale": [0.625, 0.625, 0.625]
                    },
                    "fixed": {
                      "rotation": [0, 90, 0],
                      "scale": [0.5, 0.5, 0.5]
                    }
                  }
                }
                """.formatted(planksTexture.toString());
        return JsonParser.parseString(json);
    }

    @Override
    public int bloatAmount() {
        return beamItems.items.size();
    }

    @Override
    public void registerItemsToExistingTabs(net.mehvahdjukaar.moonlight.api.platform.RegHelper.ItemToTabEvent event) {

    }

    public static void registerIfPresent() {
        if (ModList.get().isLoaded("everycomp")) {
            EveryCompatAPI.registerModule(new BeamsEveryCompatModule());
        }
    }
}
