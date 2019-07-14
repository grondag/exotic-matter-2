/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm2.dispatch;
//TODO: restore or remove

//package grondag.brocade.model.varia;
//
//
//
//
//import grondag.brocade.painting.PaintLayer;
//import grondag.brocade.model.state.ISuperModelState;
//import grondag.exotic_matter.model.texture.ITexturePalette;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.particle.ParticleDigging;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//
//
//
//public class ParticleDiggingSuperBlock extends ParticleDigging {
//
//    /**
//     * multiply resulting UVs by this factor to limit samples within a 1-box area of
//     * larger textures
//     */
//    protected float uvScale;
//
//    public ParticleDiggingSuperBlock(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
//            double ySpeedIn, double zSpeedIn, BlockState state, ISuperModelState modelState) {
//        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
//        int color = modelState.getColorARGB(PaintLayer.BASE);
//        this.particleRed = ((color >> 16) & 0xFF) / 255f;
//        this.particleGreen = ((color >> 8) & 0xFF) / 255f;
//        this.particleBlue = (color & 0xFF) / 255f;
//
//        Block block = state.getBlock();
//        this.particleAlpha = block.isTranslucent(state) ? ((color >> 24) & 0xFF) / 255f : 1f;
//        ITexturePalette tex = modelState.getTexture(PaintLayer.BASE);
//        this.particleTexture = tex.sampleSprite();
//        this.uvScale = 1f / tex.textureScale().sliceCount;
//    }
//
//    @Override
//    protected void multiplyColor(BlockPos p_187154_1_) {
//        // ignore
//    }
//
//    /** same as vanilla except for alpha and uvScale */
//    @Override
//    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks,
//            float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
//        float f = this.particleTexture
//                .getInterpolatedU((double) (this.particleTextureJitterX / 4.0F * 16.0F * this.uvScale));
//        float f1 = this.particleTexture
//                .getInterpolatedU((double) ((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F * this.uvScale));
//        float f2 = this.particleTexture
//                .getInterpolatedV((double) (this.particleTextureJitterY / 4.0F * 16.0F * this.uvScale));
//        float f3 = this.particleTexture
//                .getInterpolatedV((double) ((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F * this.uvScale));
//
//        assert !(f < this.particleTexture.getMinU() || f1 > this.particleTexture.getMaxU()
//                || f2 < this.particleTexture.getMinV()
//                || f3 > this.particleTexture.getMaxV()) : "UV out of range in ParticleDiggingSuperBlock.renderParticle";
//
//        float f4 = 0.1F * this.particleScale;
//        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
//        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
//        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
//        int i = this.getBrightnessForRender(partialTicks);
//        int j = i >> 16 & 65535;
//        int k = i & 65535;
//        buffer.pos((double) (f5 - rotationX * f4 - rotationXY * f4), (double) (f6 - rotationZ * f4),
//                (double) (f7 - rotationYZ * f4 - rotationXZ * f4)).tex((double) f, (double) f3)
//                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
//                .endVertex();
//        buffer.pos((double) (f5 - rotationX * f4 + rotationXY * f4), (double) (f6 + rotationZ * f4),
//                (double) (f7 - rotationYZ * f4 + rotationXZ * f4)).tex((double) f, (double) f2)
//                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
//                .endVertex();
//        buffer.pos((double) (f5 + rotationX * f4 + rotationXY * f4), (double) (f6 + rotationZ * f4),
//                (double) (f7 + rotationYZ * f4 + rotationXZ * f4)).tex((double) f1, (double) f2)
//                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
//                .endVertex();
//        buffer.pos((double) (f5 + rotationX * f4 - rotationXY * f4), (double) (f6 - rotationZ * f4),
//                (double) (f7 + rotationYZ * f4 - rotationXZ * f4)).tex((double) f1, (double) f3)
//                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
//                .endVertex();
//    }
//}
