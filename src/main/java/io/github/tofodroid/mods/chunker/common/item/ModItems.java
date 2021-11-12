package io.github.tofodroid.mods.chunker.common.item;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;

@ObjectHolder(ChunkerMod.MODID)
public final class ModItems {
    
    public static ChunkerModItemGroup ITEM_GROUP;

    public static final ItemChunkKey CHUNKKEY = null;
    public static final ItemChunkLock CHUNKLOCK = null;

    @Mod.EventBusSubscriber(modid = ChunkerMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            ITEM_GROUP = new ChunkerModItemGroup();

            event.getRegistry().register(new ItemChunkKey());
            event.getRegistry().register(new ItemChunkLock());
        }
    }
}