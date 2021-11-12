package io.github.tofodroid.mods.chunker.server;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import io.github.tofodroid.mods.chunker.common.config.ModConfigs;
import io.github.tofodroid.mods.chunker.common.item.ModItems;
import io.github.tofodroid.mods.chunker.common.network.NetworkManager;
import io.github.tofodroid.mods.chunker.common.network.OpenChunksSyncPacket;
import io.github.tofodroid.mods.chunker.common.world.ChunkerSavedData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ChunkerMod.MODID)
public abstract class ServerEventHandler {
    public static Map<RegistryKey<World>, ChunkerSavedData> chunkerData = new HashMap<>();

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if(e.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) e.getWorld();

            if(ModConfigs.COMMON.getEnabledDimensions().contains(world.getDimensionKey().getLocation().toString())) {
                chunkerData.put(world.getDimensionKey(), (ChunkerSavedData)world.getSavedData().getOrCreate(ChunkerSavedData::new, ChunkerSavedData.CHUNKER_SAVE_NAME));
            }            
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerLoggedInEvent e) {
        if(e.getPlayer() instanceof ServerPlayerEntity) {
            chunkerPlayerSpawn((ServerPlayerEntity)e.getPlayer());
        }
    }

    @SubscribeEvent
	public static void onPlayerSpawn(PlayerRespawnEvent e) {
        if(e.getPlayer() instanceof ServerPlayerEntity) {
            chunkerPlayerSpawn((ServerPlayerEntity)e.getPlayer());
        }
    }
    
    @SubscribeEvent
	public static void onPlayerChangeDimension(PlayerChangedDimensionEvent e) {
        if(e.getPlayer() instanceof ServerPlayerEntity) {
            chunkerPlayerSpawn((ServerPlayerEntity)e.getPlayer());
            
            // If enabled open spawn chunk in new dimension if player holding key
            if(ModConfigs.COMMON.openDimensionChunk.get() && chunkerData.containsKey(e.getPlayer().world.getDimensionKey()) && ModItems.CHUNKKEY.equals(e.getPlayer().getHeldItemOffhand().getItem())) {
                if(chunkerData.get(e.getPlayer().world.getDimensionKey()).openChunk(new ChunkPos(e.getPlayer().getPosition().getX() >> 4, e.getPlayer().getPosition().getZ() >> 4))) {
                    e.getPlayer().setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                    NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(e.getPlayer().world::getDimensionKey), new OpenChunksSyncPacket(chunkerData.get(e.getPlayer().world.getDimensionKey()).getOpenedChunks()));    
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerPearl(EntityTeleportEvent.EnderPearl e) {
        if(e.getPlayer() instanceof ServerPlayerEntity && !e.isCanceled() && chunkerData.containsKey(e.getPlayer().world.getDimensionKey())) {
            // If enabled open pearl chunk, else cancel event if pearl chunk is closed
            if(ModItems.CHUNKKEY.equals(e.getPlayer().getHeldItemOffhand().getItem()) && ModConfigs.COMMON.openEnderPearlChunk.get()) {
                if(chunkerData.get(e.getPlayer().world.getDimensionKey()).openChunk(new ChunkPos(new Double(e.getTargetX()).intValue() >> 4, new Double(e.getTargetZ()).intValue() >> 4))) {
                    e.getPlayer().setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                    NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(e.getPlayer().world::getDimensionKey), new OpenChunksSyncPacket(chunkerData.get(e.getPlayer().world.getDimensionKey()).getOpenedChunks()));
                }
            } else if(!chunkerData.get(e.getPlayer().world.getDimensionKey()).isChunkOpen(new ChunkPos(new Double(e.getTargetX()).intValue() >> 4, new Double(e.getTargetZ()).intValue() >> 4))) {
                e.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChorusFruit(EntityTeleportEvent.ChorusFruit e) {
        if(e.getEntity() instanceof ServerPlayerEntity && !e.isCanceled() && chunkerData.containsKey(e.getEntity().world.getDimensionKey())) {
            if(!chunkerData.get(e.getEntity().world.getDimensionKey()).isChunkOpen(new ChunkPos(new Double(e.getTargetX()).intValue() >> 4, new Double(e.getTargetZ()).intValue() >> 4))) {
                e.setCanceled(true);
            }
        }
    }

    protected static void chunkerPlayerSpawn(ServerPlayerEntity e) {
        if(chunkerData.containsKey(e.world.getDimensionKey())) {
            openSpawnChunk(e);
        }
        syncChunkerPlayerData(e);
    }

    protected static void openSpawnChunk(ServerPlayerEntity e) {
        if(ModConfigs.COMMON.openFirstSpawnChunk.get() && !chunkerData.get(e.world.getDimensionKey()).hasOpenedChunks()) {
            chunkerData.get(e.world.getDimensionKey()).openChunk(new ChunkPos(e.getPosition().getX() >> 4, e.getPosition().getZ() >> 4));
            NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(e.world::getDimensionKey), new OpenChunksSyncPacket(chunkerData.get(e.world.getDimensionKey()).getOpenedChunks()));
        }
    }

    protected static void syncChunkerPlayerData(ServerPlayerEntity e) {
        if(chunkerData.containsKey(e.world.getDimensionKey())) {
            NetworkManager.NET_CHANNEL.send(PacketDistributor.PLAYER.with(() -> e), new OpenChunksSyncPacket(chunkerData.get(e.world.getDimensionKey()).getOpenedChunks()));
        } else {
            NetworkManager.NET_CHANNEL.send(PacketDistributor.PLAYER.with(() -> e), new OpenChunksSyncPacket(new OrderedHashSet<>()));
        }
    }
}
