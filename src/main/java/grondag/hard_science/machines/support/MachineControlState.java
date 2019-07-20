package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelState;
import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.BitPacker64;
import grondag.exotic_matter.world.PackedBlockPos;
import grondag.hard_science.crafting.base.GenericRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;



public class MachineControlState implements IReadWriteNBT, IMessagePlus
{
    public static enum ControlMode
    {
        ON(false),
        OFF(false),
        ON_WITH_REDSTONE(true),
        OFF_WITH_REDSTONE(true);
        
        public final boolean isRedstoneControlEnabled;
        
        private ControlMode(boolean isRedstoneControlEnabled)
        {
            this.isRedstoneControlEnabled = isRedstoneControlEnabled;
        }
    }
    
    public static enum RenderLevel
    {
        NONE,
        MINIMAL,
        EXTENDED_WHEN_LOOKING,
        EXTENDED_WHEN_VISIBLE;
    }
    
    /**
     * Not all machines use all states,
     * nor are all transitions the same, but 
     * wanted a shared vocabulary.
     */
    public static enum MachineState
    {
        /**
         * Machine is not doing anything, because it is off, not powered, has no work, or is just designed to sit there.
         * This is the default state.
         */
        IDLE,
        
        /**
         * The machine is thinking about fixing to get ready to get started on doing something... maybe.
         * Might also be searching for work.
         */
        THINKING,
        
        /**
         * The machine is pulling resources into internal buffers.
         * This state may not be reported much, because usually happens simultaneously with something more interesting.
         */
        SUPPLYING,
        
        /**
         * The machine is making something.
         */
        FABRICATING,
        
        /**
         * Machine is moving or delivering something.
         */
        TRANSPORTING,
        
        // Reserved to pad enum serializer so don't break world saves if later add more substances.
        RESERVED01,
        RESERVED02,
        RESERVED03,
        RESERVED04,
        RESERVED05,
        RESERVED06,
        RESERVED07,
        RESERVED08,
        RESERVED09,
        RESERVED10,
        RESERVED11,
        RESERVED12,
        RESERVED13,
        RESERVED14,
        RESERVED15,
        RESERVED16,
        RESERVED17,
        RESERVED18,
        RESERVED19,
        RESERVED20,
        RESERVED21,
        RESERVED22,
        RESERVED23,
        RESERVED24,
        RESERVED25,
        RESERVED26,
        RESERVED27;
    }
    
    private static BitPacker64<MachineControlState> PACKER = new BitPacker64<MachineControlState>(s -> s.bits, (s,b) -> s.bits = b);
    
    private static BitPacker64<MachineControlState>.EnumElement<ControlMode> PACKED_CONTROL_MODE = PACKER.createEnumElement(ControlMode.class);
    private static BitPacker64<MachineControlState>.EnumElement<RenderLevel> PACKED_RENDER_LEVEL = PACKER.createEnumElement(RenderLevel.class);
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_MODELSTATE = PACKER.createBooleanElement();
    private static BitPacker64<MachineControlState>.IntElement PACKED_META = PACKER.createIntElement(16);
    private static BitPacker64<MachineControlState>.IntElement PACKED_LIGHT_VALUE = PACKER.createIntElement(16);
    private static BitPacker64<MachineControlState>.IntElement PACKED_SUBSTANCE = PACKER.createIntElement(BlockSubstance.MAX_SUBSTANCES);
    private static BitPacker64<MachineControlState>.EnumElement<MachineState> PACKED_MACHINE_STATAE = PACKER.createEnumElement(MachineState.class);
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_JOB_TICKS = PACKER.createBooleanElement();
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_TARGET_POS = PACKER.createBooleanElement();
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_MATERIAL_BUFFER = PACKER.createBooleanElement();
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_POWER_SUPPLY= PACKER.createBooleanElement();
    private static BitPacker64<MachineControlState>.BooleanElement PACKED_HAS_RECIPE= PACKER.createBooleanElement();

    private static final long DEFAULT_BITS;
    
    static
    {
        long bits = 0;
        bits |= PACKED_CONTROL_MODE.getBits(ControlMode.ON);
        bits |= PACKED_RENDER_LEVEL.getBits(RenderLevel.EXTENDED_WHEN_VISIBLE);
        DEFAULT_BITS = bits;
    }
    
    private long bits = DEFAULT_BITS;
    @Nullable private ISuperModelState modelState;
    private short jobDurationTicks = 0;
    private short jobRemainingTicks = 0;
    
    @Nullable private BlockPos targetPos = null;
    @Nullable private GenericRecipe currentRecipe = null;
    
