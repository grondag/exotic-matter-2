package grondag.hard_science.machines.impl.building;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.PolyethyleneFuelCell;
import grondag.hard_science.machines.matbuffer.BufferManager;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;
import net.minecraft.entity.player.EntityPlayerMP;

public class BlockFabricatorMachine extends AbstractSimpleMachine
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    //FIXME: make configurable
//    private static final int WATTS_IDLE = 20;
//    private static final int WATTS_FABRICATION = 1200;
//    private static final int JOULES_PER_TICK_IDLE = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_IDLE));
//    private static final int JOULES_PER_TICK_FABRICATING = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_FABRICATION));
//    private static final long TICKS_PER_FULL_BLOCK = 40;


    // TODO: REMOVE
    // so TESR knows which buffer to render for each gauge
    public static final int BUFFER_INDEX_HDPE = 0;
    public static final int BUFFER_INDEX_FILLER = BUFFER_INDEX_HDPE + 1;
    public static final int BUFFER_INDEX_RESIN_A = BUFFER_INDEX_FILLER + 1;
    public static final int BUFFER_INDEX_RESIN_B = BUFFER_INDEX_RESIN_A + 1;
    public static final int BUFFER_INDEX_NANOLIGHT = BUFFER_INDEX_RESIN_B + 1;
    public static final int BUFFER_INDEX_CYAN = BUFFER_INDEX_NANOLIGHT + 1;
    public static final int BUFFER_INDEX_MAGENTA = BUFFER_INDEX_CYAN + 1;
    public static final int BUFFER_INDEX_YELLOW = BUFFER_INDEX_MAGENTA + 1;
    public static final int BUFFER_INDEX_TIO2 = BUFFER_INDEX_YELLOW + 1;
    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    // job search - not persisted
//    private Future<AbstractTask> taskSearch = null;
    
    // current job id - persisted
    int taskID = IIdentified.UNASSIGNED_ID;
    
    // current task - lazy lookup, don't use directly
    @Nullable
    BlockFabricationTask task = null;
    
    // Buffer setup - all persisted
//    private final BufferDelegate bufferFiller;
//    private final BufferDelegate bufferResinA;
//    private final BufferDelegate bufferResinB;
//    private final BufferDelegate bufferNanoLights;
//    private final BufferDelegate bufferCyan;
//    private final BufferDelegate bufferMagenta;
//    private final BufferDelegate bufferYellow;
//    private final BufferDelegate bufferTiO2;   
    
    public BlockFabricatorMachine()
    {
        super();
        // note that order has to match array declaration
//        BufferManager bufferInfo = this.getBufferManager();
//        this.bufferFiller = bufferInfo.getBuffer(BUFFER_INDEX_FILLER);
//        this.bufferResinA = bufferInfo.getBuffer(BUFFER_INDEX_RESIN_A);
//        this.bufferResinB = bufferInfo.getBuffer(BUFFER_INDEX_RESIN_B);
//        this.bufferNanoLights = bufferInfo.getBuffer(BUFFER_INDEX_NANOLIGHT);
//        this.bufferCyan = bufferInfo.getBuffer(BUFFER_INDEX_CYAN);
//        this.bufferMagenta = bufferInfo.getBuffer(BUFFER_INDEX_MAGENTA);
//        this.bufferYellow = bufferInfo.getBuffer(BUFFER_INDEX_YELLOW);
//        this.bufferTiO2 = bufferInfo.getBuffer(BUFFER_INDEX_TIO2);
        this.statusState.setHasBacklog(true);
    }
    
    @Override
    protected @Nullable BufferManager createBufferManager()
    {
        return new BufferManager(
                this, 
                64L, 
                StorageType.ITEM.MATCH_ANY, 
                64L, 
                VolumeUnits.KILOLITER.nL * 64L, 
                StorageType.FLUID.MATCH_ANY, 
                64L);
    }

    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        PowerContainer output = new PowerContainer(this, ContainerUsage.PUBLIC_BUFFER_OUT);
        output.configure(VolumeUnits.LITER.nL, BatteryChemistry.SILICON);
        
        PowerContainer input = new PowerContainer(this, ContainerUsage.PRIVATE_BUFFER_IN);
        input.configure(VolumeUnits.MILLILITER.nL * 10L, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                PolyethyleneFuelCell.basic_1kw(this), 
                input,
                output);
    }
    
    @Nullable
    public BlockFabricationTask task()
    {
        if(this.task == null && this.taskID != IIdentified.UNASSIGNED_ID)
        {
            this.task = (BlockFabricationTask) ITask.taskFromId(this.taskID);
        }
        return this.task;
    }
    
    @Override
    public boolean togglePower(EntityPlayerMP player)
    {
        boolean result = super.togglePower(player);
        if(result && !this.isOn())
        {
            this.abandonTaskInProgress();
        }
        return result;
    }
    
    private void abandonTaskInProgress()
    {
        BlockFabricationTask myTask = this.task();
        if(myTask != null)
        {
            myTask.abandon();
            this.task = null;
            this.taskID = IIdentified.UNASSIGNED_ID;
        }
        this.getControlState().clearJobTicks();
        this.getControlState().setModelState(null);
        this.getControlState().setTargetPos(null);
        this.getControlState().setMachineState(MachineState.THINKING);
        this.setDirty();
    }
    
