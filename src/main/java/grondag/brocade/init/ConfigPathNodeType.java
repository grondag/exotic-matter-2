package grondag.brocade.init;

import javax.annotation.Nonnull;

import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.IStringSerializable;

/**
 * Copy of MC PathNodeType that implements IStringSerializable
 *
 */
public enum ConfigPathNodeType implements IStringSerializable {
    BLOCKED(PathNodeType.BLOCKED), OPEN(PathNodeType.OPEN), WALKABLE(PathNodeType.WALKABLE),
    TRAPDOOR(PathNodeType.TRAPDOOR), FENCE(PathNodeType.FENCE), LAVA(PathNodeType.LAVA), WATER(PathNodeType.WATER),
    RAIL(PathNodeType.RAIL), DANGER_FIRE(PathNodeType.DANGER_FIRE), DAMAGE_FIRE(PathNodeType.DAMAGE_FIRE),
    DANGER_CACTUS(PathNodeType.DANGER_CACTUS), DAMAGE_CACTUS(PathNodeType.DAMAGE_CACTUS),
    DANGER_OTHER(PathNodeType.DANGER_OTHER), DAMAGE_OTHER(PathNodeType.DAMAGE_OTHER), DOOR_OPEN(PathNodeType.DOOR_OPEN),
    DOOR_WOOD_CLOSED(PathNodeType.DOOR_WOOD_CLOSED), DOOR_IRON_CLOSED(PathNodeType.DOOR_IRON_CLOSED);

    public final PathNodeType pathNodeType;

    private ConfigPathNodeType(PathNodeType pathNodeType) {
        this.pathNodeType = pathNodeType;
    }

    @Override
    public @Nonnull String getName() {
        return this.name().toLowerCase();
    }
}
