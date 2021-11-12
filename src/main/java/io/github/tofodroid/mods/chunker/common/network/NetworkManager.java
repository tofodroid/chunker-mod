package io.github.tofodroid.mods.chunker.common.network;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String NET_PROTOCOL = "1";

    public static final SimpleChannel NET_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ChunkerMod.MODID, "net_channel"))
            .networkProtocolVersion(() -> NET_PROTOCOL)
            .clientAcceptedVersions(NET_PROTOCOL::equals)
            .serverAcceptedVersions(NET_PROTOCOL::equals)
            .simpleChannel();
    
    public static void init(final FMLCommonSetupEvent event) {
        NET_CHANNEL.registerMessage(0, OpenChunksSyncPacket.class, OpenChunksSyncPacket::encodePacket, OpenChunksSyncPacket::decodePacket, OpenChunksSyncPacketHandler::handlePacket);
        NET_CHANNEL.registerMessage(1, PlayerBorderTeleportPacket.class, PlayerBorderTeleportPacket::encodePacket, PlayerBorderTeleportPacket::decodePacket, PlayerBorderTeleportPacketHandler::handlePacket);
    }
}
