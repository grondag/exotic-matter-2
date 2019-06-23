package grondag.brocade.block;

import grondag.brocade.state.MeshState;
import grondag.brocade.state.MeshStateImpl;
import grondag.fermion.serialization.NBTDictionary;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;


public class BrocadeTileEntity extends BlockEntity implements BlockEntityClientSerializable {
    ////////////////////////////////////////////////////////////////////////
    // STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////

    public BrocadeTileEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    /**
     * Anything stored in this tag will not be sent to clients.
     */
    public static final String NBT_SERVER_SIDE_TAG = NBTDictionary.claim("serverOnly");

    /** Returns server-side tag if one is present, creating it if not. */
    public static CompoundTag getServerTag(CompoundTag fromTag) {
        Tag result = fromTag.getTag(NBT_SERVER_SIDE_TAG);
        if (result == null || result.getType() != 10) {
            result = new CompoundTag();
            fromTag.put(NBT_SERVER_SIDE_TAG, result);
        }
        return (CompoundTag) result;
    }

    /**
     * Returns tag stripped of server-side tag if it is present. If the tag must be
     * stripped, returns a modified copy. Otherwise returns input tag. Will return
     * null if a null tag is passed in.
     */
    public static CompoundTag withoutServerTag(CompoundTag inputTag) {
        if (inputTag != null && inputTag.containsKey(NBT_SERVER_SIDE_TAG)) {
            inputTag = (CompoundTag) inputTag.copy();
            inputTag.remove(NBT_SERVER_SIDE_TAG);
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

    protected MeshState modelState = null;

    // public IExtendedBlockState exBlockState;
    private boolean isModelStateCacheDirty = true;

    /**
     * Called client side at start up and when setting is changed.
     */
    public static void updateRenderDistance() {
        int configuredDist = MinecraftClient.getInstance().options.viewDistance * 16;
        maxSuperBlockRenderDistanceSq = configuredDist * configuredDist;
    }

    @Override
    public double getSquaredRenderDistance() {
        return maxSuperBlockRenderDistanceSq;
    }

    /**
     * Want to avoid the synchronization penalty of pooled block pos.
     */
    private static ThreadLocal<BlockPos.Mutable> updateClientPos = ThreadLocal.withInitial(BlockPos.Mutable::new);

    public void updateClientRenderState() {
        this.isModelStateCacheDirty = true;

        BlockPos.Mutable mPos = updateClientPos.get();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        invalidateClientCache(mPos.set(x - 1, y - 1, z - 1));
        invalidateClientCache(mPos.set(x - 1, y - 1, z));
        invalidateClientCache(mPos.set(x - 1, y - 1, z + 1));

        invalidateClientCache(mPos.set(x - 1, y, z - 1));
        invalidateClientCache(mPos.set(x - 1, y, z));
        invalidateClientCache(mPos.set(x - 1, y, z + 1));

        invalidateClientCache(mPos.set(x - 1, y + 1, z - 1));
        invalidateClientCache(mPos.set(x - 1, y + 1, z));
        invalidateClientCache(mPos.set(x - 1, y + 1, z + 1));

        invalidateClientCache(mPos.set(x, y - 1, z - 1));
        invalidateClientCache(mPos.set(x, y - 1, z));
        invalidateClientCache(mPos.set(x, y - 1, z + 1));

        invalidateClientCache(mPos.set(x, y, z - 1));
        invalidateClientCache(mPos.set(x, y, z + 1));

        invalidateClientCache(mPos.set(x, y + 1, z - 1));
        invalidateClientCache(mPos.set(x, y + 1, z));
        invalidateClientCache(mPos.set(x, y + 1, z + 1));

        invalidateClientCache(mPos.set(x + 1, y - 1, z - 1));
        invalidateClientCache(mPos.set(x + 1, y - 1, z));
        invalidateClientCache(mPos.set(x + 1, y - 1, z + 1));

        invalidateClientCache(mPos.set(x + 1, y, z - 1));
        invalidateClientCache(mPos.set(x + 1, y, z));
        invalidateClientCache(mPos.set(x + 1, y, z + 1));

        invalidateClientCache(mPos.set(x + 1, y + 1, z - 1));
        invalidateClientCache(mPos.set(x + 1, y + 1, z));
        invalidateClientCache(mPos.set(x + 1, y + 1, z + 1));

        MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    private void invalidateClientCache(BlockPos updatePos) {
        BlockEntity target = this.world.getBlockEntity(updatePos);
        if (target != null && target instanceof BrocadeTileEntity) {
            ((BrocadeTileEntity) target).isModelStateCacheDirty = true;
        }
    }

    @Override
    public void fromTag(CompoundTag compound) {
        super.fromTag(compound);
        this.modelState = MeshStateImpl.deserializeFromNBTIfPresent(compound);
        this.onModelStateChange(true);
    }

    @Override
    public CompoundTag toTag(CompoundTag compound) {
        compound = super.toTag(compound);
        this.modelState.serializeNBT(compound);
        return compound;
    }

    public MeshState getModelState(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        MeshState result = this.modelState;

        if (result == null) {
            result = ((BrocadeBlock) state.getBlock()).getDefaultModelState();
            this.modelState = result;
            this.isModelStateCacheDirty = true;

            // necessary for species
            refreshFromWorldIfNeeded = true;
        }

        if (this.isModelStateCacheDirty && refreshFromWorldIfNeeded) {
            result.refreshFromWorld(state, world, pos);
            this.isModelStateCacheDirty = false;
        }

        return result;
    }

    /**
     * Use this version when you don't have world state handy
     */
    public MeshState getModelState() {
        if (!(this.modelState == null || this.isModelStateCacheDirty)) {
            return this.modelState;
        } else {
            return getModelState(world.getBlockState(pos), world, pos, true);
        }
    }

//    /**
//     * intended for use in TESR - don't refresh unless missing because should be up
//     * to date from getExtendedState called before this
//     */
//    @SuppressWarnings("null")
//    public ISuperModelState getCachedModelState() {
//        return this.modelState == null
//                ? getModelState(world.getBlockState(pos), world, pos, true)
//                : this.modelState;
//    }

    public void setModelState(MeshState modelState) {
        // if making existing appearance static, don't need to refresh on client side
        boolean needsClientRefresh = this.world != null && this.world.isClient
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
            if (this.world.isClient) {
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

    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return withoutServerTag(toTag(tag));
    }
}
