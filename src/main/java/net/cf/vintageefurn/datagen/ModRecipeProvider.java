package net.cf.vintageefurn.datagen;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.registry.BeamsBlocks;
import net.cf.vintageefurn.registry.BeamsItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {

        for (String wood : BeamsItems.WOOD_TYPES) {;
            beamRecipe(
                    writer,
                    BeamsItems.getPlankBlock(wood),
                    BeamsItems.getBeamItem(wood),
                    wood
            );
        }
        stoneCutting(
                writer,
                RecipeCategory.BUILDING_BLOCKS,
                BeamsBlocks.STONE_GLASS_RAILING.get(),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "stone")),
                4
        );
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                            pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  VintageFurn.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
    private void beamRecipe(Consumer<FinishedRecipe> writer,
                            ItemLike planks,
                            ItemLike beam,
                            String woodName) {

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, beam, 8)
                .pattern("  P")
                .pattern(" P ")
                .pattern("P  ")
                .define('P', planks)
                .unlockedBy(getHasName(planks), has(planks))
                .save(writer);
    }
    private static void stoneCutting(Consumer<FinishedRecipe> writer,
                                     RecipeCategory category,
                                     ItemLike result,
                                     ItemLike ingredient,
                                     int count) {

        SingleItemRecipeBuilder.stonecutting(
                        Ingredient.of(ingredient),
                        category,
                        result,
                        count)
                .unlockedBy(getHasName(ingredient), has(ingredient))
                .save(writer);
    }
}