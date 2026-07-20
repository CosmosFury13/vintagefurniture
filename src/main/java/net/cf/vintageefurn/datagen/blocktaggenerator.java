package net.cf.vintageefurn.datagen;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class blocktaggenerator extends BlockTagsProvider {
    public blocktaggenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, VintageFurn.MOD_ID, existingFileHelper);
    }
    public static final TagKey<Block> MOVEABLE_EMPTY_COLLIDER =
            TagKey.create(
                    Registries.BLOCK,
                    new ResourceLocation("create", "moveable_empty_collider")
            );
    @Override
    protected void addTags(HolderLookup.Provider pProvider) {


        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BeamsBlocks.ANDESITE_CUT_GLASS_RAILING.get())
//                .add(BeamsBlocks.ANDESITE_CUT_ARCHED_RAILING.get())
//                .add(BeamsBlocks.ANDESITE_CUT_SIMPLE_RAILING.get())
                ;



        this.tag(BlockTags.NEEDS_IRON_TOOL)
                ;


        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                ;

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                
                ;

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                ;

        this.tag(BlockTags.LOGS_THAT_BURN);

        this.tag(BlockTags.PLANKS);
        this.tag(MOVEABLE_EMPTY_COLLIDER)
                .add(BeamsBlocks.BEAM_ANCHOR.get());


        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BeamsBlocks.BEAM_ANCHOR.get());
        this.tag(BlockTags.MINEABLE_WITH_SHOVEL);
                //.add();


        this.tag(BlockTags.FENCES);
        this.tag(BlockTags.FENCE_GATES);


    }
}