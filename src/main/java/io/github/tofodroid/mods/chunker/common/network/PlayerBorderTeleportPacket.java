package io.github.tofodroid.mods.chunker.common.network;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class PlayerBorderTeleportPacket {
    public final Vector3d targetPos;
    public final Boolean doDismount;
    public final Boolean teleport;

    public PlayerBorderTeleportPacket(Vector3d targetPos, Boolean doDismount, Boolean teleport) {
        this.targetPos = targetPos;
        this.doDismount = doDismount;
        this.teleport = teleport;
    }

    public static PlayerBorderTeleportPacket decodePacket(PacketBuffer buf) {
        try {
            return new PlayerBorderTeleportPacket(new Vector3d(buf.readDouble(),buf.readDouble(),buf.readDouble()), buf.readBoolean(), buf.readBoolean());
        } catch (IndexOutOfBoundsException e) {
            ChunkerMod.LOGGER.error("PlayerBorderTeleportPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(PlayerBorderTeleportPacket pkt, PacketBuffer buf) {
        buf.writeDouble(pkt.targetPos.x);
        buf.writeDouble(pkt.targetPos.y);
        buf.writeDouble(pkt.targetPos.z);
        buf.writeBoolean(pkt.doDismount);
        buf.writeBoolean(pkt.teleport);
    }
}
