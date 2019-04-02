package grondag.brocade.legacy.block;




import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.ModelState;
import grondag.fermion.serialization.NBTDictionary;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;



public class SuperTileEntity extends TileEntity {
    ////////////////////////////////////////////////////////////////////////
    // STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////

    /**
     * Anything stored in this tag will not be sent to clients.
     */
    public static final String NBT_SERVER_SIDE_TAG = NBTDictionary.claim("serverOnly");

    /** Returns server-side tag if one is present, creating it if not. */
    public static CompoundTag getServerTag(CompoundTag fromTag) {
        NBTBase result = fromTag.getTag(NBT_SERVER_SIDE_TAG);
        if (result == null || result.getId() != 10) {
            result = new CompoundTag();
            fromTag.setTag(NBT_SERVER_SIDE_TAG, result);
        }
        return (CompoundTag) result;
    }

    /**
     * Returns tag stripped of server-side tag if it is present. If the tag must be
     * stripped, returns a modified copy. Otherwise returns input tag. Will return
     * null if a null tag is passed in.
     */
    public static CompoundTag withoutServerTag(CompoundTag inputTag) {
        if (inputTag != null && inputTag.hasKey(NBT_SERVER_SIDE_TAG)) {
            inputTag = inputTag.copy();
            inputTag.removeTag(NBT_SERVER_SIDE_TAG);
        }
        return inputTag;
    }

    /**
     * Will be updated to actual game render distance on client side.
     */
    private static int maxSuperBlockRenderDistanceSq = 4096;

    ////////////////////////////////////////////////////////////////////////
    // INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////

    protected ISuperModelState modelState = null;

    // public IExtendedBlockState exBlockState;
    private boolean isModelStateCacheDirty = true;

    /**
     * Called client side at start up and when setting is changed.
     */
    public static void updateRenderDistance() {
        int configuredDist = MinecraftClient.getMinecraft().gameSettings.renderDistanceChunks * 16;
        maxSuperBlockRenderDistanceSq = configuredDist * configuredDist;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return maxSuperBlockRenderDistanceSq;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState,
            BlockState newSate) {
        if (oldState.getBlock() == newSate.getBlock()) {
            return false;
        } else {
            if (world.isRemote)
                updateClientRenderState();
            return true;
        }
    }

    /**
     * Want to avoid the synchronization penalty of pooled block pos.
     */
    private static ThreadLocal<MutableBlockPos> updateClientPos = new ThreadLocal<MutableBlockPos>() {
        @Override
        protected MutableBlockPos initialValue() {
            return new MutableBlockPos();
        }
    };

