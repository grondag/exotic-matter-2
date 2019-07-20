package grondag.exotic_matter.simulator.domain;

import grondag.exotic_matter.simulator.persistence.ISimulationNode;
import grondag.fermion.serialization.IReadWriteNBT;

public interface IDomainCapability extends IDomainMember, ISimulationNode, IReadWriteNBT
{
    public String tagName();
    
    /**
     * Called by domain after construction
     */
    public void setDomain(IDomain domain);
    
}