package grondag.xm.api.modelstate;

@FunctionalInterface
public interface ModelStateMap<T, V extends ModelState.Mutable> {
    V apply(T blockState);
    
    @FunctionalInterface
    public static interface Modifier<T, V extends ModelState.Mutable> {
        V apply(V modelState, T blockState);
    }
}
