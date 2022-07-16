package grondag.xm.api.primitive.surface;

import grondag.xm.util.SimpleEnumCodec;

public enum SurfaceLocation {
	/** Part of outside surface. */
	OUTSIDE,

	/** Part of inside surface for inlays, insets, etc. */
	INSIDE,

	/** Part of cut surfaces between outside and inside surfaces.  */
	CUT,

	/** Faces parallel to axis. */
	SIDES,

	/** Faces orthogonal to axis. */
	ENDS,

	/** Top surface if present and potentially different from other sides. */
	TOP,

	/** Bottom surface if present and potentially different from other sides. */
	BOTTOM,

	/** Left surface if present and potentially different from other sides. */
	LEFT,

	/** Top surface if present and potentially different from other sides. */
	RIGHT,

	/** Top surface if present and potentially different from other sides. */
	FRONT,

	/** Top surface if present and potentially different from other sides. */
	BACK;

	public static final SimpleEnumCodec<SurfaceLocation> CODEC = new SimpleEnumCodec<>(SurfaceLocation.class);
	public static final int COUNT = CODEC.count;
}
