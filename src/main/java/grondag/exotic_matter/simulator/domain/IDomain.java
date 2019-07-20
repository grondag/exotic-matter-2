package grondag.exotic_matter.simulator.domain;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.eventbus.EventBus;

import grondag.exotic_matter.simulator.persistence.IDirtListenerProvider;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.simulator.persistence.ISimulationNode;
import net.minecraft.entity.player.PlayerEntity;

public interface IDomain extends ISimulationNode, IDirtListenerProvider, IIdentified
{

    EventBus eventBus();
    
    List<DomainUser> getAllUsers();

    @Nullable DomainUser findPlayer(PlayerEntity player);

    @Nullable DomainUser findUser(String userName);

    boolean hasPrivilege(PlayerEntity player, Privilege privilege);

    /** 
     * Will return existing user if already exists.
     */
    DomainUser addPlayer(PlayerEntity player);

    String getName();

    void setName(String name);

    boolean isSecurityEnabled();

    void setSecurityEnabled(boolean isSecurityEnabled);

    <V extends IDomainCapability> V getCapability(Class<V> capability);


}