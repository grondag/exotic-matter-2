package grondag.exotic_matter.simulator.persistence;

public interface IDirtNotifier extends IDirtListener
{
    public abstract void setDirtKeeper(IDirtKeeper listener);
}