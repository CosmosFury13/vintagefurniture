package net.cf.vintageefurn.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.cf.vintageefurn.client.renderer.beam.BeamPrismRenderer;
import net.cf.vintageefurn.client.renderer.beam.BeamTextures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public final class BeamContraptionMovementBehavior implements MovementBehaviour {

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(
            MovementContext context,
            VirtualRenderWorld renderWorld,
            ContraptionMatrices matrices,
            MultiBufferSource buffer
    ) {
        CompoundTag tag = context.blockEntityData;
        if (tag == null || !tag.contains("links")) {
            return;
        }

        BlockPos anchor = context.contraption.anchor;
        BlockPos localPos = context.localPos;
        if (anchor == null || localPos == null) {
            return;
        }

        Vec3 capturedWorldPos = Vec3.atLowerCornerOf(anchor).add(Vec3.atLowerCornerOf(localPos));
        Vec3 renderOrigin = Vec3.atLowerCornerOf(localPos);

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(matrices.getViewProjection().last().pose());
        poseStack.last().normal().set(matrices.getViewProjection().last().normal());
        ContraptionMatrices.transform(poseStack, matrices.getModel());

        VertexConsumer consumer = buffer.getBuffer(RenderType.cutoutMipped());

        for (Tag t : tag.getList("links", Tag.TAG_COMPOUND)) {
            CompoundTag link = (CompoundTag) t;

            boolean isStart = link.getBoolean("isStart");
            boolean hasOther = link.contains("other");

            if (!isStart || !hasOther)
                continue;

            Vec3 self = readVec3(link, "self");
            Vec3 other = readVec3(link, "other");

            if (self == null || other == null)
                continue;

            String wood = link.getString("wood");
            if (wood.isEmpty())
                wood = "oak";

            TextureAtlasSprite sprite = BeamTextures.plankSprite(wood);
            if (sprite == null)
                continue;

            Vec3 startLocal = self.subtract(capturedWorldPos);
            Vec3 endLocal = other.subtract(capturedWorldPos);

            List<BeamPrismRenderer.CachedQuad> quads = BeamPrismRenderer.buildCache(
                    startLocal,
                    endLocal,
                    renderOrigin,
                    renderWorld,
                    sprite,
                    255,
                    255,
                    255
            );

            if (quads.isEmpty())
                continue;

            if (context.world != null) {
                BeamPrismRenderer.renderCachedWithRealLight(
                        poseStack,
                        consumer,
                        quads,
                        context.world,
                        matrices.getLight(),
                        255
                );
            } else {
                BeamPrismRenderer.renderCached(
                        poseStack,
                        consumer,
                        quads,
                        renderWorld,
                        255
                );
            }
        }
    }

    private static Vec3 readVec3(CompoundTag tag, String key) {
        if (!tag.contains(key))
            return null;

        CompoundTag sub = tag.getCompound(key);
        return new Vec3(
                sub.getDouble("x"),
                sub.getDouble("y"),
                sub.getDouble("z")
        );
    }
}