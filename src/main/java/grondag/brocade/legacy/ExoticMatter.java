//TODO: remove or redo

//package grondag.brocade.legacy;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import grondag.brocade.block.BlockSubstance;
//import grondag.brocade.block.SuperModelBlock;
//import grondag.brocade.model.state.ModelState;
//import grondag.exotic_matter.network.PacketHandler;
//import grondag.exotic_matter.network.PacketUpdateModifierKeys;
//import grondag.fermion.IGrondagMod;
//import net.minecraft.creativetab.CreativeTabs;
//import net.minecraft.item.ItemStack;
//
//
//public class ExoticMatter implements IGrondagMod {
//    public static final String MODID = "exotic_matter";
//    public static final String MODNAME = "Exotic Matter";
//    public static final String VERSION = "0.0.1";
//
//    public static ExoticMatter INSTANCE = new ExoticMatter();
//
//    
//    private static Logger log;
//
//    @Override
//    public Logger getLog() {
//        Logger result = log;
//        // allow access to log during unit testing or other debug scenarios
//        if (result == null) {
//            result = LogManager.getLogger();
//            log = result;
//        }
//        return result;
//    }
//
//    public static void setLog(Logger lOG) {
//        Brocade.log = lOG;
//    }
//
//    public static CreativeTabs tabMod = new CreativeTabs(MODID) {
//        @Override
//        
//        public ItemStack createIcon() {
//            return SuperModelBlock.findAppropriateSuperModelBlock(BlockSubstance.DEFAULT, new ModelState())
//                    .getSubItems().get(0);
//        }
//    };
//
//    @SuppressWarnings("null")
//    @SidedProxy(clientSide = "grondag.exotic_matter.ClientProxy", serverSide = "grondag.exotic_matter.ServerProxy")
//    public static CommonProxy proxy;
//
//    static {
//        // FluidRegistry.enableUniversalBucket();
//
//        // Packets handled on Server side, sent from Client
//        PacketHandler.registerMessage(PacketUpdateModifierKeys.class, PacketUpdateModifierKeys.class, Side.SERVER);
//
//        // Packets handled on Client side, sent from Server
//    }
//
//    @EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//        proxy.preInit(event);
//    }
//
//    @EventHandler
//    public void init(FMLInitializationEvent event) {
//        proxy.init(event);
//    }
//
//    @EventHandler
//    public void postInit(FMLPostInitializationEvent event) {
//        proxy.postInit(event);
//    }
//
//    @EventHandler
//    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
//        proxy.serverAboutToStart(event);
//    }
//
//    @EventHandler
//    public void serverStarting(FMLServerStartingEvent event) {
//        proxy.serverStarting(event);
//    }
//
//    @EventHandler
//    public void serverStopping(FMLServerStoppingEvent event) {
//        proxy.serverStopping(event);
//    }
//
//    @Override
//    public String modID() {
//        return MODID;
//    }
//}