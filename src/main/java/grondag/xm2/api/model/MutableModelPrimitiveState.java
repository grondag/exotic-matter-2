package grondag.xm2.api.model;

public interface MutableModelPrimitiveState extends ModelPrimitiveState {
    /**
     * Also resets shape-specific bits to default for the given shape. Does nothing
     * if shape is the same as existing.
     */
    void primitive(ModelPrimitive shape);

}
