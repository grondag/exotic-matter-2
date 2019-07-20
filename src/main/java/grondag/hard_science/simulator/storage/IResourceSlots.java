package grondag.hard_science.simulator.storage;

import java.util.Iterator;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public interface IResourceSlots <V extends StorageType<V>> extends Iterable<AbstractResourceWithQuantity<V>>
{
    public boolean isEmpty();

    /** number of unique resources */
    public int size();
    
    public void clear();

    public void changeQuantity(IResource<V> resource, long delta);

    public long getQuantity(IResource<V> resource);

    @Override
    public Iterator<AbstractResourceWithQuantity<V>> iterator();
}
