package io.github.tofodroid.mods.chunker.common;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.tofodroid.mods.chunker.client.ClientProxy;
import io.github.tofodroid.mods.chunker.common.config.ModConfigs;
import io.github.tofodroid.mods.chunker.common.network.NetworkManager;
import io.github.tofodroid.mods.chunker.server.ServerProxy;


@Mod(ChunkerMod.MODID)
public class ChunkerMod {
    public static final String MODID = "chunker";

    public static final Logger LOGGER = LogManager.getLogger();
    public static Proxy proxy = (Proxy)DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ChunkerMod() {
        ChunkerMod.preInit(FMLJavaModLoadingContext.get(), ModLoadingContext.get());
    }

    public static void preInit(FMLJavaModLoadingContext fmlContext, ModLoadingContext modContext) {
        // Event Listener Registration
        fmlContext.getModEventBus().addListener(ChunkerMod::init);

        // Other Pre-Init
        ModConfigs.preInit(modContext);
    }
    
    public static void init(final FMLCommonSetupEvent event) {
        NetworkManager.init(event);
        proxy.init(event);
    }
}
