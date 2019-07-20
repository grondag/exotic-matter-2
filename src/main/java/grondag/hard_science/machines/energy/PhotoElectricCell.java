package grondag.hard_science.machines.energy;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.IWorldTask;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.WorldTaskManager;
import grondag.exotic_matter.world.Location;
import grondag.exotic_matter.world.WorldInfo;
import grondag.hard_science.simulator.device.IDevice;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

//FIXME: need to prevent double tick for solar cells when simulator running accelerated due to sleep or other world clock advancement
public class PhotoElectricCell extends AbstractGenerator
{
    
    /**
     * The number of joules we should generate per tick at full brightness
     * in our location. Factors in biome / dimension factors and base
     * efficiency of the cell.
     */
    private float joulesPerTickBrightness = 0;
    
    /**
     * Tick when we will request an update of joulesPerTickBrightness
     * to account for changes in the environment. (Placing or breaking
     * blocks above us, for example.)
     */
    private int nextRefreshTick = 0;
    
    public PhotoElectricCell(IDevice owner)
    {
        super(owner);
        this.setMaxOutputJoulesPerTick((long) (
                MachinePower.DAILY_INSOLATION_MAX_JOULES
                * MachinePower.PHOTO_ELECTRIC_EFFICIENCY
                / MachinePower.TOTAL_DAILY_BRIGHTNESS_FACTOR));
    }
    
    public PhotoElectricCell(IDevice owner, NBTTagCompound tag)
    {
        this(owner);
        this.deserializeNBT(tag);
    }

    /**
     * Schedules task for world thread to refresh location-dependent factor. 
     */
    private void requestRefreshFromWorld()
    {
        this.nextRefreshTick = Simulator.currentTick() + 40 + ThreadLocalRandom.current().nextInt(20);
        WorldTaskManager.enqueue( new IWorldTask()
        {
            boolean isDone = false;
            
            @Override
            public int runInServerTick(int maxOperations)
            {
                this.isDone = true;
                Location loc = device().getLocation();
                World world = loc.world();
                if(world.getHeight(loc.getX(), loc.getZ()) != loc.getY() + 1)
                {
                    joulesPerTickBrightness = 0;
                    return 1;
                }
                
                float factor = MachinePower.insolationFactor(device().getLocation())
                        * MachinePower.DAILY_INSOLATION_MAX_JOULES 
                        * MachinePower.PHOTO_ELECTRIC_EFFICIENCY
                        / MachinePower.TOTAL_DAILY_BRIGHTNESS_FACTOR;
                
                
                joulesPerTickBrightness = factor;
                return 1;
            }

            @Override
            public boolean isDone()
            {
                return isDone;
            }});
    }
    
    @Override
    protected long generateImplementation(long maxOutput, boolean allowPartial, boolean simulate)
    {
        if(!this.device().hasLocation()) return 0;
        
        if(Simulator.currentTick() >= this.nextRefreshTick) 
        {
            requestRefreshFromWorld();
        }
        
        if(this.joulesPerTickBrightness == 0) return 0;
        
        float brightness = WorldInfo.sunBrightnessFactor(this.device().getLocation().dimensionID());
        
        if(brightness == 0) return 0;
        
        return Math.min(maxOutput, (long) (brightness * this.joulesPerTickBrightness));
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        //NOOP
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        //NOOP
    }   
}