package grondag.hard_science.simulator.storage;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public class ResourceSlotsSingle<V extends StorageType<V>> 
    implements IResourceSlots<V>
{
    private AbstractResourceWithQuantity<V> slot = null;
    
    @Override
    public boolean isEmpty()
    {
        return slot == null;
    }

    @Override
    public int size()
    {
        return slot == null ? 0 : 1;
    }

    @Override
    public void clear()
    {
        slot = null;
    }

    @Override
    public void changeQuantity(IResource<V> resource, long delta)
    {
        if(this.slot == null)
        {
            assert delta > 0 : "Encountered negative resource level";
            this.slot = resource.withQuantity(delta);
        }
        else
        {
            assert(this.slot.resource().isResourceEqual(resource)) : "Resource mismatch for single resource container";
            this.slot.changeQuantity(delta);
            if(this.slot.isEmpty()) this.slot = null;
        }
    }

    @Override
    public long getQuantity(IResource<V> resource)
    {
        return (this.slot == null || !this.slot.resource().isResourceEqual(resource))
                ? 0 : this.slot.getQuantity();
    }

    @Override
    public Iterator<AbstractResourceWithQuantity<V>> iterator()
    {
        if(this.slot == null)
        {
            // Declaration is for casting an empty iterator.
            // Probably a cleaner way to do it but this works.
            ImmutableList<AbstractResourceWithQuantity<V>> empty = ImmutableList.of();
            return empty.iterator();
        }
        else
        {
            return Iterators.singletonIterator(this.slot);
        }
    }

}
