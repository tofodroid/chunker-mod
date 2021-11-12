package io.github.tofodroid.mods.chunker.common.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


import javax.annotation.Nonnull;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;

public class ChunkerModItemGroup extends ItemGroup {
    public ChunkerModItemGroup() {
        super(ChunkerMod.MODID + ".group");
    }
    
    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(ModItems.CHUNKKEY);
    }
}
