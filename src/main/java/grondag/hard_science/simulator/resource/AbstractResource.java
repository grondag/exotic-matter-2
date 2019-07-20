package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

public abstract class AbstractResource<V extends StorageType<V>> implements IResource<V>
{
    protected AbstractResource() {};

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(@Nullable Object other)
    {
        boolean result = false;
        try
        {
            result = this.isResourceEqual((IResource<V>) other);
        } finally {}
       
        return result;
    }
}
