package fr.maw.clipboard;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.*;

/**
 * Stores blocks and optional entities relative to an origin point (0, 0, 0).
 */
public final class Clipboard {

    private final Map<Point, Block> blocks;
    private final List<EntityData> entities;
    private final Point origin;
    private final Point dimensions;

    public Clipboard(Map<Point, Block> blocks, List<EntityData> entities, Point origin, Point dimensions) {
        this.blocks = Collections.unmodifiableMap(new HashMap<>(blocks));
        this.entities = entities != null ? Collections.unmodifiableList(new ArrayList<>(entities)) : Collections.emptyList();
        this.origin = origin != null ? origin : Vec.ZERO;
        this.dimensions = dimensions != null ? dimensions : Vec.ZERO;
    }

    public Clipboard(Map<Point, Block> blocks, Point origin, Point dimensions) {
        this(blocks, Collections.emptyList(), origin, dimensions);
    }

    public Map<Point, Block> getBlocks() {
        return blocks;
    }

    public List<EntityData> getEntities() {
        return entities;
    }

    public Point getOrigin() {
        return origin;
    }

    public Point getDimensions() {
        return dimensions;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public boolean hasEntities() {
        return !entities.isEmpty();
    }
}
