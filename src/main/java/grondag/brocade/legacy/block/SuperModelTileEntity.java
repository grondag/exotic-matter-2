package grondag.brocade.legacy.block;

import grondag.brocade.block.BlockSubstance;
import net.minecraft.nbt.CompoundTag;

public class SuperModelTileEntity extends SuperTileEntity {

    ////////////////////////////////////////////////////////////////////////
    // INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////

    /** non-zero if block emits light */
    private byte lightValue = 0;

    private BlockSubstance substance = BlockSubstance.DEFAULT;

    @Override
    public void writeModNBT(CompoundTag compound) {
        super.writeModNBT(compound);
        if (this.substance != null)
            this.substance.serializeNBT(compound);
        compound.setByte(SuperBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE, (byte) this.lightValue);
    }

    @Override
    public void readModNBT(CompoundTag compound) {
        super.readModNBT(compound);
        this.substance = BlockSubstance.deserializeNBT(compound);
        this.lightValue = compound == null ? 0 : compound.getByte(SuperBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE);
    }

    public byte getLightValue() {
        return this.lightValue;
    }

    public void setLightValue(byte lightValue) {
        if (this.lightValue != lightValue) {
            this.lightValue = lightValue;
            if (this.world != null && this.world.isRemote)
                this.world.checkLight(this.pos);
            else
                this.markDirty();

        }
    }

    public BlockSubstance getSubstance() {
        return this.substance;
    }

    public void setSubstance(BlockSubstance substance) {
        if (this.substance != substance) {
            this.substance = substance;
            if (!(this.world == null || this.world.isRemote))
                this.markDirty();
        }
    }
}