    //////////////////////////////////////////////////////////////////////
    // ACCESS METHODS
    //////////////////////////////////////////////////////////////////////
    
    public ControlMode getControlMode() { return PACKED_CONTROL_MODE.getValue(this); }
    public void setControlMode(ControlMode value) { PACKED_CONTROL_MODE.setValue(value, this); }
    
    public RenderLevel getRenderLevel() { return PACKED_RENDER_LEVEL.getValue(this); }
    public void setRenderLevel(RenderLevel value) { PACKED_RENDER_LEVEL.setValue(value, this); }
    
    /**
     * If true, then modelState should be populated.
     * Used by block fabricators
     */
    public boolean hasModelState() { return PACKED_HAS_MODELSTATE.getValue(this); }
    private void updateModelStateStatus() { PACKED_HAS_MODELSTATE.setValue(this.modelState != null, this); }
    
    public ISuperModelState getModelState() { return this.modelState; }
    public void setModelState( @Nullable ISuperModelState value)
    {
        this.modelState = value; 
        this.updateModelStateStatus();
    }
    
    /**
     * If true, then recipe should be populated.
     */
    public boolean hasRecipe() { return PACKED_HAS_RECIPE.getValue(this); }
    private void updateRecipeStatus() { PACKED_HAS_RECIPE.setValue(this.currentRecipe != null, this); }
    
    public GenericRecipe getRecipe() { return this.currentRecipe; }
    public void setRecipe( GenericRecipe value)
    {
        this.currentRecipe = value; 
        this.updateRecipeStatus();
    }
    
    public boolean hasTargetPos() { return PACKED_HAS_TARGET_POS.getValue(this); }
    private void updateTargetPosStatus() { PACKED_HAS_TARGET_POS.setValue(this.targetPos != null, this); }
    
    public BlockPos getTargetPos() { return this.targetPos; }
    public void setTargetPos( BlockPos value)
    {
        this.targetPos = value; 
        this.updateTargetPosStatus();
    }
    
    /** 
     * Intended for block fabricators, but usage determined by machine. 
     * While values are always non-null, they are not always valid.  
     * Check that a modelState or other related attribute also exists
     */
    public BlockSubstance getSubstance() { return BlockSubstance.get(PACKED_SUBSTANCE.getValue(this)); }
    public void setSubstance(BlockSubstance value) { PACKED_SUBSTANCE.setValue(value.ordinal, this); }
    
    /** intended for block fabricators, but usage determined by machine. */
    public int getLightValue() { return PACKED_LIGHT_VALUE.getValue(this); }
    public void setLightValue(int value) { PACKED_LIGHT_VALUE.setValue(value, this); }
    
    /** intended for block fabricators, but usage determined by machine. */
    public int getMeta() { return PACKED_META.getValue(this); }
    public void setMeta(int value) { PACKED_META.setValue(value, this); }
    
    public MachineState getMachineState() { return PACKED_MACHINE_STATAE.getValue(this); }
    public void setMachineState(MachineState value) { PACKED_MACHINE_STATAE.setValue(value, this); }
    
    public boolean hasJobTicks() { return PACKED_HAS_JOB_TICKS.getValue(this); }
    
    private void updateJobTicksStatus()
    {
        PACKED_HAS_JOB_TICKS.setValue(this.jobDurationTicks > 0 || this.jobRemainingTicks > 0, this);
    }
    
    public short getJobDurationTicks() { return this.jobDurationTicks; }
    
    /** This will NOT set JobRemainingTicks to the same value. Use {@link #startJobTicks(short)} for that. */
    public void setJobDurationTicks(short ticks)
    {
        this.jobDurationTicks = ticks;
        this.updateJobTicksStatus();
    }
    
    /** reduces remaining job ticks by given amount and returns true if job is complete */
    public boolean progressJob(short howManyTicks)
    {
        int current = this.getJobRemainingTicks() - howManyTicks;
        if(current < 0) current = 0;
        this.setJobRemainingTicks((short) current);
        return current == 0;
    }
    
    public short getJobRemainingTicks() { return this.jobRemainingTicks; }
    public void setJobRemainingTicks(short ticks)
    {
        this.jobRemainingTicks = ticks;
        this.updateJobTicksStatus();
    }
    
    public void setJobTicks(short jobDurationTicks, short jobRemainingTicks)
    {
        this.jobDurationTicks = jobDurationTicks;
        this.jobRemainingTicks = jobRemainingTicks;
        this.updateJobTicksStatus();
    }
    
