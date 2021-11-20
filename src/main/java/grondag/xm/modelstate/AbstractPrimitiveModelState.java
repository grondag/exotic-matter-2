/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.BLOCK_SPECIES;
import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import io.vram.bitkit.BitPacker32;

import grondag.fermion.orientation.api.OrientationType;
import grondag.fermion.varia.Useful;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.connect.CornerJoinStateSelector;
import grondag.xm.dispatch.AbstractXmModel;
import grondag.xm.paint.XmPaintImpl;
import grondag.xm.painter.PaintManager;
import grondag.xm.texture.TextureSetHelper;

// WIP: Fabric deps
@SuppressWarnings({"rawtypes", "unchecked"})
@Internal
public abstract class AbstractPrimitiveModelState<V extends AbstractPrimitiveModelState<V, R, W>, R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> extends AbstractModelState
	implements MutableModelState, BaseModelState<R, W>, MutableBaseModelState<R, W> {
	////////////////////////////////////////// BIT-WISE ENCODING //////////////////////////////////////////

	/** Note that sign bit on world encoder is reserved to persist static state during serialization. */
	private static final BitPacker32<AbstractPrimitiveModelState> WORLD_ENCODER = new BitPacker32<>(m -> m.worldBits, (m, b) -> m.worldBits = b);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_X = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Y = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Z = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement SPECIES = WORLD_ENCODER.createIntElement(16);

	public static final int PRIMITIVE_BIT_COUNT;
	private static final BitPacker32<AbstractPrimitiveModelState> SHAPE_ENCODER = new BitPacker32<>(m -> m.shapeBits, (m, b) -> m.shapeBits = b);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement ORIENTATION = SHAPE_ENCODER.createIntElement(32);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement BLOCK_JOIN = SHAPE_ENCODER.createIntElement(CornerJoinState.STATE_COUNT);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement ALTERNATE_JOIN = SHAPE_ENCODER.createIntElement(SimpleJoinState.STATE_COUNT);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement PRIMITIVE_BITS;

	static {
		assert WORLD_ENCODER.bitLength() <= 32;
		assert SHAPE_ENCODER.bitLength() <= 32;
		PRIMITIVE_BIT_COUNT = 32 - SHAPE_ENCODER.bitLength();
		PRIMITIVE_BITS = SHAPE_ENCODER.createIntElement(1 << PRIMITIVE_BIT_COUNT);
		assert SHAPE_ENCODER.bitLength() <= 32;
	}

	static Consumer<AbstractPrimitiveModelState> clientClearHandler = s -> { };

	public static void useClientHandler() {
		clientClearHandler = s -> s.clearRendering();
	}

	////////////////////////////////////////// FACTORY //////////////////////////////////////////

	public static class ModelStateFactoryImpl<T extends AbstractPrimitiveModelState<T, R, W>, R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> implements BaseModelStateFactory<R, W> {
		private final ArrayBlockingQueue<T> POOL = new ArrayBlockingQueue<>(4096);
		private final Supplier<T> factory;

		public ModelStateFactoryImpl(Supplier<T> factory) {
			this.factory = factory;
		}

		public final T claimInner(ModelPrimitive<R, W> primitive) {
			T result = POOL.poll();

			if (result == null) {
				result = factory.get();
				result.isImmutable = false;
			} else {
				result.clear();
			}

			result.primitive = primitive;
			result.retain();
			return result;
		}

		protected final T claimInner(T template) {
			T result = POOL.poll();

			if (result == null) {
				result = factory.get();
				result.isImmutable = false;
			} else {
				result.clear();
			}

			result.retain();
			result.primitive = template.primitive;
			result.copyInternal(template);
			return result;
		}

		@Override
		public final W claim(ModelPrimitive<R, W> primitive) {
			return (W) claimInner(primitive);
		}

		public final W claim(R template, ModelPrimitive<R, W> primitive) {
			final W result = claim(primitive);
			result.copyFrom(template);
			return result;
		}

		public final W claim(R template) {
			return claim(template, template.primitive());
		}

		@Override
		public final W fromTag(ModelPrimitive<R, W> shape, CompoundTag tag, PaintIndex sync) {
			final T result = claimInner(shape);
			result.fromTag(tag, sync);
			return (W) result;
		}

		@Override
		public final W fromBytes(ModelPrimitive<R, W> shape, FriendlyByteBuf buf, PaintIndex sync) {
			final T result = claimInner(shape);
			result.fromBytes(buf, sync);
			return (W) result;
		}
	}

	////////////////////////////////////////// GENERAL DECLARATIONS //////////////////////////////////////////

	@Override
	protected int computeHashCode() {
		int result = 0;
		final int limit = this.surfaceCount();

		for (int i = 0; i < limit; i++) {
			final XmPaint p = paints[i];

			if (p != null) {
				result = result * 31 + p.hashCode();
			}
		}

		return (result * 31 + HashCommon.mix(this.shapeBits) * 31) + HashCommon.mix(this.worldBits);
	}

	protected abstract ModelStateFactoryImpl<V, R, W> factoryImpl();

	@Override
	public final BaseModelStateFactory<R, W> factory() {
		return factoryImpl();
	}

	@Override
	public final <T> T applyAndRelease(Function<ModelState, T> func) {
		final T result = func.apply(this);
		release();
		return result;
	}

	@Override
	public final W apply(Consumer<W> consumer) {
		final W self = (W) this;
		consumer.accept(self);
		return self;
	}

	@Override
	protected final void onLastRelease() {
		factoryImpl().POOL.offer((V) this);
	}

	@Override
	public R toImmutable() {
		if (isImmutable) {
			return (R) this;
		} else {
			final V result = factoryImpl().claimInner((V) this);
			result.isImmutable = true;
			return (R) result;
		}
	}

	protected <T extends AbstractPrimitiveModelState> void copyInternal(T template) {
		final AbstractPrimitiveModelState other = template;
		System.arraycopy(template.paints, 0, this.paints, 0, this.surfaceCount());
		worldBits = other.worldBits;
		shapeBits = other.shapeBits;
	}

	protected void clear() {
		Arrays.fill(paints, XmPaintImpl.DEFAULT_PAINT);
		worldBits = 0;
		shapeBits = 0;
		clearStateFlags();
		invalidateHashCode();
		clientClearHandler.accept(this);
	}

	@Override
	public W copyFrom(ModelState template) {
		copyInternal((V) template);
		return (W) this;
	}

	@Override
	public R releaseToImmutable() {
		final R result = this.toImmutable();
		release();
		return result;
	}

	@Override
	public W mutableCopy() {
		return (W) factoryImpl().claimInner((V) this);
	}

	////////////////////////////////////////// COMPARISON //////////////////////////////////////////

	/**
	 * Does NOT consider isStatic in comparison.
	 *
	 * <p>{@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		return this == obj ? true : obj != null && obj.getClass() == this.getClass() && equalsInner(obj);
	}

	protected boolean equalsInner(Object obj) {
		final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
		return this.primitive == other.primitive
				&& this.worldBits == other.worldBits
				&& this.shapeBits == other.shapeBits
				&& doPaintsMatchNative(other);
	}

	//    @Override
	//    public final boolean equalsIncludeStatic(Object obj) {
	//        if (this == obj)
	//            return true;
	//
	//        if (obj instanceof AbstractPrimitiveModelState) {
	//            AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
	//            return this.isStatic == other.isStatic && equalsInner(other);
	//        } else {
	//            return false;
	//        }
	//    }

	/**
	 * Returns true if visual elements and geometry match. Does not consider species
	 * in matching.
	 * @deprecated - not used and will be removed
	 */
	@Deprecated
	@Override
	public final boolean doShapeAndAppearanceMatch(ModelState other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}

		return primitive.doesShapeMatch((R) this, (R) other) && doesAppearanceMatch(other);
	}

	/**
	 * Returns true if visual elements match. Does not consider species or geometry
	 * in matching.
	 * @deprecated - not used and will be removed
	 */
	@Deprecated
	@Override
	public final boolean doesAppearanceMatch(ModelState other) {
		return other != null && doPaintsMatch(other);
	}

	////////////////////////////////////////// SERIALIZATION //////////////////////////////////////////

	@Override
	public void fromTag(CompoundTag tag, PaintIndex paintIndex) {
		final int worldBits = tag.getInt(ModelStateTagHelper.NBT_WORLD_BITS);
		// sign on world bits is used to store static indicator
		isStatic = (Useful.INT_SIGN_BIT & worldBits) == Useful.INT_SIGN_BIT;
		this.worldBits = Useful.INT_SIGN_BIT_INVERSE & worldBits;
		shapeBits = tag.getInt(ModelStateTagHelper.NBT_SHAPE_BITS);

		final ListTag paintList = tag.getList(ModelStateTagHelper.NBT_PAINTS, 10);
		final int limit = paintList.size();

		for (int i = 0; i < limit; ++i) {
			paints[i] = XmPaint.fromTag(paintList.getCompound(i), paintIndex);
		}

		clearStateFlags();
	}

	@Override
	public void toTag(CompoundTag tag) {
		tag.putString(ModelStateTagHelper.NBT_SHAPE, this.primitive().id().toString());

		tag.putInt(ModelStateTagHelper.NBT_WORLD_BITS, this.isStatic ? (worldBits | Useful.INT_SIGN_BIT) : worldBits);
		tag.putInt(ModelStateTagHelper.NBT_SHAPE_BITS, shapeBits);

		final int limit = paints.length;
		final ListTag paintList = new ListTag();

		for (int i = 0; i < limit; ++i) {
			final XmPaint p = paints[i];
			paintList.add((p == null ? XmPaintImpl.DEFAULT_PAINT : p).toTag());
		}

		tag.put(ModelStateTagHelper.NBT_PAINTS, paintList);
	}

	@Override
	public void fromBytes(FriendlyByteBuf pBuff, PaintIndex paintIndex) {
		shapeBits = pBuff.readInt();
		worldBits = pBuff.readInt();
		final int limit = primitive.surfaces((R) this).size();

		for (int i = 0; i < limit; i++) {
			this.paints[i] = XmPaint.fromBytes(pBuff, paintIndex);
		}
	}

	@Override
	public void toBytes(FriendlyByteBuf pBuff) {
		pBuff.writeVarInt(primitive.index());
		pBuff.writeInt(shapeBits);
		pBuff.writeInt(worldBits);
		final int limit = primitive.surfaces((R) this).size();

		for (int i = 0; i < limit; i++) {
			paints[i].toBytes(pBuff);
		}
	}

	////////////////////////////////////////// STATE FLAGS //////////////////////////////////////////

	/** Contains indicators derived from shape and painters. */
	private int stateFlags = 0;

	@Override
	public final int stateFlags() {
		int result = stateFlags;

		if (result == 0) {
			result = ModelStateFlags.IS_POPULATED | primitive.stateFlags((R) this);
			final int surfCount = primitive.surfaces((R) this).size();

			for (int i = 0; i < surfCount; i++) {
				final XmPaint p = paint(i);
				final int texDepth = p.textureDepth();

				for (int j = 0; j < texDepth; j++) {
					result |= p.texture(j).stateFlags();
				}
			}

			stateFlags = result;
		}

		return result;
	}

	protected final void clearStateFlags() {
		stateFlags = 0;
	}

	////////////////////////////////////////// PRIMITIVE //////////////////////////////////////////

	protected ModelPrimitive<R, W> primitive;

	@Override
	public final ModelPrimitive<R, W> primitive() {
		return primitive;
	}

	@Override
	public final void emitPolygons(Consumer<Polygon> target) {
		primitive.emitQuads((R) this, target);
	}

	@Override
	public W geometricState() {
		return primitive.geometricState((R) this);
	}

	@Override
	public final OrientationType orientationType() {
		return primitive.orientationType((R) this);
	}

	protected final int surfaceCount() {
		return primitive.surfaces((R) this).size();
	}

	////////////////////////////////////////// WORLD REFRESH CONTROL //////////////////////////////////////////

	protected boolean isStatic = false;

	@Override
	public final boolean isStatic() {
		return this.isStatic;
	}

	@Override
	public final W setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return (W) this;
	}

	////////////////////////////////////////// PAINT //////////////////////////////////////////

	protected abstract int maxSurfaces();

	protected final XmPaint[] paints = new XmPaint[maxSurfaces()];

	@Override
	public final boolean doPaintsMatch(ModelState other) {
		return other instanceof AbstractPrimitiveModelState && doPaintsMatchNative((AbstractPrimitiveModelState) other);
	}

	protected final boolean doPaintsMatchNative(AbstractPrimitiveModelState other) {
		final int limit = surfaceCount();

		if (limit == other.surfaceCount()) {
			final XmPaint[] paints = this.paints;
			final XmPaint[] otherPaints = other.paints;

			for (int i = 0; i < limit; i++) {
				if (otherPaints[i] != paints[i]) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public final XmPaint paint(int surfaceIndex) {
		final XmPaint result = paints[surfaceIndex];
		return result == null ? XmPaintImpl.DEFAULT_PAINT : result;
	}

	@Override
	public final XmPaint paint(XmSurface surface) {
		return paint(surface.ordinal());
	}

	@Override
	public final W paint(int surfaceIndex, XmPaint paint) {
		paints[surfaceIndex] = paint;
		return (W) this;
	}

	@Override
	public final W paint(XmSurface surface, XmPaint paint) {
		return paint(surface.ordinal(), paint);
	}

	@Override
	public final W paintAll(XmPaint paint) {
		final XmSurfaceList slist = primitive().surfaces((R) this);
		final int limit = slist.size();

		for (int i = 0; i < limit; i++) {
			paint(i, paint);
		}

		return (W) this;
	}

	////////////////////////////////////////// WORLD ATTRIBUTES //////////////////////////////////////////

	protected int worldBits;

	@Override
	public int posX() {
		return POS_X.getValue(this);
	}

	@Override
	public int posY() {
		return POS_Y.getValue(this);
	}

	@Override
	public int posZ() {
		return POS_Z.getValue(this);
	}

	@Override
	public final W posX(int index) {
		POS_X.setValue(index, this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public final W posY(int index) {
		POS_Y.setValue(index, this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public final W posZ(int index) {
		POS_Z.setValue(index, this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public final W pos(BlockPos pos) {
		POS_X.setValue((pos.getX()), this);
		POS_Y.setValue((pos.getY()), this);
		POS_Z.setValue((pos.getZ()), this);
		invalidateHashCode();
		return (W) this;
	}

	/**
	 * Means that one or more elements (like a texture) uses species. Does not mean
	 * that the shape or block actually capture or generate species other than 0.
	 */
	@Override
	public final boolean hasSpecies() {
		final int stateFlags = stateFlags();
		return ((stateFlags & BLOCK_SPECIES) == BLOCK_SPECIES);
	}

	/**
	 * Will return 0 if model state does not include species. This is more
	 * convenient than checking each place species is used.
	 *
	 * @return
	 */
	@Override
	public final int species() {
		return SPECIES.getValue(this);
	}

	@Override
	public final W species(int species) {
		SPECIES.setValue(species, this);
		invalidateHashCode();
		return (W) this;
	}

	////////////////////////////////////////// SHAPE ATTRIBUTES //////////////////////////////////////////

	protected int shapeBits;

	@Override
	public int orientationIndex() {
		return ORIENTATION.getValue(this);
	}

	@Override
	public W orientationIndex(int index) {
		ORIENTATION.setValue(index, this);
		return (W) this;
	}

	@Override
	public CornerJoinState cornerJoin() {
		return CornerJoinStateSelector.fromOrdinal(Mth.clamp(BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
	}

	@Override
	public final W cornerJoin(CornerJoinState join) {
		final int stateFlags = stateFlags();
		assert (stateFlags & CORNER_JOIN) != 0 : "Attempt to set corner join for model state not requiring it";

		BLOCK_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public SimpleJoinState simpleJoin() {
		// If this state is using corner join, join index is for a corner join
		// and so need to derive simple join from the corner join
		final int stateFlags = stateFlags();
		return ((stateFlags & CORNER_JOIN) == 0) ? SimpleJoinState.fromOrdinal(BLOCK_JOIN.getValue(this)) : cornerJoin().simpleJoin();
	}

	@Override
	public final W simpleJoin(SimpleJoinState join) {
		final int stateFlags = stateFlags();
		assert (stateFlags & CORNER_JOIN) == 0 : "Attempt to set simple join for model state requiring corner join";

		BLOCK_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public SimpleJoinState alternateJoin() {
		return SimpleJoinState.fromOrdinal(ALTERNATE_JOIN.getValue(this));
	}

	@Override
	public int alternateJoinBits() {
		return ALTERNATE_JOIN.getValue(this);
	}

	@Override
	public final W alternateJoin(SimpleJoinState join) {
		ALTERNATE_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public final W alternateJoinBits(int joinBits) {
		ALTERNATE_JOIN.setValue(joinBits, this);
		invalidateHashCode();
		return (W) this;
	}

	@Override
	public int primitiveBits() {
		return PRIMITIVE_BITS.getValue(this);
	}

	@Override
	public final W primitiveBits(int bits) {
		PRIMITIVE_BITS.setValue(bits, this);
		invalidateHashCode();
		return (W) this;
	}

	////////////////////////////////////////// RENDERING //////////////////////////////////////////

	@Environment(EnvType.CLIENT)
	public void clearRendering() {
		mesh = null;
		particleSprite = null;
		particleColorARBG = 0;
		quadLists = null;
	}

	@Environment(EnvType.CLIENT)
	private Mesh mesh = null;

	@Environment(EnvType.CLIENT)
	public Mesh mesh() {
		Mesh result = mesh;

		if (result == null) {
			result = PaintManager.paint(this);
			mesh = result;
		}

		return result;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		primitive.emitBlockMesh(mesh(), blockView, state, pos, randomSupplier, context);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		primitive.emitItemMesh(mesh(), stack, randomSupplier, context);
	}

	@Environment(EnvType.CLIENT)
	private TextureAtlasSprite particleSprite = null;

	@Environment(EnvType.CLIENT)
	private int particleColorARBG = 0;

	@Override
	@Environment(EnvType.CLIENT)
	public final TextureAtlasSprite particleSprite() {
		if (particleSprite == null) {
			final Mesh mesh = mesh();
			mesh.forEach(q -> {
				if (particleSprite == null) {
					final SpriteFinder finder = SpriteFinder.get(TextureSetHelper.blockAtas());
					particleSprite = finder.find(q, 0);
					particleColorARBG = q.spriteColor(0, 0);
				}
			});

			if (particleSprite == null) {
				particleSprite = TextureSetHelper.missingSprite();
			}
		}

		return particleSprite;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public final int particleColorARBG() {
		final int result = particleColorARBG;

		if (result == 0) {
			// if zero then probably requires lookup
			particleSprite();
			return particleColorARBG;
		} else {
			return result;
		}
	}

	@Environment(EnvType.CLIENT)
	private List<BakedQuad>[] quadLists = null;

	@Override
	@Environment(EnvType.CLIENT)
	public final List<BakedQuad> bakedQuads(BlockState state, Direction face, Random rand) {
		List<BakedQuad>[] lists = quadLists;

		if (lists == null) {
			lists = ModelHelper.toQuadLists(mesh());
			quadLists = lists;
		}

		final List<BakedQuad> result = lists[face == null ? 6 : face.get3DDataValue()];
		return result == null ? ImmutableList.of() : result;
	}

	@Environment(EnvType.CLIENT)
	private BakedModel itemProxy = null;

	@Override
	@Environment(EnvType.CLIENT)
	public final BakedModel itemProxy() {
		BakedModel result = itemProxy;

		if (result == null) {
			result = new AbstractXmModel() {
					@Environment(EnvType.CLIENT)
					@Override
					public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
						return bakedQuads(state, face, random);
					}

					@Environment(EnvType.CLIENT)
					@Override
					public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
						AbstractPrimitiveModelState.this.emitBlockQuads(blockView, state, pos, randomSupplier, context);
					}

					@Environment(EnvType.CLIENT)
					@Override
					public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
						AbstractPrimitiveModelState.this.emitItemQuads(stack, randomSupplier, context);
					}
				};

			itemProxy = result;
		}

		return result;
	}
}
