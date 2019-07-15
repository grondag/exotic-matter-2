package grondag.xm2.api.allocation;

public interface Reference {
    boolean isImmutable();
    
    <T extends Reference> T toImmutable();
    
    public static interface Mutable extends Reference {
        @Override
        default boolean isImmutable() {
            return false;
        }
    }
    
    public static interface Owned extends Mutable {
        void release();
    }
    
    public static interface Immutable extends Reference {
        @Override
        default boolean isImmutable() {
            return true;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        default <T extends Reference> T toImmutable() {
            return (T) this;
        }
    }
}