    /** sets both duration and remaining ticks to given value. */
    public void startJobTicks(short jobDurationTicks)
    {
        this.setJobTicks(jobDurationTicks, jobDurationTicks);
    }
    
    /**
     * Sets both job tick values to zero - saves space in packets if no job active.
     */
    public void clearJobTicks()
    {
        this.jobDurationTicks = 0;
        this.jobRemainingTicks = 0;
        this.updateJobTicksStatus();
    }
    
    public boolean hasMaterialBuffer() { return PACKED_HAS_MATERIAL_BUFFER.getValue(this); }
    public void hasMaterialBuffer(boolean hasBuffer) { PACKED_HAS_MATERIAL_BUFFER.setValue(hasBuffer, this); }
    
    public boolean hasPowerSupply() { return PACKED_HAS_POWER_SUPPLY.getValue(this); }
    public void hasPowerSupply(boolean hasProvider) { PACKED_HAS_POWER_SUPPLY.setValue(hasProvider, this); }
    
    //////////////////////////////////////////////////////////////////////
    // Serialization Stuff                                              //
    //////////////////////////////////////////////////////////////////////

    private final static String NBT_MACHINE_CONTROL_STATE = NBTDictionary.claim("devCtrlState");
    private final static String NBT_MACHINE_MODEL_STATE = NBTDictionary.claim("devMdlState");
    private final static String NBT_MACHINE_TARGET_BLOCKPOS = NBTDictionary.claim("devTargetPos");
    private final static String NBT_MACHINE_JOB_DURATION_TICKS = NBTDictionary.claim("devDurTicks");
    private final static String NBT_MACHINE_JOB_REMAINING_TICKS = NBTDictionary.claim("devRemTicks");
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        if(tag.hasKey(NBT_MACHINE_CONTROL_STATE))
        {
            this.bits = tag.getLong(NBT_MACHINE_CONTROL_STATE);
            
            if(this.hasModelState())
            {
                if(this.modelState == null) this.modelState = new ModelState();
                this.modelState.deserializeNBT(tag.getCompoundTag(NBT_MACHINE_MODEL_STATE));
            }
            if(this.hasTargetPos())
            {
                this.targetPos = PackedBlockPos.unpack(tag.getLong(NBT_MACHINE_TARGET_BLOCKPOS));
            }
            if(this.hasJobTicks())
            {
                this.jobDurationTicks = tag.getShort(NBT_MACHINE_JOB_DURATION_TICKS);
                this.jobRemainingTicks = tag.getShort(NBT_MACHINE_JOB_REMAINING_TICKS);
            }
            if(this.hasRecipe())
            {
                this.currentRecipe = new GenericRecipe(tag);
            }
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong(NBT_MACHINE_CONTROL_STATE, this.bits);
        if(this.hasModelState())
        {
            tag.setTag(NBT_MACHINE_MODEL_STATE, this.modelState.serializeNBT());
        }
        if(this.hasTargetPos())
        {
            tag.setLong(NBT_MACHINE_TARGET_BLOCKPOS, PackedBlockPos.pack(this.targetPos));
        }
        if(this.hasJobTicks())
        {
            tag.setShort(NBT_MACHINE_JOB_DURATION_TICKS, this.jobDurationTicks);
            tag.setShort(NBT_MACHINE_JOB_REMAINING_TICKS, this.jobRemainingTicks);
        }
        if(this.hasRecipe())
        {
            this.currentRecipe.serializeNBT(tag);
        }
    }   
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.bits = pBuff.readLong();
        if(this.hasModelState())
        {
            if(this.modelState == null) this.modelState = new ModelState();
            this.modelState.fromBytes(pBuff);
        }
        if(this.hasTargetPos())
        {
            this.targetPos = PackedBlockPos.unpack(pBuff.readLong());
        }
        if(this.hasJobTicks())
        {
            this.jobDurationTicks = pBuff.readShort();
            this.jobRemainingTicks = pBuff.readShort();
        }
        
        if(this.hasRecipe())
        {
            this.currentRecipe = new GenericRecipe(pBuff);
        }
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.bits);
        if(this.hasModelState())
        {
            this.modelState.toBytes(pBuff);
        }
        if(this.hasTargetPos())
        {
            pBuff.writeLong(PackedBlockPos.pack(this.targetPos));
        }
        if(this.hasJobTicks())
        {
            pBuff.writeShort(this.jobDurationTicks);
            pBuff.writeShort(this.jobRemainingTicks);
        }
        if(this.hasRecipe())
        {
            this.currentRecipe.toBytes(pBuff);
        }
    }
}
