package grondag.exotic_matter.simulator.domain;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Posted when player active domain changes.
 */
public interface PlayerDomainChangeCallback {
    public static final Event<PlayerDomainChangeCallback> EVENT = EventFactory.createArrayBacked(PlayerDomainChangeCallback.class,
            (listeners) -> (p, o, n) -> {
                for (PlayerDomainChangeCallback event : listeners) {
                    event.onDomainChange(p, o, n);
                }
            }
        );
        
        void onDomainChange(PlayerEntity player, IDomain oldDomain, IDomain newDomain);
}
