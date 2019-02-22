package grondag.brocade.api.texture;

import static grondag.brocade.model.state.ModelStateData.*;

public enum TextureScale {
    /** 1x1 */
    SINGLE(0, STATE_FLAG_NONE),

    /** 2x2 */
    TINY(1, STATE_FLAG_NEEDS_POS),

    /** 4x4 */
    SMALL(2, STATE_FLAG_NEEDS_POS),

    /** 8x8 */
    MEDIUM(3, STATE_FLAG_NEEDS_POS),

    /** 16x16 */
    LARGE(4, STATE_FLAG_NEEDS_POS),

    /** 32x32 */
    GIANT(5, STATE_FLAG_NEEDS_POS);

    /**
     * UV length for each subdivision of the texture. Used by BigTex painter. Is
     * simply 1/{@link #sliceCount}.
     */
    public final float sliceIncrement;

    /**
     * Number of texture subdivisions for BigTex (each division is one block face).
     * Equivalently, the uv width/block faces covered by the texture if rendered at
     * 1:1 blockface:uv-distance scale.
     */
    public final int sliceCount;

    /** mask to derive a value within the number of slice counts (sliceCount - 1) */
    public final int sliceCountMask;

    /** number of texture subdivisions as an exponent of 2 */
    public final int power;

    /**
     * identifies the world state needed to drive texture random rotation/selection
     */
    public final int modelStateFlag;

    public TextureScale zoom() {
        if (this == GIANT) {
            return GIANT;
        } else {
            return values()[this.ordinal() + 1];
        }
    }

    private TextureScale(int power, int modelStateFlag) {
        this.power = power;
        this.sliceCount = 1 << power;
        this.sliceCountMask = sliceCount - 1;
        this.sliceIncrement = 1f / sliceCount;
        this.modelStateFlag = modelStateFlag;
    }

}