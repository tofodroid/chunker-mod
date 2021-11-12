package io.github.tofodroid.mods.chunker.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class PlayerBorderTeleportPacketHandler {
    public static void handlePacket(final PlayerBorderTeleportPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            handlePacketServer(message, ctx.get().getSender());
        } else {
            ChunkerMod.LOGGER.warn("Client received unexpected PlayerBorderTeleportPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final PlayerBorderTeleportPacket message, ServerPlayerEntity sender) {
        if(message.teleport) {
            sender.teleport(sender.getServerWorld(), message.targetPos.x, message.targetPos.y, message.targetPos.z, sender.getPitchYaw().x, sender.getPitchYaw().y);
        } else {
            sender.setPosition(message.targetPos.x, message.targetPos.y, message.targetPos.z);
        }

        if(message.doDismount) {
            sender.stopRiding();
        }
    }
}
