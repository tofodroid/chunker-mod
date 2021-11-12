package io.github.tofodroid.mods.chunker.common.item;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.chunker.common.network.NetworkManager;
import io.github.tofodroid.mods.chunker.common.network.OpenChunksSyncPacket;
import io.github.tofodroid.mods.chunker.common.world.ChunkerSavedData;
import io.github.tofodroid.mods.chunker.server.ServerEventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemChunkKey extends Item {
    public ItemChunkKey() {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName("chunkkey");
    }
    
    @Override
    @SuppressWarnings("resource")
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        ChunkerSavedData chunkerData = ServerEventHandler.chunkerData.get(context.getWorld().getDimensionKey());

        if(!context.getWorld().isRemote && chunkerData != null) {
            ChunkPos chunkToOpen = context.getWorld().getChunk(context.getPos()).getPos();

            if(context.getPlayer().isSneaking() && chunkerData.openChunk(chunkToOpen)) {
                NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(context.getWorld()::getDimensionKey), new OpenChunksSyncPacket(chunkerData.getOpenedChunks()));
                context.getWorld().playSound(null, context.getPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.AMBIENT, 1.0f, 0.5f);

                if(!context.getPlayer().isCreative()) {
                    context.getPlayer().setHeldItem(context.getHand(), ItemStack.EMPTY);
                }
                
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }
}
