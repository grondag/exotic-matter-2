package grondag.pragmatics.mixin;

import org.spongepowered.asm.mixin.Mixin;

import grondag.pragmatics.impl.PlayerModifierAccess;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements PlayerModifierAccess {
    private byte modifierFlags = 0;

    @Override
    public void prg_flags(byte flags) {
        modifierFlags = flags; 
    }

    @Override
    public byte prg_flags() {
        return modifierFlags;
    }
}
