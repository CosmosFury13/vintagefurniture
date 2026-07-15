package net.cf.vintageefurn;

import net.minecraftforge.common.ForgeConfigSpec;

public class BeamsConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue GHOST_PREVIEW;
    public static final ForgeConfigSpec.BooleanValue BEAM_PITCH_SOUND;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        GHOST_PREVIEW = builder
                .comment(
                        "If true, the beam preview shown while placing the second endpoint is a " +
                                "translucent 'ghost'. If false, the preview instead renders fully solid."
                )
                .define("ghostPreview", false);

        BEAM_PITCH_SOUND = builder
                .comment(
                        "If true, a pitched placement sound plays whenever a new beam segment is " +
                                "added to or removed from the placement preview. If false, this sound is disabled."
                )
                .define("beamPitchSound", true);

        SPEC = builder.build();
    }
}