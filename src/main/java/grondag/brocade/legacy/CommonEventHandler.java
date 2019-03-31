//TODO: remove or redo

//package grondag.brocade.legacy;
//
//import java.util.Map;
//
//import grondag.brocade.BrocadeConfig;
//import grondag.brocade.block.BlockSubstance;
//import grondag.brocade.block.SuperModelBlock;
//import grondag.brocade.block.SuperSimpleBlock;
//import grondag.brocade.init.IBlockItemRegistrator;
//import grondag.brocade.init.ModShapes;
//import grondag.fermion.color.BlockColorMapProvider;
//import grondag.fermion.color.Chroma;
//import grondag.fermion.color.ColorMap.EnumColorMap;
//import grondag.fermion.color.Hue;
//import grondag.fermion.color.Luminance;
//import grondag.exotic_matter.model.mesh.SquareColumnMeshFactory;
//import grondag.brocade.painting.PaintLayer;
//import grondag.brocade.model.state.ISuperModelState;
//import grondag.brocade.model.state.ModelState;
//import grondag.exotic_matter.player.ModifierKeys;
//import grondag.exotic_matter.simulator.Simulator;
//import grondag.exotic_matter.simulator.WorldTaskManager;
//import net.minecraft.block.Block;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.item.Item;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.common.config.Config.Type;
//import net.minecraftforge.common.config.ConfigManager;
//import net.minecraftforge.event.AttachCapabilitiesEvent;
//import net.minecraftforge.event.RegistryEvent;
//import net.minecraftforge.fml.client.event.ConfigChangedEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
//import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
//import net.minecraftforge.fml.common.registry.GameRegistry;
//import net.minecraftforge.registries.IForgeRegistry;
//
//@Mod.EventBusSubscriber
//public class CommonEventHandler {
//    @SubscribeEvent
//    public static void onServerTick(ServerTickEvent event) {
//        if (event.phase == Phase.START) {
//            // noop
//        } else {
//            WorldTaskManager.doServerTick();
//
//            // thought it might be more determinism if simulator runs after block/entity
//            // ticks
//            Simulator.instance().onServerTick(event);
//        }
//    }
//
//    @SubscribeEvent
//    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
//        if (event.getModID().equals(Brocade.MODID)) {
//            ConfigManager.sync(Brocade.MODID, Type.INSTANCE);
//            BrocadeConfig.recalcDerived();
//        }
//    }
//
//    @SubscribeEvent
//    public static void registerBlocks(RegistryEvent.Register<Block> event) {
//        SuperModelBlock.registerSuperModelBlocks(event);
//
//        // TODO: disable test blocks
//        ISuperModelState workingModel;
//        workingModel = new ModelState();
//        workingModel.setShape(ModShapes.CSGTEST);
//        workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_STRONG);
//        workingModel.setColorRGB(PaintLayer.BASE, 0xFF9B898C);
//        workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//        workingModel.setColorRGB(PaintLayer.CUT, 0xFF7F9BA6);
//
//        workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//        workingModel.setColorRGB(PaintLayer.LAMP, 0xFFD5E9FF);
//        event.getRegistry().register(
//                new SuperSimpleBlock(Brocade.INSTANCE.prefixName("csgtest"), BlockSubstance.DEFAULT, workingModel)
//                        .setCreativeTab(Brocade.tabMod));
//
//        workingModel = new ModelState();
//        workingModel.setShape(ModShapes.CUBE);
//        workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//        workingModel.setColorRGB(PaintLayer.BASE, 0xEBF0F5);
//        workingModel.setTexture(PaintLayer.MIDDLE, grondag.brocade.init.BrocadeTextures.BLOCK_COBBLE);
//        workingModel.setColorRGB(PaintLayer.MIDDLE, 0xFF4444);
//        workingModel.setTranslucent(PaintLayer.MIDDLE, true);
//        workingModel.setAlpha(PaintLayer.MIDDLE, 32);
//        workingModel.setEmissive(PaintLayer.MIDDLE, true);
//        workingModel.setTexture(PaintLayer.OUTER, grondag.brocade.init.BrocadeTextures.BORDER_SMOOTH_BLEND);
//        workingModel.setColorRGB(PaintLayer.OUTER, 0xD7FFFF);
//        workingModel.setTranslucent(PaintLayer.OUTER, true);
//        workingModel.setEmissive(PaintLayer.OUTER, true);
//        event.getRegistry().register(new SuperSimpleBlock(Brocade.INSTANCE.prefixName("blocktest"),
//                BlockSubstance.DEFAULT, workingModel).setCreativeTab(Brocade.tabMod));
//
//        workingModel = new ModelState();
//        workingModel.setShape(ModShapes.SPHERE);
//        workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_COBBLE);
//        workingModel.setColorRGB(PaintLayer.BASE, 0xBBC3C4);
//        event.getRegistry().register(new SuperSimpleBlock(Brocade.INSTANCE.prefixName("spheretest"),
//                BlockSubstance.DEFAULT, workingModel).setCreativeTab(Brocade.tabMod));
//
//        workingModel = new ModelState();
//        workingModel.setShape(ModShapes.COLUMN_SQUARE);
//        SquareColumnMeshFactory.setCutCount(3, workingModel);
//        SquareColumnMeshFactory.setCutsOnEdge(true, workingModel);
//        workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//        workingModel.setColorRGB(PaintLayer.BASE, BlockColorMapProvider.INSTANCE
//                .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//        workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//        workingModel.setColorRGB(PaintLayer.CUT, BlockColorMapProvider.INSTANCE
//                .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//        workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.WHITE);
//        workingModel.setEmissive(PaintLayer.LAMP, true);
//        workingModel.setColorRGB(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE
//                .getColorMap(Hue.CYAN, Chroma.RICH, Luminance.BRIGHT).getColor(EnumColorMap.LAMP));
//        event.getRegistry().register(
//                new SuperSimpleBlock(Brocade.INSTANCE.prefixName("coltest"), BlockSubstance.DEFAULT, workingModel)
//                        .setCreativeTab(Brocade.tabMod));
//
//    }
//
//    @SubscribeEvent
//    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
//        if (event.getObject() instanceof EntityPlayer) {
//            if (!event.getObject().hasCapability(ModifierKeys.CAP_INSTANCE, null)) {
//                event.addCapability(new ResourceLocation(Brocade.MODID, "PlayerCaps"), new ModifierKeys());
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void registerItems(RegistryEvent.Register<Item> event) {
//        handleRegisterItems(Brocade.MODID, event);
//    }
//
//    /**
//     * Call from each mod's event handler. Could do all in library mod handler but
//     * Forge will spam warning messages because domain names don't match the current
//     * handler.
//     */
//    @SuppressWarnings("null")
//    public static void handleRegisterItems(String modID, RegistryEvent.Register<Item> event) {
//        IForgeRegistry<Item> itemReg = event.getRegistry();
//        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
//
//        for (Map.Entry<ResourceLocation, Block> entry : blockReg.getEntries()) {
//            if (entry.getKey().getNamespace().equals(modID) && entry.getValue() instanceof IBlockItemRegistrator) {
//                ((IBlockItemRegistrator) entry.getValue()).registerItems(itemReg);
//            }
//        }
//    }
//}
