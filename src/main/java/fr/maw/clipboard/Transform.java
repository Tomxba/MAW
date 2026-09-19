package fr.maw.clipboard;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Geometric transformations for clipboards (rotation, reflection/flip) including block state updates.
 */
public final class Transform {

    public enum Direction {
        NORTH, SOUTH, EAST, WEST, UP, DOWN
    }

    private Transform() {}

    /**
     * Rotates a clipboard clockwise around the Y-axis by the given angle in degrees (must be 90, 180, or 270).
     */
    public static Clipboard rotate(Clipboard clipboard, int degrees) {
        int normalized = ((degrees % 360) + 360) % 360;
        if (normalized == 0) return clipboard;
        if (normalized % 90 != 0) {
            throw new IllegalArgumentException("Rotation angle must be a multiple of 90 degrees, got " + degrees);
        }

        int steps = normalized / 90;
        Clipboard current = clipboard;
        for (int i = 0; i < steps; i++) {
            current = rotate90Clockwise(current);
        }
        return current;
    }

    private static Clipboard rotate90Clockwise(Clipboard clipboard) {
        Map<Point, Block> rotatedBlocks = new HashMap<>(clipboard.getBlocks().size());

        for (Map.Entry<Point, Block> entry : clipboard.getBlocks().entrySet()) {
            Point p = entry.getKey();
            Block b = entry.getValue();

            // 90 deg CW: (x, y, z) -> (-z, y, x)
            Point newPoint = new Vec(-p.blockZ(), p.blockY(), p.blockX());
            Block newBlock = rotateBlock90(b);
            rotatedBlocks.put(newPoint, newBlock);
        }

        Point orig = clipboard.getOrigin();
        Point newOrigin = new Vec(-orig.blockZ(), orig.blockY(), orig.blockX());

        Point dim = clipboard.getDimensions();
        Point newDim = new Vec(dim.blockZ(), dim.blockY(), dim.blockX());

        return new Clipboard(rotatedBlocks, clipboard.getEntities(), newOrigin, newDim);
    }

    /**
     * Flips (reflects) a clipboard along the specified direction/axis.
     */
    public static Clipboard flip(Clipboard clipboard, Direction direction) {
        Map<Point, Block> flippedBlocks = new HashMap<>(clipboard.getBlocks().size());

        for (Map.Entry<Point, Block> entry : clipboard.getBlocks().entrySet()) {
            Point p = entry.getKey();
            Block b = entry.getValue();

            Point newPoint;
            Block newBlock = b;

            switch (direction) {
                case EAST, WEST -> {
                    newPoint = new Vec(-p.blockX(), p.blockY(), p.blockZ());
                    newBlock = flipBlockX(b);
                }
                case NORTH, SOUTH -> {
                    newPoint = new Vec(p.blockX(), p.blockY(), -p.blockZ());
                    newBlock = flipBlockZ(b);
                }
                case UP, DOWN -> {
                    newPoint = new Vec(p.blockX(), -p.blockY(), p.blockZ());
                    newBlock = flipBlockY(b);
                }
                default -> newPoint = p;
            }
            flippedBlocks.put(newPoint, newBlock);
        }

        Point orig = clipboard.getOrigin();
        Point newOrigin;
        switch (direction) {
            case EAST, WEST -> newOrigin = new Vec(-orig.blockX(), orig.blockY(), orig.blockZ());
            case NORTH, SOUTH -> newOrigin = new Vec(orig.blockX(), orig.blockY(), -orig.blockZ());
            case UP, DOWN -> newOrigin = new Vec(orig.blockX(), -orig.blockY(), orig.blockZ());
            default -> newOrigin = orig;
        }

        return new Clipboard(flippedBlocks, clipboard.getEntities(), newOrigin, clipboard.getDimensions());
    }

    private static Block rotateBlock90(Block block) {
        String facing = block.getProperty("facing");
        if (facing != null) {
            String newFacing = switch (facing) {
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> facing;
            };
            block = block.withProperty("facing", newFacing);
        }

        String axis = block.getProperty("axis");
        if (axis != null) {
            String newAxis = switch (axis) {
                case "x" -> "z";
                case "z" -> "x";
                default -> axis;
            };
            block = block.withProperty("axis", newAxis);
        }

        String rot = block.getProperty("rotation");
        if (rot != null) {
            try {
                int r = Integer.parseInt(rot);
                int newRot = (r + 4) % 16;
                block = block.withProperty("rotation", String.valueOf(newRot));
            } catch (NumberFormatException ignored) {}
        }

        return block;
    }

    private static Block flipBlockX(Block block) {
        String facing = block.getProperty("facing");
        if (facing != null) {
            String newFacing = switch (facing) {
                case "east" -> "west";
                case "west" -> "east";
                default -> facing;
            };
            block = block.withProperty("facing", newFacing);
        }
        return block;
    }

    private static Block flipBlockZ(Block block) {
        String facing = block.getProperty("facing");
        if (facing != null) {
            String newFacing = switch (facing) {
                case "north" -> "south";
                case "south" -> "north";
                default -> facing;
            };
            block = block.withProperty("facing", newFacing);
        }
        return block;
    }

    private static Block flipBlockY(Block block) {
        String facing = block.getProperty("facing");
        if (facing != null) {
            String newFacing = switch (facing) {
                case "up" -> "down";
                case "down" -> "up";
                default -> facing;
            };
            block = block.withProperty("facing", newFacing);
        }
        String half = block.getProperty("half");
        if (half != null) {
            String newHalf = switch (half) {
                case "top" -> "bottom";
                case "bottom" -> "top";
                default -> half;
            };
            block = block.withProperty("half", newHalf);
        }
        return block;
    }
}