    public void updateClientRenderState() {
        this.isModelStateCacheDirty = true;

        MutableBlockPos mPos = updateClientPos.get();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        invalidateClientCache(mPos.setPos(x - 1, y - 1, z - 1));
        invalidateClientCache(mPos.setPos(x - 1, y - 1, z));
        invalidateClientCache(mPos.setPos(x - 1, y - 1, z + 1));

        invalidateClientCache(mPos.setPos(x - 1, y, z - 1));
        invalidateClientCache(mPos.setPos(x - 1, y, z));
        invalidateClientCache(mPos.setPos(x - 1, y, z + 1));

        invalidateClientCache(mPos.setPos(x - 1, y + 1, z - 1));
        invalidateClientCache(mPos.setPos(x - 1, y + 1, z));
        invalidateClientCache(mPos.setPos(x - 1, y + 1, z + 1));

        invalidateClientCache(mPos.setPos(x, y - 1, z - 1));
        invalidateClientCache(mPos.setPos(x, y - 1, z));
        invalidateClientCache(mPos.setPos(x, y - 1, z + 1));

        invalidateClientCache(mPos.setPos(x, y, z - 1));
        invalidateClientCache(mPos.setPos(x, y, z + 1));

        invalidateClientCache(mPos.setPos(x, y + 1, z - 1));
        invalidateClientCache(mPos.setPos(x, y + 1, z));
        invalidateClientCache(mPos.setPos(x, y + 1, z + 1));

        invalidateClientCache(mPos.setPos(x + 1, y - 1, z - 1));
        invalidateClientCache(mPos.setPos(x + 1, y - 1, z));
        invalidateClientCache(mPos.setPos(x + 1, y - 1, z + 1));

        invalidateClientCache(mPos.setPos(x + 1, y, z - 1));
        invalidateClientCache(mPos.setPos(x + 1, y, z));
        invalidateClientCache(mPos.setPos(x + 1, y, z + 1));

        invalidateClientCache(mPos.setPos(x + 1, y + 1, z - 1));
        invalidateClientCache(mPos.setPos(x + 1, y + 1, z));
        invalidateClientCache(mPos.setPos(x + 1, y + 1, z + 1));

        this.world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);

    }

    private void invalidateClientCache(BlockPos updatePos) {
        TileEntity target = this.world.getTileEntity(updatePos);
        if (target != null && target instanceof SuperTileEntity) {
            ((SuperTileEntity) target).isModelStateCacheDirty = true;
        }
    }

    /**
     * Generate tag sent to client when block/chunk start loads. MUST include x, y,
     * z tags so client knows which TE belong to. Super calls writeInternal()
     * instead of {@link #writeToNBT(CompoundTag)} We call writeToNBT so that we
     * include all info, but filter out server-side only tag to prevent wastefully
     * large packets.
     */
    @Override
    public CompoundTag getUpdateTag() {
        return withoutServerTag(writeToNBT(super.getUpdateTag()));
    }

    /**
     * Generate packet sent to client for TE synch after block/chunk is loaded. Is
     * inefficient that the information is serialized twice: start to NBT then to
     * ByteBuffer but that is how MC does it and the packet only accepts NBT.
     */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), this.getUpdateTag());
    }

    /**
     * Process packet sent to client for TE synch after block/chunk is loaded.
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public final void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        this.readModNBT(compound);
    }

    /**
     * Restores all state previously serialized by
     * {@link #writeModNBT(CompoundTag)}
     * <p>
     * 
     * Note: important that tags used here match those used PlacementItem helper
     * methods.
     */
    public void readModNBT(CompoundTag compound) {
        this.modelState = ModelState.deserializeFromNBTIfPresent(compound);
        this.onModelStateChange(true);
    }

    /**
     * Stores all state for this mod to the given tag. Used internally for
     * serialization but can also be used to restore state from ItemStack
     * <p>
     * 
     * Note: important that tags used here match those used PlacementItem helper
     * methods
     */
    public void writeModNBT(CompoundTag compound) {
        this.modelState.serializeNBT(compound);
    }

    @Override
    public final CompoundTag writeToNBT(CompoundTag compound) {
        this.writeModNBT(compound);
        return super.writeToNBT(compound);
    }

    public ISuperModelState getModelState(BlockState state, ExtendedBlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        ISuperModelState result = this.modelState;

        if (result == null) {
            result = ((ISuperBlock) state.getBlock()).getDefaultModelState();
            this.modelState = result;
            this.isModelStateCacheDirty = true;

            // necessary for species
            refreshFromWorldIfNeeded = true;
        }

        if (this.isModelStateCacheDirty && refreshFromWorldIfNeeded) {
            result.refreshFromWorld(state, SuperBlockWorldAccess.access(world), pos);
            this.isModelStateCacheDirty = false;
        }

        return result;
    }

    /**
     * Use this version when you don't have world state handy
     */
    public ISuperModelState getModelState() {
        if (!(this.modelState == null || this.isModelStateCacheDirty)) {
            return this.modelState;
        } else {
            return SuperBlockWorldAccess.access(this.world).getModelState((ISuperBlock) this.blockType, pos, true);
        }
    }

    /**
     * intended for use in TESR - don't refresh unless missing because should be up
     * to date from getExtendedState called before this
     */
    @SuppressWarnings("null")
    public ISuperModelState getCachedModelState() {
        return this.modelState == null
                ? SuperBlockWorldAccess.access(this.world).getModelState((ISuperBlock) this.blockType, pos, true)
                : this.modelState;
    }

    public void setModelState(ISuperModelState modelState) {
        // if making existing appearance static, don't need to refresh on client side
        boolean needsClientRefresh = this.world != null && this.world.isRemote
                && !(this.modelState != null && this.modelState.equals(modelState) && modelState.isStatic()
                        && this.modelState.isStatic() != modelState.isStatic());
        {
            this.modelState = modelState;
            this.onModelStateChange(!modelState.isStatic());
        }

        this.modelState = modelState;
        this.onModelStateChange(needsClientRefresh);
    }

    /**
     * call whenever modelState changes (or at least probably did). Parameter should
     * always be true except in case of changing dynamic blocks to static without
     * altering appearance.
     */
    protected void onModelStateChange(boolean refreshClientRenderState) {
        /**
         * This can be called by onBlockPlaced after we've already been established. If
         * that happens, need to treat it like an update, markDirty(), refresh client
         * state, etc.
         */
        this.isModelStateCacheDirty = true;
        if (this.world != null) {
            if (this.world.isRemote) {
                if (refreshClientRenderState)
                    this.updateClientRenderState();
            } else {
                this.markDirty();
            }
        }
    }

    /**
     * Only true for virtual blocks. Prevents "instanceof" checking.
     */
    public boolean isVirtual() {
        return false;
    }

    
    private net.minecraft.util.math.BoundingBox renderBB;

    /**
     * Cache result. Gets called alot for TESR. <br>
     * <br>
     * 
     * {@inheritDoc}
     */
    
    @Override
    public net.minecraft.util.math.BoundingBox getRenderBoundingBox() {
        net.minecraft.util.math.BoundingBox result = this.renderBB;
        if (result == null) {
            result = new net.minecraft.util.math.BoundingBox(pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            this.renderBB = result;
        }
        return result;
    }

}
