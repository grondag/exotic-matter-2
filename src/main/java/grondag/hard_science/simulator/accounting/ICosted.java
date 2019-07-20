package grondag.hard_science.simulator.accounting;

import java.util.List;

//WIP
public interface ICosted
{
    public long getEnergyCost();
    
    public List<?> getMaterialCost();
    
    public int getComputeCost();
}
