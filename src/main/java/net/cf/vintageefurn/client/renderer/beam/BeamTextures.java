package net.cf.vintageefurn.client.renderer.beam;

import net.cf.vintageefurn.registry.BeamsWoodTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public final class BeamTextures {

    private BeamTextures() {
    }

    public static TextureAtlasSprite plankSprite(String woodType) {
        ResourceLocation loc = BeamsWoodTypeRegistry.getTexture(woodType);

        if (loc == null) {
            loc = new ResourceLocation("minecraft", "block/" + woodType + "_planks");
        }

        return Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(loc);
    }
    public static TextureAtlasSprite logSprite(String woodType) {
        ResourceLocation loc = BeamsWoodTypeRegistry.getTexture(woodType);

        if (loc == null) {
            loc = new ResourceLocation("minecraft", "block/" + woodType + "_log");
        }

        return Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(loc);
    }
}