//    /**
//     * Call to confirm still have an active task.
//     * Returns false if no task or task abandoned.
//     */
//    private boolean isTaskAbandoned()
//    {
//        if(this.task() == null || this.task().getStatus() != RequestStatus.ACTIVE)
//        {
//            this.abandonTaskInProgress();
//            return true;
//        }
//        return false;
//    }
    
//    private void progressFabrication()
//    {
//        if(this.isTaskAbandoned()) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_FABRICATING, false, false) != 0)
//        {
//            if(this.getControlState().progressJob((short) 1))
//            {
//                this.getControlState().clearJobTicks();
//                this.getControlState().setMachineState(MachineState.TRANSPORTING);
//            }
//            this.setDirty();
//        }
//        else
//        {
//            this.blamePowerSupply();
//        }
//    }
    
//    /**
//     * Unlike inputs, outputs need to go into domain-managed storage
//     * so that drones can locate them for pickup. If no domain storage
//     * available, then stall.
//     */
//    private void outputFabricatedBlock()
//    {
//        if(this.isTaskAbandoned()) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
//        {
//            this.blamePowerSupply();
//            return;
//        }
//        
//        // If we got to this point, we have a fabricated block.
//        // Put it in domain storage if there is room for it
//        // and tag the task with the location of the bulkResource so drone can find it
//        // otherwise stall
//        if(this.getDomain() == null) return;
//        
//        ItemStack stack = this.task().procurementTask().getStack().copy();
//        stack.setItemDamage(this.getControlState().getMeta());
//        PlacementItem.setStackLightValue(stack, this.getControlState().getLightValue());
//        PlacementItem.setStackSubstance(stack, this.getControlState().getSubstance());
//        PlacementItem.setStackModelState(stack, this.getControlState().getModelState());
//        
//        ItemResource res = ItemResource.fromStack(stack);
//        
//        //TODO: pass in procurement request
//        List<IResourceContainer<StorageTypeStack>> locations = this.getDomain().itemStorage.findSpaceFor(res, this);
//        
//        if(locations.isEmpty()) return;
//        
//       // this all needs to happen in any case
//          this.task.procurementTask().setStack(stack);
//          this.task.complete();
//          this.task = null;
//          this.taskID = IIdentified.UNASSIGNED_ID;
//          this.getControlState().setModelState(null);
//          this.getControlState().setTargetPos(null);
//          this.getControlState().setMachineState(MachineState.THINKING);
//          this.setDirty();
//          return;
//    }
   
