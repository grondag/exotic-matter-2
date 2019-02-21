package grondag.brocade.primitives.stream;

/**
 * Implementations of IPolyStream that declare this interface guarantee that no
 * polygon in the stream will be mutated.
 * <p>
 * 
 * If present means IMutablePolyStream is NOT implemented but
 * IWritablePolyStream may be. Use to exclude mutable streams from use cases
 * where they would cause problems.
 */
public interface IReadOnlyPolyStream extends IPolyStream {

}
