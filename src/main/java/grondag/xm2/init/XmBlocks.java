package grondag.xm2.init;

import grondag.xm2.Xm;
import grondag.xm2.block.XmBorderMatch;
import grondag.xm2.block.XmSimpleBlock;
import grondag.xm2.painting.PaintLayer;
import grondag.xm2.placement.XmBlockItem;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.ModelStateImpl;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class XmBlocks {
    
    
    public static final void init() {
        Xm.LOG.debug("Registering Exotic Matter Test Blocks");
        
        // use for registration
        // XmBorderMatch.INSTANCE
        // XmBlock.defaultModelStateFunc
        
        ModelState workingModel = new ModelStateImpl();
        workingModel.setShape(ModShapes.WEDGE);
        workingModel.setTexture(PaintLayer.BASE, XmTextures.WHITE);
        workingModel.setColorRGB(PaintLayer.BASE, 0xFFFFFFFF);
        register(XmSimpleBlock.create(FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), workingModel), "test_wedge");
        
        workingModel = new ModelStateImpl();
        workingModel.setShape(ModShapes.CUBE);
        workingModel.setTexture(PaintLayer.BASE, XmTextures.WHITE);
        workingModel.setColorRGB(PaintLayer.BASE, 0xFFFFFFFF);
        register(XmSimpleBlock.create(FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), workingModel), "test_cube");
    }
    
//        workingModel.setTexture(PaintLayer.MIDDLE, BrocadeTextures.BLOCK_COBBLE);
//        workingModel.setColorRGB(PaintLayer.MIDDLE, 0xFF4444);
//        workingModel.setTranslucent(PaintLayer.MIDDLE, true);
//        workingModel.setAlpha(PaintLayer.MIDDLE, 64);
//        workingModel.setEmissive(PaintLayer.MIDDLE, true);
//        workingModel.setTexture(PaintLayer.OUTER, BrocadeTextures.BORDER_CAUTION);
//        workingModel.setColorRGB(PaintLayer.OUTER, 0xD7FFFF);
//        workingModel.setColorRGB(PaintLayer.OUTER, 0xFFFFD300);
//        workingModel.setTranslucent(PaintLayer.OUTER, true);
//        workingModel.setEmissive(PaintLayer.OUTER, true);
    
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
