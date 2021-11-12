package io.github.tofodroid.mods.chunker.common.config;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.Color;

public class ClientConfig {
    protected ForgeConfigSpec.IntValue borderColorRed;
    protected ForgeConfigSpec.IntValue borderColorGreen;
    protected ForgeConfigSpec.IntValue borderColorBlue;
    protected ForgeConfigSpec.IntValue borderColorAlpha;
    public ForgeConfigSpec.BooleanValue enableBorderAnimation;

    public Color getBorderColor() {
        return new Color(borderColorRed.get(), borderColorGreen.get(), borderColorBlue.get(), borderColorAlpha.get());
    }

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Render Settings");
        borderColorRed = builder.comment("An integer from 0 to 255 representing the Red color component of the border.")
            .translation(ChunkerMod.MODID + ".config.border.color.red")
            .defineInRange("borderColorRed", 255, 0, 255);
        borderColorGreen = builder.comment("An integer from 0 to 255 representing the Green color component of the border.")
            .translation(ChunkerMod.MODID + ".config.border.color.green")
            .defineInRange("borderColorGreen", 40, 0, 255);
        borderColorBlue = builder.comment("An integer from 0 to 255 representing the Blue color component of the border.")
            .translation(ChunkerMod.MODID + ".config.border.color.blue")
            .defineInRange("borderColorBlue", 255, 0, 255);
        borderColorAlpha = builder.comment("An integer from 0 to 255 representing Alpha (transparency) color component of the border.")
            .translation(ChunkerMod.MODID + ".config.border.color.red")
            .defineInRange("borderColorAlpha", 115, 0, 255);
        enableBorderAnimation = builder.comment("Whether or not the border should animate.")
            .translation(ChunkerMod.MODID + ".config.border.animate")
            .define("enableBorderAnimation", true);
        builder.pop();
    }
}
