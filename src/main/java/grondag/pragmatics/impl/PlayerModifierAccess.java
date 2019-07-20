package grondag.pragmatics.impl;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerModifierAccess {
    int SHIFT = 1;
    int CONTROL = 2;
    int ALT = 4;
    int SUPER = 8;
    
    void prg_flags(byte flags);
    
    byte prg_flags();
    
    static boolean isShiftPressed(PlayerEntity player) {
        return player != null && (((PlayerModifierAccess)player).prg_flags() & SHIFT) == SHIFT;
    }
    
    static boolean isControlPressed(PlayerEntity player) {
        return player != null && (((PlayerModifierAccess)player).prg_flags() & CONTROL) == CONTROL;
    }
    
    static boolean isAltPressed(PlayerEntity player) {
        return player != null && (((PlayerModifierAccess)player).prg_flags() & ALT) == ALT;
    }
    
    static boolean isSuperPressed(PlayerEntity player) {
        return player != null && (((PlayerModifierAccess)player).prg_flags() & SUPER) == SUPER;
    }
}
