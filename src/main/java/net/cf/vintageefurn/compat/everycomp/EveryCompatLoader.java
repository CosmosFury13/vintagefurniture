package net.cf.vintageefurn.compat.everycomp;


import net.minecraftforge.fml.ModList;

public final class EveryCompatLoader {

    public static void init() {
        if (ModList.get().isLoaded("everycomp")) {
            BeamsEveryCompatModule.registerIfPresent();
        }
    }

    private EveryCompatLoader() {}
}