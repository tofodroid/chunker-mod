package io.github.tofodroid.mods.chunker.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.chunker.client.ClientEventHandler;
import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import io.github.tofodroid.mods.chunker.common.world.ChunkerSavedData;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class OpenChunksSyncPacketHandler {
    public static void handlePacket(final OpenChunksSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ChunkerMod.LOGGER.warn("Server received unexpected OpenChunksSyncPacket!");
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketClient(final OpenChunksSyncPacket message) {
        ClientEventHandler.setChunkShape(ChunkerSavedData.buildShapeForChunks(message.openedChunks));
    }
}
