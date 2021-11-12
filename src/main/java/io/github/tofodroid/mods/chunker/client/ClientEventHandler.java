package io.github.tofodroid.mods.chunker.client;

import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Sets;

import java.awt.Color;

import io.github.tofodroid.mods.chunker.client.renderer.ChunkerRenderer;
import io.github.tofodroid.mods.chunker.common.config.ModConfigs;
import io.github.tofodroid.mods.chunker.common.item.ModItems;
import io.github.tofodroid.mods.chunker.common.network.NetworkManager;
import io.github.tofodroid.mods.chunker.common.network.PlayerBorderTeleportPacket;
import io.github.tofodroid.mods.chunker.common.world.ChunkerSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@Mod.EventBusSubscriber(value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public abstract class ClientEventHandler {
    public static final Double BORDER_BUFFER = 0.001;
    protected static VoxelShape chunkShape = null;
    protected static AxisAlignedBB lastContainedBox = null;

    public static void setChunkShape(VoxelShape newChunk) {
        chunkShape = newChunk;
        lastContainedBox = null;
    }
    
    @SubscribeEvent
    @SuppressWarnings("resource")
	public static void onRenderWorld(RenderWorldLastEvent e) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if(e.getPhase() == EventPriority.NORMAL && player != null) {
            // Render Open Chunks
            if(chunkShape != null) {
                ChunkerRenderer.renderChunkerBorders(e, chunkShape, ModConfigs.CLIENT.getBorderColor());

                if(player != null && player.isSneaking()) {
                    if(ModItems.CHUNKLOCK.equals(player.getHeldItemMainhand().getItem()) || ModItems.CHUNKKEY.equals(player.getHeldItemOffhand().getItem())) {
                        // Render ToClose Chunk
                        RayTraceResult block = player.pick(5.0d, 0.0f, false);
                        if(block.getType() == RayTraceResult.Type.BLOCK) {
                            BlockPos blockPos = ((BlockRayTraceResult)block).getPos();
                            List<AxisAlignedBB> bboxList = chunkShape.toBoundingBoxList();
                    
                            if(bboxList.stream().anyMatch(box -> {
                                    return box.contains(blockPos.getX(), box.minY + 0.05, blockPos.getZ());
                                })
                            ) {
                                ChunkPos chunk = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
                                ChunkerRenderer.renderChunkerBorders(e, ChunkerSavedData.buildShapeForChunks(Sets.newHashSet(chunk)), Color.RED);
                            }
                        }
                    }
                }
            }

            if(player != null && player.isSneaking()) {
                if(ModItems.CHUNKKEY.equals(player.getHeldItemMainhand().getItem()) || ModItems.CHUNKKEY.equals(player.getHeldItemOffhand().getItem())) {
                    // Render ToOpen Chunk
                    RayTraceResult block = player.pick(5.0d, 0.0f, false);
                    if(block.getType() == RayTraceResult.Type.BLOCK) {
                        BlockPos blockPos = ((BlockRayTraceResult)block).getPos();
                        List<AxisAlignedBB> bboxList = chunkShape != null ? chunkShape.toBoundingBoxList() : new ArrayList<>();
                
                        if(!bboxList.stream().anyMatch(box -> {
                                return box.contains(blockPos.getX(), box.minY + 0.05, blockPos.getZ());
                            })
                        ) {
                            ChunkPos chunk = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
                            ChunkerRenderer.renderChunkerBorders(e, ChunkerSavedData.buildShapeForChunks(Sets.newHashSet(chunk)), Color.GREEN);
                        }
                    }
                } else if(ModItems.CHUNKKEY.equals(player.getHeldItemMainhand().getItem()) || ModItems.CHUNKKEY.equals(player.getHeldItemOffhand().getItem())) {
                    // Render Anchor Chunk
                    ChunkPos chunk = new ChunkPos(player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
                    ChunkerRenderer.renderChunkerBorders(e, ChunkerSavedData.buildShapeForChunks(Sets.newHashSet(chunk)), Color.BLUE);
                }
            }
        }
    }

    @SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent e) {
        if(e.player instanceof ClientPlayerEntity && ((ClientPlayerEntity)e.player).isUser() && chunkShape != null && Phase.START.equals(e.phase) && e.player.isAlive() && e.player.isAddedToWorld() && e.player.canUpdate()) {
            List<AxisAlignedBB> bboxList = chunkShape.toBoundingBoxList();
            
            if(!bboxList.stream().anyMatch(box -> {
                    Boolean contained = box.contains(e.player.getPosX(), box.minY + 0.05, e.player.getPosZ());
                    if(contained) {
                        lastContainedBox = box;
                    }
                    return contained;
                })
            ) {
                if(lastContainedBox == null || !chunkShape.toBoundingBoxList().contains(lastContainedBox)) {
                    AxisAlignedBB nearestBox = bboxList.get(0);
                    Double nearestDistance = nearestBox.rayTrace(e.player.getPositionVec(), nearestBox.getCenter()).orElse(nearestBox.getCenter()).distanceTo(e.player.getPositionVec());

                    if(bboxList.size() > 1) { 
                        for(int i = 1; i < bboxList.size(); i++) {
                            Double boxDistance = bboxList.get(i).rayTrace(e.player.getPositionVec(), bboxList.get(i).getCenter()).orElse(bboxList.get(i).getCenter()).distanceTo(e.player.getPositionVec());
                            if(boxDistance < nearestDistance) {
                                nearestBox = bboxList.get(i);
                                nearestDistance = boxDistance;
                            }
                        }
                    }
                    lastContainedBox = nearestBox;
                }
                
                Entity entity = e.player;
                Boolean doDismount = false;
                Boolean doTeleport = false;
                Boolean positiveX = null;
                Boolean positiveZ = null;

                if(e.player.getRidingEntity() != null) { 
                    if(e.player.getRidingEntity().canPassengerSteer() && e.player.getRidingEntity().getRidingEntity() == null) {
                        entity = e.player.getRidingEntity();
                    } else {
                        doDismount = true;
                    }
                }

                if(entity.getPosX() > lastContainedBox.maxX) {
                    positiveX = true;
                } else if(entity.getPosX() < lastContainedBox.minX) {
                    positiveX = false;
                }
                
                if(entity.getPosZ() > lastContainedBox.maxZ) {
                    positiveZ = true;
                } else if(entity.getPosZ() < lastContainedBox.minZ) {
                    positiveZ = false;
                }

                Vector3d newPositionVec = new Vector3d(
                    positiveX != null ? positiveX ? lastContainedBox.maxX - BORDER_BUFFER : lastContainedBox.minX + BORDER_BUFFER : entity.getPositionVec().x,
                    entity.getPositionVec().y,
                    positiveZ != null ? positiveZ ? lastContainedBox.maxZ - BORDER_BUFFER : lastContainedBox.minZ + BORDER_BUFFER : entity.getPositionVec().z
                );

                Vector3d newMotionVec = new Vector3d(
                    positiveX != null ? 0 : entity.getMotion().x,
                    entity.getMotion().y,
                    positiveZ != null ? 0 : entity.getMotion().z
                );

                Double positionDistance = entity.getDistanceSq(newPositionVec);

                if(positionDistance >= 17.0d) {
                    entity = e.player;
                    doDismount = true;
                    doTeleport = true;
                }

                entity.setPosition(newPositionVec.x, newPositionVec.y, newPositionVec.z);
                entity.setMotion(newMotionVec.x, newMotionVec.y, newMotionVec.z);

                if(entity instanceof ClientPlayerEntity) {
                    // If target location is going to suffocate try to move the player
                    if(entity.getEntityWorld().getBlockState(entity.getPosition().up()).isSuffocating(entity.getEntityWorld(), entity.getPosition().up())) {
                        newPositionVec = new Vector3d(
                            Math.floor(newPositionVec.x) + (positiveX != null ? positiveX ? -1 : 1 : 0),
                            Math.floor(newPositionVec.y),
                            Math.floor(newPositionVec.z) + (positiveZ != null ? positiveZ ? -1 : 1 : 0)
                        );
                        newMotionVec = new Vector3d(newMotionVec.x, 0, newMotionVec.z);
                        entity.setPosition(newPositionVec.x, newPositionVec.y, newPositionVec.z);
                        entity.setMotion(newMotionVec.x, newMotionVec.y, newMotionVec.z);
                    }

                    // Sync data with server if we need to move a significant distance or dismount
                    if(doDismount || doTeleport) {
                        NetworkManager.NET_CHANNEL.sendToServer(new PlayerBorderTeleportPacket(entity.getPositionVec(), doDismount, doTeleport));
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
	public static void onPlayerLoggedOut(PlayerLoggedOutEvent e) {
        if(e.getPlayer() instanceof ClientPlayerEntity && ((ClientPlayerEntity)e.getPlayer()).isUser()) {
            setChunkShape(null);
        }
    }
}
