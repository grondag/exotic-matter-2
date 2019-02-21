package grondag.brocade.placement;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Does two things: 1) Concise way to pass the block hit information for
 * placement events. 2) For floating selection, emulates block hit information
 * would get if had clicked on face behind the floating selection. This means we
 * can use consistent logic downstream for everything that uses block hit info.
 * 
 * Note that will do this even if floating selection if disabled but pass in
 * null values for hit information, so need to check floating selection before
 * calling this from an event that doesn't generate hit info, like clicking in
 * air. Such events generally display a GUI or have no effect if floating
 * selection is off.
 * 
 * For excavations, inPos will be the same as onPos unless floating selection is
 * on.
 */
public class PlacementPosition {
    public final EnumFacing onFace;
    public final BlockPos onPos;
    public final BlockPos inPos;
    public final double hitX;
    public final double hitY;
    public final double hitZ;
    public final boolean isFloating;

    /**
     * 
     * @param player
     * @param onPos
     * @param onFace
     * @param hitVec
     * @param floatingSelectionRange zero means non-floating
     * @param isExcavation           if true will select *in* starting block vs *on*
     *                               it
     */
    public PlacementPosition(EntityPlayer player, @Nullable BlockPos onPos, @Nullable EnumFacing onFace,
            @Nullable Vec3d hitVec, int floatingSelectionRange, boolean isExcavation) {

        this.isFloating = floatingSelectionRange > 0;
        if (this.isFloating || onPos == null || onFace == null || hitVec == null) {

            Vec3d start = player.getPositionEyes(1);
            Vec3d end = start.add(player.getLookVec().scale(floatingSelectionRange));

            this.inPos = new BlockPos(end);

            // have the position, now emulate which pos/face are we targeting
            // Do this by tracing towards the viewer from other side of block
            // to get the far-side hit. Hit coordinates are same irrespective
            // of face but need to the flip the face we get.
            RayTraceResult hit = Blocks.DIRT.getDefaultState().collisionRayTrace(player.world, this.inPos,
                    start.add(player.getLookVec().scale(10)), start);

            this.onPos = isExcavation ? this.inPos : this.inPos.offset(hit.sideHit);
            this.onFace = hit.sideHit.getOpposite();
            this.hitX = hit.hitVec.x;
            this.hitY = hit.hitVec.y;
            this.hitZ = hit.hitVec.z;
        } else {
            this.onFace = onFace;
            this.onPos = onPos;
            this.hitX = hitVec.x;
            this.hitY = hitVec.y;
            this.hitZ = hitVec.z;
            this.inPos = isExcavation ? this.onPos : this.onPos.offset(this.onFace);
        }
    }
}
