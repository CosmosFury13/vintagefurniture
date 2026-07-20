package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blocks.beam.BeamAnchorBlock;
import net.cf.vintageefurn.blocks.railing.RailingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BeamsBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VintageFurn.MOD_ID);

    public static final RegistryObject<BeamAnchorBlock> BEAM_ANCHOR =
            BLOCKS.register(
                    "beam_anchor",
                    () -> new BeamAnchorBlock(beamAnchorProperties())
            );
    public static final RegistryObject<Block> ANDESITE_CUT_GLASS_RAILING = BLOCKS.register("andesite_cut_glass_railing",
            () -> new RailingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0f).noOcclusion()));

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

    private BeamsBlocks() {
    }
}
