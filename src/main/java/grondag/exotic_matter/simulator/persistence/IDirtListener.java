package grondag.exotic_matter.simulator.persistence;

/**
 * Tracks if the instance needs to be persisted.  Used by persistence nodes and some sub nodes.
 */
public interface IDirtListener 
{
    public void setDirty();
}
