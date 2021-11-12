package io.github.tofodroid.mods.chunker.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.github.tofodroid.mods.chunker.common.ChunkerMod;

import java.awt.Color;

public abstract class ChunkerRenderer {
    public static final float BORDER_RENDER_OFFSET = 0.05f;
    
    public static RenderType getChunkBorderRenderType(ResourceLocation locationIn) {
        RenderType.State rendertype$state = 
        RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(locationIn, false, false))
            .transparency(new RenderState.TransparencyState("translucent_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
             }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
             }))
            .fog(new RenderState.FogState("no_fog", () -> {}, () -> {}))
            .writeMask(new RenderState.WriteMaskState(true, false))
            .cull(new RenderState.CullState(false))
            .build(false);
        return RenderType.makeType("chunker_border", DefaultVertexFormats.BLOCK, 7, 256, false, true, rendertype$state);
    }

    @SuppressWarnings("resource")
    public static void renderChunkerBorders(RenderWorldLastEvent e, VoxelShape chunkShape, Color color) {
        drawChunkBorders(e.getContext(), e.getFinishTimeNano()/1000000l, Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), e.getMatrixStack(), BlockPos.ZERO, Minecraft.getInstance().gameRenderer.getActiveRenderInfo(), chunkShape, color);
    }

    private static void drawChunkBorders(WorldRenderer worldRenderer, long milliTime, IRenderTypeBuffer renderTypeBuffers, MatrixStack matrixStack, BlockPos blockPos, ActiveRenderInfo activeRenderInfo, VoxelShape shape, Color color) {
        RenderType renderType = getChunkBorderRenderType(new ResourceLocation(ChunkerMod.MODID, "textures/border.png"));
        Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);

        double eyeX = activeRenderInfo.getProjectedView().getX();
        double eyeY = activeRenderInfo.getProjectedView().getY();
        double eyeZ = activeRenderInfo.getProjectedView().getZ();
        matrixStack.push();
        drawShapeOutline(matrixStack, milliTime, vertexBuilder, shape, blockPos.getX() - eyeX, blockPos.getY() - eyeY, blockPos.getZ() - eyeZ, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        matrixStack.pop();
        buffer.finish(renderType);
    }

    @SuppressWarnings("resource")
    private static void drawShapeOutline(MatrixStack matrixStack, long milliTime, IVertexBuilder vertexBuilder, VoxelShape voxelShape, double originX, double originY, double originZ, float red, float green, float blue, float alpha) {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if(player != null) {
            Matrix4f matrix4f = matrixStack.getLast().getMatrix();
            Matrix3f matrix3f = matrixStack.getLast().getNormal();  
            float textOffsetA = (float)-1*((milliTime % 2300L) / 2300.0F);
            float textOffsetB = (float)-1*((milliTime % 1300L) / 1300.0F);

            voxelShape.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
                float renderOffsetX0 = player.getPosX() >= x0 ? -BORDER_RENDER_OFFSET : BORDER_RENDER_OFFSET;
                float renderOffsetX1 = player.getPosX() >= x1 ? -BORDER_RENDER_OFFSET : BORDER_RENDER_OFFSET;
                float renderOffsetZ0 = player.getPosZ() >= z0 ? -BORDER_RENDER_OFFSET : BORDER_RENDER_OFFSET;
                float renderOffsetZ1 = player.getPosZ() >= z1 ? -BORDER_RENDER_OFFSET : BORDER_RENDER_OFFSET;
    
                if(Math.abs(x0-x1) > 0) {
                    Integer numWalls = new Double(Math.abs(x0-x1)/16).intValue();
                    for(int i = 0; i < numWalls; i++) {
                        Double distanceToWall = player.getPositionVec().distanceTo(new Vector3d(x0 + i*16 + 8, player.getPosY(), z0 + 8));
                        Float finalAlpha = alpha - 0.8f*alpha*(float)(distanceToWall / ((Minecraft.getInstance().gameSettings.renderDistanceChunks + 2) * 16));
                            
                        if(finalAlpha > 0) {
                            buildQuad(vertexBuilder, matrix4f, matrix3f, x0 + i*16 + originX + (i == 0 ? renderOffsetX0 : 0), 0 + originY, z0 + originZ + renderOffsetZ0, x0 + i*16 + 16 + originX + (i == (numWalls - 1) ? renderOffsetX1 : 0), player.getPosition().getY() + 32 + originY, z1 + originZ + renderOffsetZ1, red, green, blue, finalAlpha, textOffsetA);
                            buildQuad(vertexBuilder, matrix4f, matrix3f, x0 + i*16 + originX + (i == 0 ? renderOffsetX0 : 0), 0 + originY, z0 + originZ + renderOffsetZ0, x0 + i*16 + 16 + originX + (i == (numWalls - 1) ? renderOffsetX1 : 0), player.getPosition().getY() + 32 + originY, z1 + originZ + renderOffsetZ1, red, green, blue, finalAlpha, textOffsetB);
                        }
                    }
                } else {
                    Integer numWalls = new Double(Math.abs(z0-z1)/16).intValue();
                    for(int i = 0; i < numWalls; i++) {
                        Double distanceToWall = player.getPositionVec().distanceTo(new Vector3d(x0 + 8, player.getPosY(), z0 + i*16 + 8));
                        Float finalAlpha = alpha - 0.8f*alpha*(float)(distanceToWall / ((Minecraft.getInstance().gameSettings.renderDistanceChunks + 2) * 16));
                        
                        if(finalAlpha > 0) {
                            buildQuad(vertexBuilder, matrix4f, matrix3f, x0 + originX + renderOffsetX0, 0 + originY, z0 + i*16 + originZ + (i == 0 ? renderOffsetZ0 : 0), x1 + originX + renderOffsetX1, player.getPosition().getY() + 32 + originY, z0 + i*16 + 16 + originZ + (i == (numWalls - 1) ? renderOffsetZ1 : 0), red, green, blue, finalAlpha, textOffsetA);
                            buildQuad(vertexBuilder, matrix4f, matrix3f, x0 + originX + renderOffsetX0, 0 + originY, z0 + i*16 + originZ + (i == 0 ? renderOffsetZ0 : 0), x1 + originX + renderOffsetX1, player.getPosition().getY() + 32 + originY, z0 + i*16 + 16 + originZ + (i == (numWalls - 1) ? renderOffsetZ1 : 0), red, green, blue, finalAlpha, textOffsetB);
                        }
                    }
                }
            });
        }
    }

    private static void buildQuad(IVertexBuilder vertexBuilder, Matrix4f sourceMatrix, Matrix3f normalMatrix, double x0, double y0, double z0, double x1, double y1, double z1, float red, float green, float blue, float alpha, float textOffset) {
        vertexBuilder.pos(sourceMatrix, (float)(x0), (float)(y0), (float)(z0)).color(red, green, blue, alpha).tex(textOffset, textOffset).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        vertexBuilder.pos(sourceMatrix, (float)(x1), (float)(y0), (float)(z1)).color(red, green, blue, alpha).tex(textOffset + 16f, textOffset).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        vertexBuilder.pos(sourceMatrix, (float)(x1), (float)(y1), (float)(z1)).color(red, green, blue, alpha).tex(textOffset + 16f, textOffset + 256f).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        vertexBuilder.pos(sourceMatrix, (float)(x0), (float)(y1), (float)(z0)).color(red, green, blue, alpha).tex(textOffset, textOffset + 256f).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
