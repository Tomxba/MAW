package fr.maw.selection;

import fr.maw.clipboard.Transform.Direction;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.Locale;

/**
 * Geometric operations for expanding, contracting, shifting, and analyzing CuboidSelections.
 */
public final class SelectionHelper {

    private SelectionHelper() {}

    /**
     * Determines the primary direction the player is looking towards.
     */
    public static Direction getPlayerDirection(Player player) {
        Vec dir = player.getPosition().direction().normalize();
        if (Math.abs(dir.y()) > 0.7) {
            return dir.y() > 0 ? Direction.UP : Direction.DOWN;
        }
        if (Math.abs(dir.x()) > Math.abs(dir.z())) {
            return dir.x() > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dir.z() > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    /**
     * Parses a direction string (north, south, east, west, up, down, or u, d, n, s, e, w).
     */
    public static Direction parseDirection(String input, Direction fallback) {
        if (input == null || input.isBlank()) return fallback;
        String s = input.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            case "up", "u" -> Direction.UP;
            case "down", "d" -> Direction.DOWN;
            default -> fallback;
        };
    }

    /**
     * Expands selection in the given direction by amount.
     */
    public static CuboidSelection expand(CuboidSelection sel, int amount, Direction dir) {
        return expand(sel, amount, 0, dir);
    }

    /**
     * Expands selection in direction by amount, and in opposite direction by reverseAmount.
     */
    public static CuboidSelection expand(CuboidSelection sel, int amount, int reverseAmount, Direction dir) {
        int minX = sel.getMinX();
        int maxX = sel.getMaxX();
        int minY = sel.getMinY();
        int maxY = sel.getMaxY();
        int minZ = sel.getMinZ();
        int maxZ = sel.getMaxZ();

        switch (dir) {
            case NORTH -> {
                minZ -= amount;
                maxZ += reverseAmount;
            }
            case SOUTH -> {
                maxZ += amount;
                minZ -= reverseAmount;
            }
            case EAST -> {
                maxX += amount;
                minX -= reverseAmount;
            }
            case WEST -> {
                minX -= amount;
                maxX += reverseAmount;
            }
            case UP -> {
                maxY += amount;
                minY -= reverseAmount;
            }
            case DOWN -> {
                minY -= amount;
                maxY += reverseAmount;
            }
        }

        return new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
    }

    /**
     * Expands selection vertically from world minY to maxY (typically -64 to 319).
     */
    public static CuboidSelection expandVert(CuboidSelection sel, int worldMinY, int worldMaxY) {
        return new CuboidSelection(
                new Vec(sel.getMinX(), worldMinY, sel.getMinZ()),
                new Vec(sel.getMaxX(), worldMaxY, sel.getMaxZ())
        );
    }

    /**
     * Contracts selection in the given direction by amount.
     */
    public static CuboidSelection contract(CuboidSelection sel, int amount, Direction dir) {
        return contract(sel, amount, 0, dir);
    }

    /**
     * Contracts selection in direction by amount, and in opposite direction by reverseAmount.
     */
    public static CuboidSelection contract(CuboidSelection sel, int amount, int reverseAmount, Direction dir) {
        int minX = sel.getMinX();
        int maxX = sel.getMaxX();
        int minY = sel.getMinY();
        int maxY = sel.getMaxY();
        int minZ = sel.getMinZ();
        int maxZ = sel.getMaxZ();

        switch (dir) {
            case NORTH -> {
                maxZ -= amount;
                minZ += reverseAmount;
            }
            case SOUTH -> {
                minZ += amount;
                maxZ -= reverseAmount;
            }
            case EAST -> {
                minX += amount;
                maxX -= reverseAmount;
            }
            case WEST -> {
                maxX -= amount;
                minX += reverseAmount;
            }
            case UP -> {
                minY += amount;
                maxY -= reverseAmount;
            }
            case DOWN -> {
                maxY -= amount;
                minY += reverseAmount;
            }
        }

        // Clamp so min doesn't cross max
        if (minX > maxX) minX = maxX = (minX + maxX) / 2;
        if (minY > maxY) minY = maxY = (minY + maxY) / 2;
        if (minZ > maxZ) minZ = maxZ = (minZ + maxZ) / 2;

        return new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
    }

    /**
     * Shifts (translates) selection boundaries without modifying blocks.
     */
    public static CuboidSelection shift(CuboidSelection sel, int amount, Direction dir) {
        int dx = 0, dy = 0, dz = 0;
        switch (dir) {
            case NORTH -> dz = -amount;
            case SOUTH -> dz = amount;
            case EAST -> dx = amount;
            case WEST -> dx = -amount;
            case UP -> dy = amount;
            case DOWN -> dy = -amount;
        }

        return new CuboidSelection(
                new Vec(sel.getMinX() + dx, sel.getMinY() + dy, sel.getMinZ() + dz),
                new Vec(sel.getMaxX() + dx, sel.getMaxY() + dy, sel.getMaxZ() + dz)
        );
    }

    /**
     * Insets (shrinks) selection uniformly on all axes by amount.
     */
    public static CuboidSelection inset(CuboidSelection sel, int amount) {
        int minX = sel.getMinX() + amount;
        int maxX = sel.getMaxX() - amount;
        int minY = sel.getMinY() + amount;
        int maxY = sel.getMaxY() - amount;
        int minZ = sel.getMinZ() + amount;
        int maxZ = sel.getMaxZ() - amount;

        if (minX > maxX) minX = maxX = (minX + maxX) / 2;
        if (minY > maxY) minY = maxY = (minY + maxY) / 2;
        if (minZ > maxZ) minZ = maxZ = (minZ + maxZ) / 2;

        return new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
    }

    /**
     * Outsets (expands) selection uniformly on all axes by amount.
     */
    public static CuboidSelection outset(CuboidSelection sel, int amount) {
        int minX = sel.getMinX() - amount;
        int maxX = sel.getMaxX() + amount;
        int minY = sel.getMinY() - amount;
        int maxY = sel.getMaxY() + amount;
        int minZ = sel.getMinZ() - amount;
        int maxZ = sel.getMaxZ() + amount;

        return new CuboidSelection(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));
    }
}
