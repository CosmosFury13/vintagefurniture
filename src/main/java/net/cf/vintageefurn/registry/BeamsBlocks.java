package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blocks.beam.BeamAnchorBlock;
import net.cf.vintageefurn.blocks.railing.RailingBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class BeamsBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VintageFurn.MOD_ID);

    public static final RegistryObject<BeamAnchorBlock> BEAM_ANCHOR =
            BLOCKS.register(
                    "beam_anchor",
                    () -> new BeamAnchorBlock(beamAnchorProperties())
            );
    public static final RegistryObject<Block> STONE_GLASS_RAILING =
            registerBlock("stone_glass_railing",
                    () -> new RailingBlock(
                            BlockBehaviour.Properties.copy(Blocks.STONE)
                                    .strength(2.0f)
                                    .noOcclusion()));

    private static BlockBehaviour.Properties beamAnchorProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(1.0F, 2.0F)
                .noOcclusion()
                .noCollission()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .pushReaction(PushReaction.DESTROY);
    }
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        BeamsItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private BeamsBlocks() {
    }
}
