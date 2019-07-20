package grondag.exotic_matter.simulator.persistence;

/** use this to avoid checking null on dirt listener */
public class NullDirtListener implements IDirtListener
{
    public static final NullDirtListener INSTANCE = new NullDirtListener();

    @Override
    public void setDirty() {}
    
}