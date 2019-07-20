package grondag.pragmatics.api;

import grondag.pragmatics.impl.PlayerModifierAccess;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerModifiers {
    static boolean isShiftPressed(PlayerEntity player) {
        return PlayerModifierAccess.isShiftPressed(player);
    }
    
    static boolean isControlPressed(PlayerEntity player) {
        return PlayerModifierAccess.isControlPressed(player);
    }
    
    static boolean isAltPressed(PlayerEntity player) {
        return PlayerModifierAccess.isAltPressed(player);
    }
    
    static boolean isSuperPressed(PlayerEntity player) {
        return PlayerModifierAccess.isSuperPressed(player);
    }
}
