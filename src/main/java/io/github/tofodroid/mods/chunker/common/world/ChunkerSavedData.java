package io.github.tofodroid.mods.chunker.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.storage.WorldSavedData;

public class ChunkerSavedData extends WorldSavedData {
    public static final String OPENED_CHUNKS_TAG = "openedChunks";
    public static final String CHUNKER_SAVE_NAME = "ChunkerSavedData";

    protected OrderedHashSet<ChunkPos> openedChunks = new OrderedHashSet<ChunkPos>();
    protected VoxelShape chunkShape = null;

    public ChunkerSavedData() {
        super(CHUNKER_SAVE_NAME);
    }

    public OrderedHashSet<ChunkPos> getOpenedChunks() {
        return openedChunks;
    }

    public Boolean isChunkOpen(ChunkPos pos) {
        return openedChunks.contains(pos);
    }

    public Boolean openChunk(ChunkPos newChunk) {
        if(openedChunks.add(newChunk)) {   
            this.setDirty(true);
            refreshChunkShape();
            return true;
        };
        return false;
    }
    
    public Boolean closeChunk(ChunkPos oldChunk) {
        if(openedChunks.elements().indexOf(oldChunk) >= 0) {
            openedChunks.remove(openedChunks.elements().indexOf(oldChunk));
            this.setDirty(true);
            refreshChunkShape();
            return true;
        }

        return false;
    }

    public void closeAll() {
        openedChunks.clear();
        this.setDirty(true);
        refreshChunkShape();
    }

    public Boolean hasOpenedChunks() {
        return !openedChunks.isEmpty();
    }

    public VoxelShape getChunkShape() {
        return this.chunkShape;
    }

    protected void refreshChunkShape() {
        chunkShape = ChunkerSavedData.buildShapeForChunks(openedChunks);
    }

    public static VoxelShape buildShapeForChunks(Set<ChunkPos> chunks) {
        VoxelShape finalShape = null;
        List<VoxelShape> inputShapes = new ArrayList<>();
        for(ChunkPos chunk : chunks) {
            inputShapes.add(VoxelShapes.create(chunk.getXStart(), 0, chunk.getZStart(), chunk.getXEnd()+1, 1, chunk.getZEnd()+1));
        }
        
        if(!inputShapes.isEmpty()) {
            VoxelShape resultShape = inputShapes.get(0);
            if(inputShapes.size() > 1) {
                for(int i = 1; i < inputShapes.size(); i++) {
                    resultShape = VoxelShapes.or(resultShape, inputShapes.get(i));
                }
            }
            finalShape = VoxelShapes.getFaceShape(resultShape, Direction.DOWN).simplify();
        }
        
        return finalShape;
    }

    public static OrderedHashSet<ChunkPos> intArrayToChunks(int[] coords) {
        OrderedHashSet<ChunkPos> chunks = new OrderedHashSet<ChunkPos>();

        if(coords.length > 0 && coords.length % 2 == 0) {
            chunks = new OrderedHashSet<ChunkPos>();
            for(int i = 0; i < coords.length - 1; i += 2) {
                chunks.add(new ChunkPos(coords[i], coords[i+1]));
            }
        } else if(coords.length > 0) {
            ChunkerMod.LOGGER.error("Attempted to decode ChunkPos coordinate array with odd number of elements.");
            return null;
        }

        return chunks;
    }
    
    public static int[] chunksToIntArray(OrderedHashSet<ChunkPos> chunks) {
        if(chunks != null && chunks.size() > 0) {
            int[] coords = new int[chunks.size() * 2];

            for(int i = 0; i < coords.length - 1; i += 2) {
                coords[i] = chunks.get(i/2).x;
                coords[i+1] = chunks.get(i/2).z;
            }

            return coords;
        }

        return new int[0];
    }

    @Override
    public void read(CompoundNBT tag) {
        openedChunks = intArrayToChunks(tag.getIntArray(OPENED_CHUNKS_TAG));

        if(openedChunks != null) {
            refreshChunkShape();
        } else {
            openedChunks = new OrderedHashSet<ChunkPos>();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        int[] coords = chunksToIntArray(openedChunks);

        if(coords != null && coords.length > 0) {
            tag.putIntArray(OPENED_CHUNKS_TAG, coords);
        } else if(tag.contains(OPENED_CHUNKS_TAG)) {
            tag.remove(OPENED_CHUNKS_TAG);
        }

        return tag;
    }
}