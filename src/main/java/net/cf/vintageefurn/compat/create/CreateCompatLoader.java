package net.cf.vintageefurn.compat.create;


import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries;
import net.cf.vintageefurn.blockentities.BeamBlockEntity;
import net.cf.vintageefurn.registry.BeamsBlockEntities;
import net.cf.vintageefurn.registry.BeamsBlocks;

public final class CreateCompatLoader {

    public static void init() {
        MovementBehaviour.REGISTRY.register(
                BeamsBlocks.BEAM_ANCHOR.get(),
                new BeamContraptionMovementBehavior()
        );

        MovedBlockTransformerRegistries.BLOCK_ENTITY_TRANSFORMERS.register(
                BeamsBlockEntities.BEAM_ANCHOR.get(),
                (be, transform) -> {
                    if (be instanceof BeamBlockEntity beamBlockEntity) {
                        beamBlockEntity.reconcilePositionAfterExternalMove();
                    }
                }
        );
    }

    private CreateCompatLoader() {}
}
