package grondag.xm2.model.state;

import grondag.xm2.api.model.ModelPrimitive;

public abstract class AbstractModelState {
    protected final ModelPrimitive primitive;
    
    protected final int[] paints;
    
    protected AbstractModelState(ModelPrimitive primitive) {
        this.primitive = primitive;
        paints = new int[primitive.surfaces().size()];
    }
    
    protected boolean isStatic = false;

    public boolean isStatic() {
        return this.isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public ModelPrimitive primitive() {
        return primitive;
    }
}
