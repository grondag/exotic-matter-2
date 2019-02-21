package grondag.brocade.painting;

/**
 * Used by painters to know how to UV map a surface for painting.
 */
public enum SurfaceTopology {
    /**
     * Surface represents six faces of a single block. This is the topology to use
     * for any model that fits within a single world block position and has some
     * approximate orientation within the world.
     * <p>
     * 
     * Face is identified by {@link IPolygon#getNominalFace()} and does not have to
     * actually be on the face of a cube. (But it will be textured as if it were.)
     * <p>
     * 
     * Models generating quads for this topology should emit quads with min/max UVs
     * with the full span of 0-1. A UV span of 1 represents one in-world block. The
     * model mesh do not have to span a full block.
     * <p>
     * 
     * During BigTex painting, quad UV min/max will be set to some sub-span of 0-1
     * based on location within the world and texture scale. For single-block tiled
     * texture painting, the span is not altered but UV coordinates may be flipped,
     * rotated or the underlying texture randomized if multiple versions exist.
     * <p>
     * 
     * Surface UV scale is generally ignored for this surface type.
     */
    CUBIC,

    /**
     * Quad is part of a continuous surface. A UV distance of 1 should approximate 1
     * in-world block width.
     * <p>
     * 
     * UV coordinates should be kept positive for simplicity but can be in any
     * range. If any coordinate exceeds the size of the selected texture, the
     * painter will subdivide the quad to allow tiling and will also use the
     * coordinate information to randomize tiling and texture selection. This
     * prevent visible repetition on larger models and also means the mesh generator
     * doesn't have to consider texture tiling for textures that are smaller than
     * the whole surface - the painter will handle it.
     * <p>
     * 
     * If the surface wraps in any dimension, then periodicity should be
     * communicated in {@link SurfaceInstance#uvWrapDistance}. If that value is
     * non-zero, the painter will scale and tile textures to the closest
     * approximation of 1:1 UV to world scale. If the surface wraps in both
     * dimensions (toroids should be the only ones that do this) then the smallest
     * wrapping dimension should govern and to avoid seams the longer dimension
     * should wrap at a multiple of the smaller.
     * <p>
     * 
     * Some surface topologies need to wrap at edges but don't map to a square
     * texture - icosahedrons and cubic spheres, for example. For these, break the
     * model into quads that should connect and then emit (possibly subdivided)
     * quads with a wrap distance that is the same as the maxUV values used for each
     * face. For best results, use textures that can wrap in rotation. Some bigTex
     * textures won't join perfectly, but it's the best that can be done.
     * <p>
     * 
     * Unlike a cubic surface, in-world position can't be used for face
     * randomization within the model. As mentioned before, for surfaces large
     * enough to tile with a given texture, the painter will using tiling position
     * as data for randomization. The painter will also use texture salt (specified
     * in the surface instance) and nominal face (if provided) to drive texture
     * randomization. These last two are useful for smaller models that have to wrap
     * the same texture within a small visible region. (The painter <em>may</em> use
     * in-world origin position or other in-world information to randomize the whole
     * texturing process, but this does not provide variation of faces within the
     * model.)
     * <p>
     * 
     * Example 1: A cubic sphere of one block diameter has a UVwrap of ~1 with
     * nominal faces assigned to quads. All quads will have UV min of 0 and max
     * equal to the wrap distance. The quad painter will scale/tile the selected
     * texture to fit within the wrap distance span. Big textures will appear small
     * but will wrap correctly. Nominal face will be used to prevent visual
     * repetition.
     * <p>
     * 
     * Example 2: A single-texture cylinder of radius 1 and length 10 will have a
     * UVwrap distance ~6.2 because the texture must wrap on the circular surface
     * and the approximate circumference will be ~6.2. The cylinder is longer than 8
     * in the lengthwise dimension, so UV coordinate outside of 0-6.2 will be used
     * in that dimension, giving the painter information it can use to avoid
     * repetition along the length. Quads on the cylinder ends will use the same UV
     * scale so that textures appear similar, but do not need to wrap or join with
     * the lengthwise surface. If a 1x1, 2x2 or texture is selected, it will be
     * tiled within the surface by subdivision. 4x4 will probably be zoomed to fill
     * 6.2 vs. tiling.. An 8x8 texture will wrap around the cylinder exactly once at
     * something close to the intended visual scale and larger textures will be
     * scaled down to wrap exactly once.
     */
    TILED
}
