package grondag.exotic_matter.simulator.persistence;

public interface IDirtKeeper extends IDirtListener
{
    public abstract boolean isDirty();

    public abstract void setDirty(boolean isDirty);

    @Override
    public default void setDirty() { this.setDirty(true); }
}