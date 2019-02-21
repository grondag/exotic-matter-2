package grondag.brocade.legacy;

import javax.annotation.Nullable;

import grondag.exotic_matter.init.ModTileEntities;
import grondag.exotic_matter.player.ModifierKeys;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.statecache.IWorldStateCache;
import grondag.exotic_matter.varia.Base32Namer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@SuppressWarnings("deprecation")
public abstract class CommonProxy {

    /**
     * Will error if accessed on physical server. Should only be used on logical
     * client.
     */
    abstract public IWorldStateCache clientWorldStateCache();

    public void preInit(FMLPreInitializationEvent event) {
        ExoticMatter.setLog(event.getModLog());
        ConfigXM.recalcDerived();
        ModTileEntities.preInit(event);

        ForgeChunkManager.setForcedChunkLoadingCallback(ExoticMatter.INSTANCE, Simulator.RAW_INSTANCE_DO_NOT_USE);

        CapabilityManager.INSTANCE.register(ModifierKeys.class, new Capability.IStorage<ModifierKeys>() {

            @Override
            public @Nullable NBTBase writeNBT(@Nullable Capability<ModifierKeys> capability,
                    @Nullable ModifierKeys instance, @Nullable EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(@Nullable Capability<ModifierKeys> capability, @Nullable ModifierKeys instance,
                    @Nullable EnumFacing side, @Nullable NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });

    }

    public void init(FMLInitializationEvent event) {
        Base32Namer.loadBadNames(I18n.translateToLocal("misc.offensive"));
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void serverStarting(FMLServerStartingEvent event) {
        Simulator.loadSimulatorIfNotLoaded();
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        Simulator.instance().stop();
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    /**
     * Always false on server side.
     */
    public boolean isAcuityEnabled() {
        return false;
    }
}