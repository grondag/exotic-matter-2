package grondag.exotic_matter.simulator.persistence;

public enum AssignedNumber
{
    DOMAIN,
    DEVICE,
    JOB,
    TASK, 
    BUILD;
    
    public final String tagName;
    
    private AssignedNumber()
    {
        this.tagName = "hsanum" + this.ordinal();
    }
    
}
