package net.cf.vintageefurn.datagen;


import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.items.BeamItem;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class itemmodelgenerator extends ItemModelProvider {
    public itemmodelgenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, VintageFurn.MOD_ID, existingFileHelper);
    }
    private static final Map<String, RegistryObject<BeamItem>> BEAM_ITEMS = new LinkedHashMap<>();

    public static Map<String, RegistryObject<BeamItem>> getBeamItems() {
        return BEAM_ITEMS;
    }

    @Override
    protected void registerModels() {
        BeamsItems.getBeamItems().forEach(this::beamItem);
        railingItems("andesite_cut");
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(VintageFurn.MOD_ID,"item/" + item.getId().getPath()));
    }
    private ItemModelBuilder blockItem(String name) {
        return withExistingParent(name,
                modLoc("block/" + name));
    }
    private void railingItems(String stoneType) {
        blockItem(stoneType + "_glass_railing");
        blockItem(stoneType + "_arched_railing");
        blockItem(stoneType + "_simple_railing");
    }
    private ItemModelBuilder beamItem(String woodType, RegistryObject<BeamItem> beam) {
        return getBuilder(beam.getId().getPath())
                .parent(getExistingFile(mcLoc("block/block")))

                .texture("1", mcLoc("block/" + woodType + "_planks"))
                .texture("particle", mcLoc("block/" + woodType + "_planks"))

                .element()
                .from(6, 6, 0)
                .to(10, 10, 16)

                .face(net.minecraft.core.Direction.NORTH)
                .uvs(0, 0, 4, 4)
                .texture("#1")
                .end()

                .face(net.minecraft.core.Direction.SOUTH)
                .uvs(0, 0, 4, 4)
                .texture("#1")
                .end()

                .face(net.minecraft.core.Direction.EAST)
                .uvs(0, 0, 16, 4)
                .texture("#1")
                .end()

                .face(net.minecraft.core.Direction.WEST)
                .uvs(0, 0, 16, 4)
                .texture("#1")
                .end()

                .face(net.minecraft.core.Direction.UP)
                .uvs(0, 0, 16, 4)
                .rotation(net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90)
                .texture("#1")
                .end()

                .face(net.minecraft.core.Direction.DOWN)
                .uvs(0, 0, 16, 4)
                .rotation(net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90)
                .texture("#1")
                .end()
                .end()

                .transforms()

                .transform(net.minecraft.world.item.ItemDisplayContext.GUI)
                .rotation(30, 135, 0)
                .scale(0.625f)
                .end()

                .transform(net.minecraft.world.item.ItemDisplayContext.FIXED)
                .rotation(0, 90, 0)
                .scale(0.5f)
                .end()

                .end();
    }
}
