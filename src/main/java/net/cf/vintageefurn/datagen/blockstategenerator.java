package net.cf.vintageefurn.datagen;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blocks.railing.RailingBlock;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class blockstategenerator extends BlockStateProvider {
    public blockstategenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, VintageFurn.MOD_ID, exFileHelper);
    }
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;


    @Override
    protected void registerStatesAndModels() {
        railingglassModel(
                "stone_glass_railing",
                new ResourceLocation("minecraft", "block/stone"),
                new ResourceLocation("minecraft", "block/oak_planks")
        );
        railingBlockState(
                BeamsBlocks.STONE_GLASS_RAILING,
                "stone_glass_railing"
        );
        railingarchedModel(
                "stone_arched_railing",
                new ResourceLocation("minecraft", "block/stone"),
                new ResourceLocation("minecraft", "block/oak_planks")
        );
        railingsimpleModel(
                "stone_simple_railing",
                new ResourceLocation("minecraft", "block/stone"),
                new ResourceLocation("minecraft", "block/oak_planks")
        );
//        railingarchedModel(
//                "andesite_cut_arched_railing",
//                new ResourceLocation(VintageFurn.MOD_ID, "block/create/stone_types/cut/andesite_cut"),
//                new ResourceLocation("minecraft", "block/oak_planks")
//        );
//        railingsimpleModel(
//                "andesite_cut_simple_railing",
//                new ResourceLocation(VintageFurn.MOD_ID, "block/create/stone_types/cut/andesite_cut"),
//                new ResourceLocation("minecraft", "block/oak_planks")
//        );

    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
    private void railingglassModel(String name,
                              ResourceLocation base,
                              ResourceLocation rail) {

        models().withExistingParent(name,
                        modLoc("block/railing/template_glass_railing"))
                .texture("base", base)
                .texture("rail", rail)
                .texture("holder", rail)
                .texture("glass", mcLoc("block/glass"))
                .texture("glass_edge", mcLoc("block/glass_pane_top"));
    }
    private void railingarchedModel(String name,
                                   ResourceLocation base,
                                   ResourceLocation rail) {

        models().withExistingParent(name,
                        modLoc("block/railing/template_arched_railing"))
                .texture("base", base)
                .texture("rail", rail);
    }
    private void railingsimpleModel(String name,
                                    ResourceLocation base,
                                    ResourceLocation rail) {

        models().withExistingParent(name,
                        modLoc("block/railing/template_arched_railing"))
                .texture("base", base)
                .texture("rail", rail);
    }
    private void railingBlockState(RegistryObject<? extends Block> block, String modelName) {
        var builder = getMultipartBuilder(block.get());

                builder.part()
                .modelFile(models().getExistingFile(modLoc("block/" + modelName)))
                .rotationY(90)
                .addModel()
                .condition(NORTH, true);

                builder.part()
                .modelFile(models().getExistingFile(modLoc("block/" + modelName)))
                .rotationY(270) // -90
                .addModel()
                .condition(SOUTH, true);

                builder.part()
                .modelFile(models().getExistingFile(modLoc("block/" + modelName)))
                .rotationY(180)
                .addModel()
                .condition(EAST, true);

                builder.part()
                .modelFile(models().getExistingFile(modLoc("block/" + modelName)))
                .addModel()
                .condition(WEST, true);
    }

}
