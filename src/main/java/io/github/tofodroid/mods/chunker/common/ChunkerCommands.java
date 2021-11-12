package io.github.tofodroid.mods.chunker.common;

import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import io.github.tofodroid.mods.chunker.common.network.NetworkManager;
import io.github.tofodroid.mods.chunker.common.network.OpenChunksSyncPacket;
import io.github.tofodroid.mods.chunker.server.ServerEventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ChunkerMod.MODID)
public class ChunkerCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("chunker")
                .then(Commands.literal("open")
                    .requires(cs -> cs.getServer().isSinglePlayer() || cs.hasPermissionLevel(3))
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(ctx -> open(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z, IntegerArgumentType.getInteger(ctx, "radius"))) 
                            )
                            .executes(ctx -> open(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z, 0))
                        )
                    )
                    .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                            .executes(ctx -> open(ctx.getSource(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z, IntegerArgumentType.getInteger(ctx, "radius"))) 
                        )
                        .executes(ctx -> open(ctx.getSource(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z, 0)) 
                    )
                    .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                        .executes(ctx -> open(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))
                    )
                    .executes(ctx -> open(ctx.getSource(), 0))
                )
                .then(Commands.literal("close")
                    .requires(cs -> cs.getServer().isSinglePlayer() || cs.hasPermissionLevel(3))
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(ctx -> close(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z))
                        )
                    )
                    .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .executes(ctx -> close(ctx.getSource(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z)) 
                    )
                    .then(Commands.literal("all")
                        .then(Commands.argument("dim", DimensionArgument.getDimension())
                            .executes(ctx -> closeAll(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey()))
                        )
                        .executes(ctx -> closeAll(ctx.getSource()))
                    )
                    .executes(ctx -> close(ctx.getSource()))
                )
                .then(Commands.literal("list")
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                            .executes(ctx -> list(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), IntegerArgumentType.getInteger(ctx, "page")))
                        )
                        .executes(ctx -> list(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), 1))
                    )
                    .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> list(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                    )
                    .executes(ctx -> list(ctx.getSource(), 1))
                )
                .then(Commands.literal("info")
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(ctx -> info(ctx.getSource(), DimensionArgument.getDimensionArgument(ctx, "dim").getDimensionKey(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z))
                        )
                    )
                    .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .executes(ctx -> info(ctx.getSource(), ColumnPosArgument.fromBlockPos(ctx, "pos").x, ColumnPosArgument.fromBlockPos(ctx, "pos").z)) 
                    )
                    .executes(ctx -> info(ctx.getSource()))
                )
                .then(Commands.literal("dimensions")
                    .executes(ctx -> dimensions(ctx.getSource()))
                )
        );
	}

	private static int open(CommandSource source, Integer radius) {
        try {
            return open(source, source.asPlayer().world.getDimensionKey(), source.asPlayer().getPosition().getX(), source.asPlayer().getPosition().getZ(), radius);
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), true);
        }
        return 1;
    }        

	private static int open(CommandSource source, Integer blockX, Integer blockZ,  Integer radius) {
        try {
            return open(source, source.asPlayer().world.getDimensionKey(), blockX, blockZ, radius);
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), true);
        }
        return 1;
    }

	private static int open(CommandSource source, RegistryKey<World> dimension, Integer blockX, Integer blockZ, Integer radius) {
        Integer x = blockX >> 4;
        Integer z = blockZ >> 4;
        if(ServerEventHandler.chunkerData.containsKey(dimension)) {
            for(int xi = x-radius; xi <= x+radius; xi++) {
                for(int zi = z-radius; zi <= z+radius; zi++) {
                    ServerEventHandler.chunkerData.get(dimension).openChunk(new ChunkPos(xi,zi));
                }
            }
            NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), new OpenChunksSyncPacket(ServerEventHandler.chunkerData.get(dimension).getOpenedChunks()));
            source.sendFeedback(new StringTextComponent("Chunks Opened: Center Chunk [" + x + ", " + z + "] | Radius: " + radius), true);
        } else {
            source.sendFeedback(new StringTextComponent("Current dimension is not enabled in Chunker."), true);
        }

        return 1;
    }

    private static int close(CommandSource source) {
        try {
            return close(source, source.asPlayer().world.getDimensionKey(), source.asPlayer().getPosition().getX(), source.asPlayer().getPosition().getZ());
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), true);
        }
        return 1;
    }        

	private static int close(CommandSource source, Integer blockX, Integer blockZ) {
        try {
            return close(source, source.asPlayer().world.getDimensionKey(), blockX, blockZ);
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), true);
        }
        return 1;
    }

	private static int close(CommandSource source, RegistryKey<World> dimension, Integer blockX, Integer blockZ) {
        Integer x = blockX >> 4;
        Integer z = blockZ >> 4;
        if(ServerEventHandler.chunkerData.containsKey(dimension)) {
            ServerEventHandler.chunkerData.get(dimension).closeChunk(new ChunkPos(x,z));
            NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), new OpenChunksSyncPacket(ServerEventHandler.chunkerData.get(dimension).getOpenedChunks()));
            source.sendFeedback(new StringTextComponent("Chunk Closed: [" + x + ", " + z + "]"), true);
        } else {
            source.sendFeedback(new StringTextComponent("Current dimension is not enabled in Chunker."), true);
        }
        return 1;
    }

	private static int closeAll(CommandSource source) {
        try {
            return closeAll(source, source.asPlayer().world.getDimensionKey());
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), false);
        }
        return 1;
    }

	private static int closeAll(CommandSource source, RegistryKey<World> dimension) {
        if(ServerEventHandler.chunkerData.containsKey(dimension)) {
            ServerEventHandler.chunkerData.get(dimension).closeAll();
            NetworkManager.NET_CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), new OpenChunksSyncPacket(ServerEventHandler.chunkerData.get(dimension).getOpenedChunks()));
            source.sendFeedback(new StringTextComponent("All Chunks Closed"), true);
        } else {
            source.sendFeedback(new StringTextComponent("Target dimension is not enabled in Chunker."), true);
        }
        return 1;
    }


	private static int info(CommandSource source) {
        try {
            return info(source, source.asPlayer().world.getDimensionKey(), source.asPlayer().getPosition().getX(), source.asPlayer().getPosition().getZ());
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), false);
        }
        return 1;
    }

	private static int info(CommandSource source, Integer x, Integer z) {
        try {
            return info(source, source.asPlayer().world.getDimensionKey(), x, z);
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), false);
        }
        return 1;
    }

	private static int info(CommandSource source, RegistryKey<World> dimension, Integer blockX, Integer blockZ) {
        if(ServerEventHandler.chunkerData.containsKey(dimension)) {
            ChunkPos currentChunk = new ChunkPos(blockX >> 4, blockZ >> 4);
            source.sendFeedback(new StringTextComponent("Chunk: [" + currentChunk.x + ", " + currentChunk.z + "] - " + (ServerEventHandler.chunkerData.get(dimension).isChunkOpen(currentChunk) ? "Open" : "Closed")), false);
        } else {
            source.sendFeedback(new StringTextComponent("Current dimension is not enabled in Chunker."), false);
        }
        return 1;
    }

	private static int list(CommandSource source, Integer page) {
        try {
            return list(source, source.asPlayer().world.getDimensionKey(), page);
        } catch(CommandSyntaxException e) {
            ChunkerMod.LOGGER.warn("Command Syntax Error: ", e);
            source.sendFeedback(new StringTextComponent("Command can only be run by a player."), false);
        }
        return 1;
    }

	private static int list(CommandSource source, RegistryKey<World> dimension, Integer page) {
        if(ServerEventHandler.chunkerData.containsKey(dimension)) {
            OrderedHashSet<ChunkPos> openChunks = ServerEventHandler.chunkerData.get(dimension).getOpenedChunks();
            page--;

            if(openChunks.size() > (5*page)) {
                String commandString = "Open Chunks (" + (page+1) + "/" + (openChunks.size()/5 + (openChunks.size() % 5 == 0 ? 0 : 1)) + "):\n";

                for(int i = 5*page; i < (5*page)+5; i++) {
                    if(openChunks.size() <= i) {
                        break;
                    }
                    ChunkPos chunk = openChunks.get(i);
                    commandString += (i+1) + ". [" + chunk.x + ", " + chunk.z + "]\n";
                }
                source.sendFeedback(new StringTextComponent(commandString), false);
            } else {
                source.sendFeedback(new StringTextComponent("Invalid page number"), false);
            }
        } else {
            source.sendFeedback(new StringTextComponent("Current dimension is not enabled in Chunker."), false);
        }
        return 1;
    }

	private static int dimensions(CommandSource source) {
        String dimensionList = source.func_241861_q().getRegistry(Registry.DIMENSION_TYPE_KEY).getEntries().stream().map(k -> "  - " + k.getKey().getLocation().toString()).collect(Collectors.joining("\n"));
        source.sendFeedback(new StringTextComponent("Dimensions:\n" + dimensionList), false);
        return 1;
    }
}
