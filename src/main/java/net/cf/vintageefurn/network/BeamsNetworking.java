package net.cf.vintageefurn.network;

package com.syl.beams.network;

import net.cf.vintageefurn.VintageFurn;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BeamsNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VintageFurn.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private BeamsNetworking() {
    }

    public static void register() {

        CHANNEL.registerMessage(
                packetId++,
                BeamSyncPayload.class,
                BeamSyncPayload::encode,
                BeamSyncPayload::new,
                BeamSyncPayload::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BeamCancelPayload.class,
                BeamCancelPayload::encode,
                BeamCancelPayload::new,
                BeamCancelPayload::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BeamBreakRequestPayload.class,
                BeamBreakRequestPayload::encode,
                BeamBreakRequestPayload::new,
                BeamBreakRequestPayload::handle
        );
    }
}