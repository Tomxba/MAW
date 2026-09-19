package fr.maw.clipboard;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;

/**
 * Encapsulates entity data relative to clipboard origin when copying with -e flag.
 */
public record EntityData(EntityType entityType, Vec relativePosition, float yaw, float pitch, String customTag) {

    public EntityData(EntityType entityType, Vec relativePosition, float yaw, float pitch) {
        this(entityType, relativePosition, yaw, pitch, null);
    }
}
