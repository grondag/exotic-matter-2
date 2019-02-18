package grondag.brocade.painting;

import net.minecraft.util.math.MathHelper;

public class Surface
{
    public final static class Builder
    {
        private SurfaceTopology topology;
        private float uvWrapDistance;
        private boolean ignoreDepthForRandomization;
        private boolean allowBorders;
        private boolean isLampGradient;
        private int layerDisabledFlags;
        
        private Builder()
        {
            this.topology = SurfaceTopology.CUBIC;
            this.uvWrapDistance = 1;
            this.allowBorders = true;
        }
        
        private Builder(Surface template)
        {
            this.topology = template.topology;
            this.uvWrapDistance = template.uvWrapDistance;
            this.ignoreDepthForRandomization = template.ignoreDepthForRandomization;
            this.allowBorders = template.allowBorders;
            this.isLampGradient = template.isLampGradient;
            this.layerDisabledFlags = template.layerDisabledFlags;
        }
        
        private Builder(Builder template)
        {
            this.topology = template.topology;
            this.uvWrapDistance = template.uvWrapDistance;
            this.ignoreDepthForRandomization = template.ignoreDepthForRandomization;
            this.allowBorders = template.allowBorders;
            this.isLampGradient = template.isLampGradient;
            this.layerDisabledFlags = template.layerDisabledFlags;
        };
        
        public final Builder withTopology(SurfaceTopology topology)
        {
            this.topology = topology;
            return this;
        }
        
        public final Builder withWrapDistance(double uvWrapDistance)
        {
            this.uvWrapDistance = clampUVScale(uvWrapDistance);
            return this;
        }
        
        public final Builder withWrapDistance(int uvWrapDistance)
        {
            this.uvWrapDistance = clampUVScale(uvWrapDistance);
            return this;
        }
        
        public final Builder withIgnoreDepthForRandomization(boolean ignoreDepthForRandomization)
        {
            this.ignoreDepthForRandomization = ignoreDepthForRandomization;
            return this;
        }
        
        public final Builder withAllowBorders(boolean allowBorders)
        {
            this.allowBorders = allowBorders;
            return this;
        }
        
        public final Builder withLampGradient(boolean isLampGradient)
        {
            this.isLampGradient = isLampGradient;
            return this;
        }
        
        /**
         * Note the new list <em>replaces</em> the existing list of disabled layers.
         */
        public final Builder withDisabledLayers(PaintLayer... disabledLayers)
        {
            this.layerDisabledFlags = PaintLayer.BENUMSET.getFlagsForIncludedValues(disabledLayers);
            return this;
        }
        
        /**
         * Convenient when only one or two layers are enabled.
         * Note the new list <em>replaces</em> the existing list of disabled layers.
         */
        public final Builder withEnabledLayers(PaintLayer... enabledLayers)
        {
            this.layerDisabledFlags = ~PaintLayer.BENUMSET.getFlagsForIncludedValues(enabledLayers);
            return this;
        }
        
        public final Surface build()
        {
            return new Surface(topology, uvWrapDistance, ignoreDepthForRandomization, 
                    allowBorders, isLampGradient, this.layerDisabledFlags);
        }
        
        @Override
        public Builder clone()
        {
            return new Builder(this);
        }
        
        public final SurfaceTopology topology() { return this.topology; }
        public final float uvWrapDistance() { return this.uvWrapDistance; }
        public final boolean ignoreDepthForRandomization() { return this.ignoreDepthForRandomization; }
        public final boolean allowBorders() { return this.allowBorders; }
        public final boolean isLampGradient() { return this.isLampGradient; }
        public final int layerDisabledFlags() { return this.layerDisabledFlags; }
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static Builder builder(Builder template)
    {
        return new Builder(template);
    }

    public static Builder builder(Surface template)
    {
        return new Builder(template);
    }
    
    public static Builder builder(SurfaceTopology topology)
    {
        return new Builder().withTopology(topology);
    }
    
    private static int clampUVScale(int rawValue)
    {
        if(rawValue <= 1) return 1;
        
        if((rawValue & rawValue - 1) == 0) return rawValue;
        
        final int high = MathHelper.smallestEncompassingPowerOfTwo(rawValue);
        final int low = high / 2;
        
        return (high - rawValue) < (rawValue - low) ? high : low;
    }
    
    private static int clampUVScale(double rawValue)
    {
        if(rawValue <= 1) return 1;
        
        return clampUVScale((int) Math.round(rawValue));
    }
    
    public final SurfaceTopology topology;
    
    /** 
     * The maximum wrapping uv distance for either dimension on this surface.<p>
     * 
     * Must be zero or positive. Setting to zero disable uvWrapping - painter will use a 1:1 scale.<p>
     * 
     * If the surface is painted with a texture larger than this distance, the texture will be
     * scaled down to fit in order to prevent visible seams. A scale of 4, for example,
     * would force a 32x32 texture to be rendered at 1/8 scale.<p>
     * 
     * if the surface is painted with a texture smaller than this distance, then the texture
     * will be zoomed tiled to fill the surface.<p>
     * 
     * Default is 0 and generally only comes into play for non-cubic surface painters.<p>
     * 
     * See also {@link SurfaceTopology#TILED}
     */
    public final float uvWrapDistance;
    
    /**
     * If true, texture painting should not vary by axis
     * orthogonal to the surface.  Ignored if {@link #textureSalt} is non-zero.
     */
    public final boolean ignoreDepthForRandomization;
    
    /**
     * If false, border and masonry painters will not render on this surface.
     * Set false for topologies that don't play well with borders.
     */
    public final boolean allowBorders;
    
    /** 
     * If true, generator will assign colors to vertexes to indicate proximity to lamp surface.
     * Vertices next to lamp have color WHITE and those away have color BLACK.
     * If the lighting mode for the surface is shaded, then quad bake should color
     * vertices to form a gradient. <p>
     * 
     * If the surface is full-brightness, need to re-color all vertices to white.
     */
    public final boolean isLampGradient;
    
    /**
     * Bits here indicate a layer is <em>disabled</em>
     */
    private final int layerDisabledFlags;

    public static final Surface NO_SURFACE = builder(SurfaceTopology.CUBIC).build();
    
    public final boolean isLayerDisabled(PaintLayer layer)
    {
        return PaintLayer.BENUMSET.isFlagSetForValue(layer, layerDisabledFlags);
    }
    
    private Surface(
            SurfaceTopology topology,
            float uvWrapDistance, 
            boolean ignoreDepthForRandomization, 
            boolean allowBorders, 
            boolean isLampGradient,
            int layerDisabledFlags)
    {
        this.topology = topology;
        this.uvWrapDistance = uvWrapDistance;
        this.ignoreDepthForRandomization = ignoreDepthForRandomization;
        this.allowBorders = allowBorders;
        this.isLampGradient = isLampGradient;
        this.layerDisabledFlags = layerDisabledFlags;
    }
}