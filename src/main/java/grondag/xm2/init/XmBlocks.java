/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm2.init;

import grondag.xm2.Xm;
import grondag.xm2.block.XmSimpleBlock;
import grondag.xm2.model.impl.state.ModelState;
import grondag.xm2.model.impl.state.ModelStateImpl;
import grondag.xm2.paint.api.XmPaint;
import grondag.xm2.paint.api.XmPaintFinder;
import grondag.xm2.placement.XmBlockItem;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class XmBlocks {
    public static final void init() {
        Xm.LOG.debug("Registering Exotic Matter Test Blocks");
        
        final XmPaintFinder paintFinder = XmPaint.finder();
        XmPaint paint = paintFinder.texture(0, XmTextures.WHITE).textureColor(0, 0xFFFFFFFF).find();
        ModelState workingModel = new ModelStateImpl();
        workingModel.setShape(ModShapes.WEDGE);
        workingModel.paintAll(paint);
        register(new XmSimpleBlock(FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), workingModel), "test_wedge");
        
        workingModel = new ModelStateImpl();
        workingModel.setShape(ModShapes.CUBE);
        workingModel.paintAll(paint);
        register(new XmSimpleBlock(FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), workingModel), "test_cube");
        
        workingModel = new ModelStateImpl();
        workingModel.setShape(ModShapes.CUBE);
        paint = paintFinder.textureDepth(2).texture(0, XmTextures.SANDSTONE_ZOOM).textureColor(0, 0xFF808590)
        		.blendMode(1, BlockRenderLayer.TRANSLUCENT).emissive(1, true)
        		.texture(1, XmTextures.BORDER_CAUTION).textureColor(1, 0xFFFFD300).find();
        workingModel.paintAll(paint);
        register(new XmSimpleBlock(FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), workingModel), "test_borders");
    }
    
    private static void register(Block block, String name) {
        Identifier id = new Identifier(Xm.MODID, name);
        Registry.BLOCK.add(id, block);
        Registry.ITEM.add(id, new XmBlockItem(block, 
                new Item.Settings().maxCount(64).group(ItemGroup.BUILDING_BLOCKS)));
    }
    
//  SuperModelBlock.registerSuperModelBlocks(event);
//
//  // TODO: disable test blocks
//  ISuperModelState workingModel;
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.CSGTEST);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_STRONG);
//  workingModel.setColorRGB(PaintLayer.BASE, 0xFF9B898C);
//  workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//  workingModel.setColorRGB(PaintLayer.CUT, 0xFF7F9BA6);
//
//  workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//  workingModel.setColorRGB(PaintLayer.LAMP, 0xFFD5E9FF);
//  event.getRegistry().register(
//          new SuperSimpleBlock(Brocade.INSTANCE.prefixName("csgtest"), BlockSubstance.DEFAULT, workingModel)
//                  .setCreativeTab(Brocade.tabMod));
//
//
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.SPHERE);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_COBBLE);
//  workingModel.setColorRGB(PaintLayer.BASE, 0xBBC3C4);
//  event.getRegistry().register(new SuperSimpleBlock(Brocade.INSTANCE.prefixName("spheretest"),
//          BlockSubstance.DEFAULT, workingModel).setCreativeTab(Brocade.tabMod));
//
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.COLUMN_SQUARE);
//  SquareColumnMeshFactory.setCutCount(3, workingModel);
//  SquareColumnMeshFactory.setCutsOnEdge(true, workingModel);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//  workingModel.setColorRGB(PaintLayer.BASE, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//  workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//  workingModel.setColorRGB(PaintLayer.CUT, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//  workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.WHITE);
//  workingModel.setEmissive(PaintLayer.LAMP, true);
//  workingModel.setColorRGB(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.CYAN, Chroma.RICH, Luminance.BRIGHT).getColor(EnumColorMap.LAMP));
//  event.getRegistry().register(
//          new SuperSimpleBlock(Brocade.INSTANCE.prefixName("coltest"), BlockSubstance.DEFAULT, workingModel)
//                  .setCreativeTab(Brocade.tabMod));
}
