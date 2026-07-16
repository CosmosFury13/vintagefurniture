package net.cf.vintageefurn.attachment;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeamsAttachments {

    private static final Map<UUID, BeamPlacementData> PLACEMENTS = new ConcurrentHashMap<>();

    private BeamsAttachments() {
    }

    public static BeamPlacementData get(UUID player) {
        return PLACEMENTS.get(player);
    }

    public static void set(UUID player, BeamPlacementData data) {
        if (data == null) {
            PLACEMENTS.remove(player);
        } else {
            PLACEMENTS.put(player, data);
        }
    }

    public static void clear(UUID player) {
        PLACEMENTS.remove(player);
    }

    public static boolean has(UUID player) {
        return PLACEMENTS.containsKey(player);
    }
}