//    /** returns substance that should be used to create the block in world if it can be successfully fabricated.  
//     * Must be called prior to calling fabricate */
//    private BlockSubstance prepareFabrication(ItemStack stack)
//    {
//        ModelState modelState = PlacementItem.getStackModelState(stack);
//        if(modelState == null) return null;
//        
//        BlockSubstance substance = PlacementItem.getStackSubstance(stack);
//        if(substance == null) return null;
//        
//        int lightValue = PlacementItem.getStackLightValue(stack);
//        
//        DemandManager2 demand = this.getBufferManager().demandManager();
//        
//        demand.clearAllDemand();
//        
//        SuperBlockMaterialCalculator needs = new SuperBlockMaterialCalculator(modelState, substance, lightValue);
//        
//        if(needs.filler_nL > 0) this.bufferFiller.addDemand(needs.filler_nL);
//        if(needs.resinA_nL > 0) this.bufferResinA.addDemand(needs.resinA_nL);
//        if(needs.resinB_nL > 0) this.bufferResinB.addDemand(needs.resinB_nL);
//        if(needs.nanoLights_nL > 0) this.bufferNanoLights.addDemand(needs.nanoLights_nL);
//        if(needs.cyan_nL > 0) this.bufferCyan.addDemand(needs.cyan_nL);
//        if(needs.magenta_nL > 0) this.bufferMagenta.addDemand(needs.magenta_nL);
//        if(needs.yellow_nL > 0) this.bufferYellow.addDemand(needs.yellow_nL);
//        if(needs.TiO2_nL > 0) this.bufferTiO2.addDemand(needs.TiO2_nL);
//    
//        
//        if(demand.canAllDemandsBeMetAndBlameIfNot())
//        {
//            // As soon as we are able to make any block, forget that we have material shortages.
//            // Maybe some other builder will handle.
//            this.getBufferManager().forgiveAll();
//            return needs.actualSubtance;
//        }
//        else
//        {
//            return null;
//        }
//    }

    @Override
    public void onDisconnect()
    {
        this.abandonTaskInProgress();
        super.onDisconnect();
    }

//    private void searchForWork()
//    {
//        if(this.getDomain() == null) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
//        {
//            this.blamePowerSupply();
//            return;
//        }
//        
//        // because we consumed power
//        this.setDirty();
//        this.markTEPlayerUpdateDirty(false);
//        
//        // find a job
//        if(this.taskSearch == null)
//        {
//            this.taskSearch = this.getDomain().jobManager.claimReadyWork(TaskType.BLOCK_FABRICATION, null);
//            return;
//        }
//        else if(this.taskSearch.isDone())
//        {
//            try
//            {
//                this.task = (BlockFabricationTask) this.taskSearch.get();
//                if(this.task != null)
//                {
//                    this.taskID = this.task.getId();
//                    this.setDirty();
//                }
//            }
//            catch(Exception e)
//            {
//                this.task = null;
//                this.taskID = IIdentified.UNASSIGNED_ID;
//            }
//            this.taskSearch = null;
//        }
//        
//        if(this.task == null) return;
//        
//        ItemStack stack = this.task.procurementTask().getStack();
//        
//        BlockSubstance substance = this.prepareFabrication(stack);
//        
//        if(substance == null)
//        {
//            this.task.abandon();
//        }
//        else
//        {
//            // setup job duration
//            this.getControlState().startJobTicks((short) (this.getBufferManager().demandManager().totalDemandNanoLiters() * TICKS_PER_FULL_BLOCK / VolumeUnits.KILOLITER.nL));
//
//            // consume resources
//            this.getBufferManager().demandManager().consumeAllDemandsAndClear();
//
//            // save placement info
//            this.getControlState().setModelState(PlacementItem.getStackModelState(stack));
//            this.getControlState().setTargetPos(task.procurementTask().pos());
//            this.getControlState().setLightValue(PlacementItem.getStackLightValue(stack));
//            this.getControlState().setSubstance(substance);
//            this.getControlState().setMeta(stack.getMetadata());
//            this.getControlState().setMachineState(MachineState.FABRICATING);
//            
//            // we want to send an immediate update when job starts
//            this.markTEPlayerUpdateDirty(true);
//        }
//    }

//    @Override
//    public void updateMachine(long tick)
//    {
//        super.updateMachine(tick);
//        
//        if(!this.isOn()) return;
//        
//        if(this.getDomain() == null) return;
//        
//        if((tick & 0x1F) == 0x1F)
//        {
//            this.setCurrentBacklog(this.getDomain().jobManager.getQueueDepth(TaskType.BLOCK_FABRICATION));
//        }
//        
//        switch(this.getControlState().getMachineState())
//        {
//        case FABRICATING:
//            this.progressFabrication();
//            break;
//            
//        case TRANSPORTING:
//            this.outputFabricatedBlock();
//            break;
//            
//        default:
//            this.searchForWork();
//            break;
//            
//        }
//    }
}
