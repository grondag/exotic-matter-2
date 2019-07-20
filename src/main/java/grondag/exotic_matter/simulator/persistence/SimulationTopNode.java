package grondag.exotic_matter.simulator.persistence;

import net.minecraft.world.PersistentState;

public abstract class SimulationTopNode extends PersistentState implements ISimulationNode {

    public SimulationTopNode(String string_1) {
        super(string_1);
    }

}
