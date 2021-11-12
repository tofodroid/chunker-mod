package io.github.tofodroid.mods.chunker.common.config;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod.EventBusSubscriber(modid = ChunkerMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfigs {
    public static ClientConfig CLIENT;
    public static CommonConfig COMMON;
    private static ForgeConfigSpec CLIENTSPEC;
    private static ForgeConfigSpec COMMONSPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENTSPEC = clientPair.getRight();
    }
    
    static {
        final Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMONSPEC = commonPair.getRight();
    }

    public static void preInit(ModLoadingContext context) { 
        context.registerConfig(Type.CLIENT, ModConfigs.CLIENTSPEC);
        context.registerConfig(Type.COMMON, ModConfigs.COMMONSPEC);
    }
    
    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading event) {
    }
}
