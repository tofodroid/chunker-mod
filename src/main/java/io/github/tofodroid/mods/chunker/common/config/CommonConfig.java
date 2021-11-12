package io.github.tofodroid.mods.chunker.common.config;

import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    protected ForgeConfigSpec.ConfigValue<String> enabledDimensionsString;
    public ForgeConfigSpec.BooleanValue openFirstSpawnChunk;
    public ForgeConfigSpec.BooleanValue openEnderPearlChunk;
    public ForgeConfigSpec.BooleanValue openDimensionChunk;

    public List<String> getEnabledDimensions() {
        String rawString = enabledDimensionsString.get();

        if(!rawString.trim().isEmpty()) {
            return Arrays.asList(rawString.split(","));
        }

        return Arrays.asList();
    }
    
    public CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("General Settings");
        enabledDimensionsString = builder.comment("Comma-Separated list of dimensions that should have Chunker enabled. You can get a list of all registered dimensions by running the command '/chunker dimensions' in game.")
            .translation(ChunkerMod.MODID + ".config.enabled.dimensions")
            .define("enabledDimensions", "minecraft:overworld");
        openFirstSpawnChunk = builder.comment("Whether or not the first chunk that any player spawns into in any enabled dimension should be automatically opened.")
            .translation(ChunkerMod.MODID + ".config.open.spawn")
            .define("openFirstSpawnChunk", true);
        openDimensionChunk = builder.comment("Whether or not any chunk that any player spawns into when traveling from another dimension to an enabled dimension should be opened if they're holding a Chunk Key.")
            .translation(ChunkerMod.MODID + ".config.open.dimension")
            .define("openDimensionChunk", true);
        openEnderPearlChunk = builder.comment("Whether or the chunk a player lands in after using an ender pearl should be opened if they're holding a Chunk Key.")
            .translation(ChunkerMod.MODID + ".config.open.pearl")
            .define("openEnderPearlChunk", true);
        builder.pop();
    }
}
