package grondag.xm.api.mesh;

import grondag.xm.mesh.XmMeshesImpl;

public class XmMeshes {
    private XmMeshes() {}
    
    public static WritableMesh claimWritable() {
        return XmMeshesImpl.claimWritable();
    }

    public static MutableMesh claimMutable() {
        return XmMeshesImpl.claimMutable();
    }

    /**
     * Creates a mesh with randomly recolored copies of the input mesh polygons.<p>
     * 
     * Does not modify or release the input mesh.
     */
    public static ReadOnlyMesh claimRecoloredCopy(XmMesh mesh) {
        return XmMeshesImpl.claimRecoloredCopy(mesh);
    }

    public static CsgMesh claimCsg() {
        return XmMeshesImpl.claimCsg();
    }

    public static CsgMesh claimCsg(XmMesh mesh) {
        return XmMeshesImpl.claimCsg(mesh);
    }

}
