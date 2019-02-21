package grondag.brocade.primitives.vertex;

public interface IVertexCollection {
    public int vertexCount();

    // PERF: use value types instead
    @Deprecated
    public Vec3f getPos(int index);

    /**
     * Wraps around if index out of range.
     */
    public default Vec3f getPosModulo(int index) {
        return getPos(index % vertexCount());
    }
}
