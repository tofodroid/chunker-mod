package io.github.tofodroid.mods.chunker.common.network;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import io.github.tofodroid.mods.chunker.common.world.ChunkerSavedData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

public class OpenChunksSyncPacket {
    public final OrderedHashSet<ChunkPos> openedChunks;

    public OpenChunksSyncPacket(OrderedHashSet<ChunkPos> openedChunks) {
        this.openedChunks = openedChunks;
    }

    public static OpenChunksSyncPacket decodePacket(PacketBuffer buf) {
        try {
            return new OpenChunksSyncPacket(ChunkerSavedData.intArrayToChunks(buf.readVarIntArray()));
        } catch (IndexOutOfBoundsException e) {
            ChunkerMod.LOGGER.error("OpenChunksSyncPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(OpenChunksSyncPacket pkt, PacketBuffer buf) {
        buf.writeVarIntArray(ChunkerSavedData.chunksToIntArray(pkt.openedChunks));
    }
}
