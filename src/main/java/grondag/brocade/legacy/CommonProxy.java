//TODO: remove or redo

//package grondag.brocade.legacy;
//
//
//
//import grondag.brocade.BrocadeConfig;
//import grondag.brocade.init.ModTileEntities;
//import grondag.exotic_matter.player.ModifierKeys;
//import grondag.exotic_matter.simulator.Simulator;
//import grondag.exotic_matter.statecache.IWorldStateCache;
//import grondag.fermion.varia.Base32Namer;
//import net.minecraft.nbt.NBTBase;
//import net.minecraft.util.math.Direction;
//import net.minecraft.client.resource.language.I18n;
//import net.minecraftforge.common.ForgeChunkManager;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.CapabilityManager;
//import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
//import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
//import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
//
//@SuppressWarnings("deprecation")
//public abstract class CommonProxy {
//
//    /**
//     * Will error if accessed on physical server. Should only be used on logical
//     * client.
//     */
//    abstract public IWorldStateCache clientWorldStateCache();
//
//    public void preInit(FMLPreInitializationEvent event) {
//        Brocade.setLog(event.getModLog());
//        BrocadeConfig.recalcDerived();
//        ModTileEntities.preInit(event);
//
//        ForgeChunkManager.setForcedChunkLoadingCallback(Brocade.INSTANCE, Simulator.RAW_INSTANCE_DO_NOT_USE);
//
//        CapabilityManager.INSTANCE.register(ModifierKeys.class, new Capability.IStorage<ModifierKeys>() {
//
//            @Override
//            public NBTBase writeNBT(Capability<ModifierKeys> capability,
//                    ModifierKeys instance, Direction side) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public void readNBT(Capability<ModifierKeys> capability, ModifierKeys instance,
//                    Direction side, NBTBase nbt) {
//                throw new UnsupportedOperationException();
//            }
//
//        }, () -> {
//            throw new UnsupportedOperationException();
//        });
//
//    }
//
//    public void init(FMLInitializationEvent event) {
//        Base32Namer.loadBadNames(I18n.translate("misc.offensive"));
//    }
//
//    public void postInit(FMLPostInitializationEvent event) {
//    }
//
//    public void serverStarting(FMLServerStartingEvent event) {
//        Simulator.loadSimulatorIfNotLoaded();
//    }
//
//    public void serverStopping(FMLServerStoppingEvent event) {
//        Simulator.instance().stop();
//    }
//
//    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
//
//    }
//
//    /**
//     * Always false on server side.
//     */
//    public boolean isAcuityEnabled() {
//        return false;
//    }
//}