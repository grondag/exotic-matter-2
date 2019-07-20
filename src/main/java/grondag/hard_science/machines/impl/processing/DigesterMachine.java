package grondag.hard_science.machines.impl.processing;

import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.machines.matbuffer.BufferManager;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.domain.ProcessManager;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.storage.FluidStorageManager;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import grondag.hard_science.simulator.storage.PowerContainer;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.item.ItemStack;

public class DigesterMachine extends AbstractSimpleMachine
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    private static final long BULK_BUFFER_SIZE = VolumeUnits.KILOLITER.nL;
    
    //FIXME: make configurable
    private static final int WATTS_IDLE = 20;
    private static final int WATTS_PROCESSING = 10000;
    private static final int JOULES_PER_TICK_IDLE = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_IDLE));
    private static final int JOULES_PER_TICK_PROCESSING = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_PROCESSING));

    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    private Future<NewProcurementTask<StorageTypeStack>> inputFuture;
    private Future<?> outputFuture;
    
    /** current output fluid based on last input */
    private FluidResource outputResource;
    
    /**
     * Reamining output to be... outputed
     */
    private long nanoLitersRemaining;
    
    /**
     * Nanoliters output per powered tick.
     * Softer material will output more per tick.
     */
    private long nanoLitersPerTick;
    
    
    public DigesterMachine()
    {
        super();
        this.statusState.setHasBacklog(true);
    }
    
    @Override
    protected @Nullable BufferManager createBufferManager()
    {
        BufferManager result = new BufferManager(
                this, 
                64L, 
                MicronizerRecipe.INPUT_RESOURCE_PREDICATE, 
                64L, 
                0, 
                StorageType.FLUID.MATCH_NONE, 
                BULK_BUFFER_SIZE);
        return result;
    }

    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        PowerContainer input = new PowerContainer(this, ContainerUsage.PRIVATE_BUFFER_IN);
        input.configure(VolumeUnits.LITER.nL * 10L, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                null, 
                input,
                null);
    }
    
    /**
     * Processes output and clears status if finishes.
     * Does not mark dirty unless job complete, because
     * {@link #doOffTick()} will have already done a normal mark dirty.
     */
    private void progressFabrication()
    {
        final FluidContainer fluidOut = this.getBufferManager().fluidOutput();
        // exit if not processing anything
        if(this.nanoLitersRemaining == 0) return;
        
        // determine max output based on available space in
        // output buffer
        long capacity = fluidOut.addLocally(this.outputResource, this.nanoLitersPerTick, true);
        
        // exit if output buffer is incompatible or full
        if(capacity == 0) return; 
        
        long nlOutput = Math.min(this.nanoLitersRemaining, this.nanoLitersPerTick);
        if(nlOutput > capacity) nlOutput = capacity;
        
        // always request full power - machine either processes at full power or doesn't
        if(this.energyManager().provideEnergy(JOULES_PER_TICK_PROCESSING, false, false) != JOULES_PER_TICK_PROCESSING)
        {
            // blame power supply if we can't provide full power
            this.blamePowerSupply();
            return;
        }
        
        nlOutput = fluidOut.addLocally(outputResource, nlOutput, false);
        this.nanoLitersRemaining -= nlOutput;
        this.getControlState().progressJob((short) 1);
        
        assert nlOutput >= 0 : "Encountered negative output remaining value";
        
        if(this.nanoLitersRemaining <= 0)
        {
            this.nanoLitersRemaining = 0;
            this.nanoLitersPerTick = 0;
            this.outputResource = null;
            this.getControlState().clearJobTicks();
            this.getControlState().setRecipe(null);
            this.getControlState().setMachineState(MachineState.IDLE);
            this.markTEPlayerUpdateDirty(true);
        }
    }
   

    @Override
    public void doOffTick()
    {
        super.doOffTick();
        
        //TODO: if the machine is off, export any input items
        
        if(!this.isOn()) return;
        
        if(this.getDomain() == null) return;
        
        if((Simulator.currentTick() & 0x1F) == 0x1F)
        {
            ProcessManager pm = this.getDomain().getCapability(ProcessManager.class);
            this.setCurrentBacklog(pm.micronizerInputSelector.estimatedBacklogDepth());
            this.statusState.setMaxBacklog(pm.micronizerInputSelector.maxBacklogDepth());
        }
        
        // if we don't have power to do basic control functions
        // then abort and blame power
        if(!this.provideAllPowerOrBlameSupply(JOULES_PER_TICK_IDLE)) return;
        
        // because we consumed power
        this.setDirty();
        this.markTEPlayerUpdateDirty(false);
        
        // if an inbound transport request is pending, check on it
        if(this.inputFuture != null && inputFuture.isDone())
        {
            // release future
            // we don't need to do anything with the task except
            // to let it go out of scope - its sole purpose was 
            // for inventory allocation.  InputSelector cancels ths
            // after requesting transport to free anything allocated
            // but not transported
            this.inputFuture = null;
        }
        
        // if there is something in item input and 
        // we aren't currently processing, load it
        // and initialize processing time
        this.loadInputIfPossible();
        
        // if we don't have anything in item input buffer
        // and no request is pending then
        // then look for something so that we don't idle
        if(this.inputFuture == null && this.getBufferManager().itemInput().isEmpty())
        {
            //TODO
            //this.inputFuture = this.getDomain().processManager.micronizerInputSelector.requestInput(this);
        }
        
        // do stuff if we can
        this.progressFabrication();
        
        // request that any output be put into primary storage if
        // an off-device storage location is available
        this.exportOutputIfPossible();
    }
    
    private void loadInputIfPossible()
    {
        final ItemContainer itemIn = this.getBufferManager().itemInput();
        
        /** exit if already processing something */
        if(this.outputResource != null) return;

        /** exit if nothing in input buffer */
        if(itemIn.isEmpty()) return;
        
        ItemStack stack = itemIn.extractItem(0, 1, false);
        if(stack == null || stack.isEmpty())
        {
            assert false : "Unable to retrive item input stack from input container";
            return;
        }
        
        MicronizerRecipe recipe = MicronizerRecipe.getForInput(stack);
        
        if(recipe == null)
        {
            assert false : "No recipe found for Micronizer input";
        
            ItemResource booger = ItemResource.fromStack(stack);
            
            // Should never get here, because input buffer should
            // refuse non inputs.  But if we do, try to clear
            // the input buffer by moving the offending resource
            // to the output buffer.
            long moved = this.getBufferManager().itemOutput().add(booger, 1, false);
            assert moved == 1
                    : "Items lost when trying to clear micronizer buffer.";
            return;
        }
        
        this.outputResource = recipe.outputResource().fluidResource();
        this.nanoLitersRemaining = recipe.outputForStack(stack);
        double joules = recipe.energyForStack(stack);
        double ticks = joules / JOULES_PER_TICK_PROCESSING;
        this.nanoLitersPerTick = (long) (this.nanoLitersRemaining / ticks); 
        
        this.getControlState().startJobTicks((short) Math.ceil(ticks));
        this.getControlState().setRecipe(recipe.displayForStack(stack));
        this.getControlState().setMachineState(MachineState.FABRICATING);
        
        // we want to send an immediate update when job starts
        this.markTEPlayerUpdateDirty(true);
    }
    
    private void exportOutputIfPossible()
    {
        // exit if last request isn't done yet
        if(this.outputFuture != null && !this.outputFuture.isDone()) return;
        
        final FluidContainer fluidOut = this.getBufferManager().fluidOutput();
        
        // exit if nothing to store
        if(fluidOut.isEmpty()) return;
                
        this.outputFuture = LogisticsService.FLUID_SERVICE.executor.submit(() -> 
        {
            // abort if machine isn't connected to anything
            if(!this.fluidTransport().hasAnyCircuit()) return null;

            FluidStorageManager fsm  = this.getDomain().getCapability(FluidStorageManager.class);
            
            for(AbstractResourceWithQuantity<StorageTypeFluid> rwq : fluidOut.findAll())
            {
                // find places to store output
                ImmutableList<IResourceContainer<StorageTypeFluid>> dumps 
                    = fsm.findSpaceFor(rwq.resource(), this);
                
                if(!dumps.isEmpty())
                {
                    long targetAmount = rwq.getQuantity();
                    for(IResourceContainer<StorageTypeFluid> store : dumps)
                    {
                        if(targetAmount <= 0) break;
                        targetAmount -= LogisticsService.FLUID_SERVICE.sendResourceNow(
                                rwq.resource(), 
                                targetAmount, 
                                this, 
                                store.device(), 
                                false, 
                                false, 
                                null);
                    }
                }
            }

            return null;
        }, false);
    }
}
