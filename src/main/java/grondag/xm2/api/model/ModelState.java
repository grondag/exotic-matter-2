package grondag.xm2.api.model;

import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.paint.XmPaintRegistry;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

public interface ModelState {
    boolean isImmutable();

    ImmutableModelState toImmutable();

    MutableModelState mutableCopy();

    ModelWorldState worldState();

    /**
     * Persisted but not part of hash nor included in equals comparison. If true,
     * refreshFromWorldState does nothing.
     */
    boolean isStatic();

    int paintIndex(int surfaceIndex);

    default XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    default XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
    }

    Direction.Axis getAxis();

    boolean isAxisInverted();

    /**
     * Usage is determined by shape. Limited to 44 bits and does not update from
     * world.
     */
    long getStaticShapeBits();

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    ClockwiseRotation getAxisRotation();

    /**
     * Multiblock shapes also get a full 64 bits of information - does not update
     * from world
     */
    long getMultiBlockBits();

    TerrainState getTerrainState();

    long getTerrainStateKey();

    int getTerrainHotness();

    /**
     * True if base paint layer is translucent or lamp paint layer is present and
     * translucent.
     */
    boolean hasTranslucentGeometry();

    boolean hasMasonryJoin();

    boolean hasTextureRotation();

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    boolean hasSpecies();

    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    boolean doShapeAndAppearanceMatch(ModelState other);

    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    boolean doesAppearanceMatch(ModelState other);

    /**
     * Returns a copy of this model state with only the bits that matter for
     * geometry. Used as lookup key for block damage models.
     */
    ModelState geometricState();

    void serializeNBT(CompoundTag tag);
}
