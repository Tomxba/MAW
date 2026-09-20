package fr.maw.tool.brush;

import fr.maw.MawConfig;
import fr.maw.async.MawAsyncEngine;
import fr.maw.async.TickDispatcher;
import fr.maw.pattern.Mask;
import fr.maw.pattern.Pattern;
import fr.maw.session.SessionManager;
import fr.maw.tool.HandClickType;
import fr.maw.tool.Tool;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;

/**
 * Base abstract class for WorldEdit brushes.
 */
public abstract class Brush implements Tool {

    protected final MawConfig config;
    protected final SessionManager sessionManager;
    protected final MawAsyncEngine asyncEngine;
    protected final TickDispatcher dispatcher;

    protected Pattern pattern;
    protected int radius;
    protected Mask mask;
    protected boolean hollow;

    public Brush(
            MawConfig config,
            SessionManager sessionManager,
            MawAsyncEngine asyncEngine,
            TickDispatcher dispatcher,
            Pattern pattern,
            int radius,
            boolean hollow
    ) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.asyncEngine = asyncEngine;
        this.dispatcher = dispatcher;
        this.pattern = pattern;
        this.radius = Math.max(1, Math.min(radius, 50));
        this.hollow = hollow;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(1, Math.min(radius, 50));
    }

    public Mask getMask() {
        return mask;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
    }

    public boolean isHollow() {
        return hollow;
    }

    public void setHollow(boolean hollow) {
        this.hollow = hollow;
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (clickType != HandClickType.RIGHT_CLICK) {
            return false;
        }
        if (targetBlock == null) {
            return false;
        }
        applyBrush(player, instance, targetBlock, face);
        return true;
    }

    /**
     * Applies the brush geometry and block edits at the target location.
     */
    protected abstract void applyBrush(Player player, Instance instance, Point targetBlock, BlockFace face);
}
