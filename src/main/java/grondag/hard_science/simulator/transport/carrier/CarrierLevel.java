package grondag.hard_science.simulator.transport.carrier;

import javax.annotation.Nullable;

public enum CarrierLevel
{
    /**
     * Sub-sonic bus, moderate volume/power.
     */
    BOTTOM,
    
    /**
     * Sub-sonic, high capacity, multi-path bus.
     */
    MIDDLE,
    
    /**
     * Supersonic/superconducting switches and interconnects.
     */
    TOP;
    
    public boolean isBottom()
    {
        return this == BOTTOM;
    }
    
    public boolean isTop()
    {
        return this == TOP;
    }
    
    @Nullable 
    public CarrierLevel above()
    {
        switch(this)
        {
        case BOTTOM:
            return MIDDLE;
            
        case MIDDLE:
            return TOP;
            
        case TOP:
        default:
            return null;
        }
    }
    
    @Nullable 
    public CarrierLevel below()
    {
        switch(this)
        {
        case TOP:
            return MIDDLE;
            
        case MIDDLE:
            return BOTTOM;
            
        case BOTTOM:
        default:
            return null;
        }
    }
}
