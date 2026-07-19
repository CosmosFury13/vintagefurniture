package net.cf.vintageefurn.datagen;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class blocktaggenerator extends BlockTagsProvider {
    public blocktaggenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, VintageFurn.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {


        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
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



        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BeamsBlocks.BEAM_ANCHOR.get());
        this.tag(BlockTags.MINEABLE_WITH_SHOVEL);
                //.add();


        this.tag(BlockTags.FENCES);
        this.tag(BlockTags.FENCE_GATES);


    }
}