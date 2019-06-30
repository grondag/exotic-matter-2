package grondag.xm2.terrain;

/**
 * Terrain state is longer than 64 bits and we don't always want/need to
 * instantiate a TerrainState object to pass the raw bits around. Implement this
 * for objects that can consume the raw state directly. (Including terrain
 * state.)
 */
@FunctionalInterface
public interface ITerrainBitConsumer<T> {
    public T apply(long terrainBits, int hotnessBits);
}
