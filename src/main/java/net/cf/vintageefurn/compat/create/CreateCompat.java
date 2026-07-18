package net.cf.vintageefurn.compat.create;


import net.minecraftforge.fml.ModList;

public final class CreateCompat {

    private CreateCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("create");
    }
}
