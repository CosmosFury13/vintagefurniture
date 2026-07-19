package net.cf.vintageefurn.datagen;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.items.BeamItem;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.CompletableFuture;

public class itemtaggenerator extends ItemTagsProvider {
    public itemtaggenerator(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, blockTags, VintageFurn.MOD_ID, existingFileHelper);
    }
    public static Map<String, RegistryObject<BeamItem>> getBeamItems() {
        return BEAM_ITEMS;
    }
    private static final Map<String, RegistryObject<BeamItem>> BEAM_ITEMS = new LinkedHashMap<>();
    public static final TagKey<Item> BEAMS =
            TagKey.create(Registries.ITEM, new ResourceLocation("vintagefurn", "beams"));



//    @Override
//    protected void addTags(HolderLookup.Provider pProvider) {
//
//
//        this.tag(ItemTags.PLANKS);
//
//
//
//    }
    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

        var tag = this.tag(BEAMS);

        for (RegistryObject<BeamItem> item : BeamsItems.getBeamItems().values()) {
            tag.add(item.get());
        }

    }

}
