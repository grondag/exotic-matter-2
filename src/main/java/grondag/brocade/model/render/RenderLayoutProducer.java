package grondag.brocade.model.render;

import grondag.exotic_matter.ExoticMatter;

/**
 * Selects of appropriate render layout based on availability of Acuity
 * Rendering API.
 * <p>
 * 
 * When API is available, quads with a solid base layer can always render
 * entirely in the solid block layer. When it isn't available, they will also
 * need to render in translucent.
 * <p>
 * 
 * Not an enum to avoid deadlocks during static initialization. (Was a problem,
 * not sure if still is.)
 */
public abstract class RenderLayoutProducer {
    public static final RenderLayoutProducer ALWAYS_SOLID = new Simple(0, RenderLayout.SOLID_ONLY);

    public static final RenderLayoutProducer ALWAYS_BOTH = new Simple(1, RenderLayout.SOLID_AND_TRANSLUCENT);

    public static final RenderLayoutProducer ALWAYS_TRANSLUCENT = new Simple(2, RenderLayout.TRANSLUCENT_ONLY);

    public static final RenderLayoutProducer ALWAYS_NONE = new Simple(3, RenderLayout.NONE);

    public static final RenderLayoutProducer DEPENDS = new RenderLayoutProducer(4) {
        @Override
        public final RenderLayout renderLayout() {
            return ExoticMatter.proxy.isAcuityEnabled() ? RenderLayout.SOLID_ONLY : RenderLayout.SOLID_AND_TRANSLUCENT;
        }
    };

    public static final RenderLayoutProducer[] VALUES = { ALWAYS_SOLID, ALWAYS_BOTH, ALWAYS_TRANSLUCENT, ALWAYS_NONE,
            DEPENDS };
    public static final int VALUE_COUNT = VALUES.length;

    public final int ordinal;

    public abstract RenderLayout renderLayout();

    private RenderLayoutProducer(int ordinal) {
        this.ordinal = ordinal;
    }

    private static class Simple extends RenderLayoutProducer {
        private final RenderLayout layout;

        private Simple(int ordinal, RenderLayout layout) {
            super(ordinal);
            this.layout = layout;
        }

        @Override
        public final RenderLayout renderLayout() {
            return this.layout;
        }
    }
}
