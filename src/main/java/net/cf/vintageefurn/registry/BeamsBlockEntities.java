package net.cf.vintageefurn.registry;

import net.cf.vintageefurn.VintageFurn;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BeamsBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VintageFurn.MOD_ID);

    public static final RegistryObject<BlockEntityType<BeamBlockEntity>> BEAM_ANCHOR =
            BLOCK_ENTITIES.register("beam_anchor",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new BeamBlockEntity(BEAM_ANCHOR.get(), pos, state),
                            BeamsBlocks.BEAM_ANCHOR.get()
                    ).build(null)
            );

    private BeamsBlockEntities() {
    }
}