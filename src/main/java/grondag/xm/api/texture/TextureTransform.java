package grondag.xm.api.texture;

import grondag.fermion.orientation.api.ClockwiseRotation;

public enum TextureTransform {
	IDENTITY(ClockwiseRotation.ROTATE_NONE, false),
	ROTATE_90(ClockwiseRotation.ROTATE_90, false),
	ROTATE_180(ClockwiseRotation.ROTATE_180, false),
	ROTATE_270(ClockwiseRotation.ROTATE_270, false),
	ROTATE_RANDOM(ClockwiseRotation.ROTATE_NONE, true),
	/** Use for tiles that must remain consistent for the same species */
	ROTATE_BIGTEX(ClockwiseRotation.ROTATE_NONE, true),
	/** Rotate 180 and allow horizontal texture flip */
	STONE_LIKE(ClockwiseRotation.ROTATE_NONE, true);

	public final ClockwiseRotation baseRotation;
	public final boolean hasRandom;

	private TextureTransform(ClockwiseRotation baseRotation, boolean hasRandom) {
		this.baseRotation = baseRotation;
		this.hasRandom = hasRandom;
	}
}
