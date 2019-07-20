package grondag.hard_science.machines.impl.processing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.domain.ProcessManager;
import grondag.hard_science.simulator.domain.ProcessManager.ProcessInfo;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.fobs.SimpleProcurementTask;
import grondag.hard_science.simulator.fobs.TransientTaskContainer;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.storage.ItemStorageManager;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.MathHelper;

/**
 * Answers the question:  if I have machine that
 * can process two things, and I have both things on hand, which thing
 * should I process first?<p>
 * 
 * General rules
 * Process resources that satisfy higher-priority requests first
 * Process resources that will satisfy the largest number of requests.
 * Don't process a resource if an immediate output has reached max backlog or storage is full.
 *
 *
 */
public class MicronizerInputSelector implements IDomainMember
{
    private final ProcessManager owner;
    private int maxBacklog;
    private int backlogDepth;
    private int nextUpdateTick;
    
    private ImmutableList<IResource<StorageTypeStack>> bestInputs;
    
    public MicronizerInputSelector(ProcessManager owner)
    {
        this.owner = owner;
    }

    /**
     * Rules for input selection
     * 
     * Constraint: Always exclude inputs below defined reserve stocking levels.
     * Constraint: Always exclude inputs without a defined stocking level.
     * 
     * 1) Use inputs that produce the most in-demand output first
     * 
     * 2) If inputs produce same output or outputs with same demand level,
     * use inputs above target stock level first. Pick input with the highest
     * absolute overstock amount.
     * 
     * 3) If all inputs for an output are below target stocking levels,
     * pick the input most available relative to the target.
     *      
     */
    private void updateBacklogIfStale()
    {
        if(Simulator.currentTick() < nextUpdateTick) return;
        
        nextUpdateTick = Simulator.currentTick() + 20;
        
        long maxDemand = 0;
        long totalDemand = 0;
        
        Object2IntOpenHashMap<FluidResource> demands = new Object2IntOpenHashMap<>();
        
        for(BulkResource br : MicronizerRecipe.allOutputs())
        {
            ProcessInfo info = this.owner.getInfo(br.fluidResource());
            if(info == null) continue;
            maxDemand += info.targetStockLevel();
            long demand = info.demand();
            if(demand > 0)
            {
                totalDemand += demand;
                
                // Gives a 15-bit showing relative demand level.
                // Will be most significant bits of ranking value.
                int demandFactor = (int) (Short.MAX_VALUE * info.demandFactor());
                demands.put(br.fluidResource(), demandFactor);
            }
        }
        this.maxBacklog = MathHelper.ceil(VolumeUnits.nL2Liters(maxDemand));
        this.backlogDepth = MathHelper.ceil(VolumeUnits.nL2Liters(totalDemand));
        
        
        // find all inputs
        List<AbstractResourceWithQuantity<StorageTypeStack>> candidates 
            = this.owner.getDomain().getCapability(ItemStorageManager.class).findEstimatedAvailable(MicronizerRecipe.INPUT_RESOURCE_PREDICATE);
        
        ArrayList<BasicEntry<IResource<StorageTypeStack>>> allInputs  = new ArrayList<>();
        
        // for each input, see if outputs are needed and prioritize
        for(AbstractResourceWithQuantity<StorageTypeStack> c : candidates)
        {
            ItemResource inputRes = (ItemResource)c.resource();
            
            MicronizerRecipe recipe = MicronizerRecipe.getForInput(inputRes);
            if(recipe == null) continue;
            
            long demand = demands.getInt(recipe.outputResource().fluidResource());
            if(demand == 0) continue;
                    
            ProcessInfo inputInfo = this.owner.getInfo(inputRes);
            if(inputInfo == null) continue;
            
            double inputAvailability = inputInfo.availabilityFactor();
            if(inputAvailability == 0) continue;
            
            long ranking = (long)(inputAvailability * 0xFFFFFFFFFFFFL) | (demand << 48);
                    
            allInputs.add(new BasicEntry<>(inputRes, ranking));
        }
        
        this.bestInputs = allInputs.stream()
            .sorted(new Comparator<BasicEntry<IResource<StorageTypeStack>>>()
            {
                @Override
                public int compare(@Nullable BasicEntry<IResource<StorageTypeStack>> o1, @Nullable BasicEntry<IResource<StorageTypeStack>> o2)
                {
                    // note reverse order, higher ranking first
                    return Long.compare(o2.getLongValue(), o1.getLongValue());
                }
            })
            .map(p -> p.getKey())
            .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Finds best inputs for micronizing, and an upper bound
     * for how much of it should be processed.  Upper bound
     * because could exist multiple items that satisfy the need.
     * 
     * TODO: Use request priority as an input - right now only considers
     *  domain stocking levels and priorities
     */
    private List<IResource<StorageTypeStack>> findBestInputs()
    {
        this.updateBacklogIfStale();
        return this.bestInputs;
    }

    /**
     * 
     * Estimated number of crusher cycles that need to run to 
       reach target output levels.
     */
    public int estimatedBacklogDepth()
    {
        this.updateBacklogIfStale();
        return this.backlogDepth;
    }

    public int maxBacklogDepth()
    {
        this.updateBacklogIfStale();
        return this.maxBacklog;
    }
    
    public Future<NewProcurementTask<StorageTypeStack>> requestInput(MicronizerMachine machine)
    {
        return LogisticsService.ITEM_SERVICE.executor.submit(new Callable<NewProcurementTask<StorageTypeStack>>()
        {
            @Override
            public NewProcurementTask<StorageTypeStack> call() throws Exception
            {
                // abort if requesting machine isn't connected to anything
                if(!machine.itemTransport().hasAnyCircuit()) return null;

                // look for output resources that are in quantity
                // for those in quantity, take the highest priority
                List<IResource<StorageTypeStack>> candidates
                    = findBestInputs();

                NewProcurementTask<StorageTypeStack> result = null;

                if(!candidates.isEmpty())
                {
                    ItemStorageManager ism = machine.getDomain().getCapability(ItemStorageManager.class);
                    
                    for(IResource<StorageTypeStack> res : candidates)
                    {
                        List<IResourceContainer<StorageTypeStack>> sources = ism.findSourcesFor(res, machine);

                        if(sources.isEmpty()) continue;

                        // skip if machine can't accept this resource
                        if(machine.getBufferManager().itemInput().availableCapacityFor(res) == 0) continue;
                        
                        IResourceContainer<StorageTypeStack> store = sources.get(0);

                        result = new SimpleProcurementTask<>(
                                machine.getDomain().getCapability(TransientTaskContainer.class), res, 1);

                        if(ism.setAllocation(res, result, 1) == 1)
                        {
                            // already on service thread so send immediately
                            LogisticsService.ITEM_SERVICE.sendResourceNow(res, 1L, store.device(), machine, false, false, result);
                        }
                        
                        // release any outstanding allocation that wasn't transported
                        result.cancel();
                    }
                }
                return result;
            }
        }, false);

    }


    @Override
    public @Nullable IDomain getDomain()
    {
        return this.owner.getDomain();
    }
}
