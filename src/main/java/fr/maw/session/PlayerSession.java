package fr.maw.session;

import fr.maw.MawConfig;
import fr.maw.clipboard.Clipboard;
import fr.maw.history.HistoryManager;
import fr.maw.selection.CuboidSelection;
import fr.maw.selection.Selection;
import net.minestom.server.coordinate.Point;

import java.util.Objects;
import java.util.UUID;

/**
 * Manages player-specific WorldEdit state (selection, clipboard, history, settings).
 */
public final class PlayerSession {

    private final UUID uuid;
    private final HistoryManager historyManager;

    private Point pos1;
    private Point pos2;
    private Clipboard clipboard;
    private boolean updatePhysics;
    private boolean manageEntities;

    // Interactive tools and brushes
    private final java.util.Map<String, fr.maw.tool.Tool> boundTools = new java.util.concurrent.ConcurrentHashMap<>();
    private fr.maw.pattern.Mask brushMask;
    private fr.maw.pattern.Mask globalMask;
    private int brushSize = 2;
    private int brushRange = 100;
    private boolean fastMode = false;

    public PlayerSession(UUID uuid, MawConfig config) {
        this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
        this.historyManager = new HistoryManager(config.maxHistoryPerPlayer());
        this.updatePhysics = config.defaultUpdatePhysics();
        this.manageEntities = config.defaultManageEntities();
    }

    public UUID getUuid() {
        return uuid;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public synchronized Point getPos1() {
        return pos1;
    }

    public synchronized void setPos1(Point pos1) {
        this.pos1 = pos1;
    }

    public synchronized Point getPos2() {
        return pos2;
    }

    public synchronized void setPos2(Point pos2) {
        this.pos2 = pos2;
    }

    public synchronized boolean isSelectionComplete() {
        return pos1 != null && pos2 != null;
    }

    public synchronized Selection getSelection() {
        if (!isSelectionComplete()) {
            throw new IllegalStateException("Both pos1 and pos2 must be set to create a selection!");
        }
        return new CuboidSelection(pos1, pos2);
    }

    public synchronized Clipboard getClipboard() {
        return clipboard;
    }

    public synchronized void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    public synchronized boolean isUpdatePhysics() {
        return updatePhysics;
    }

    public synchronized void setUpdatePhysics(boolean updatePhysics) {
        this.updatePhysics = updatePhysics;
    }

    public synchronized boolean isManageEntities() {
        return manageEntities;
    }

    public synchronized void setManageEntities(boolean manageEntities) {
        this.manageEntities = manageEntities;
    }

    public void bindTool(String itemNamespace, fr.maw.tool.Tool tool) {
        if (itemNamespace == null || tool == null) return;
        boundTools.put(itemNamespace.toLowerCase(java.util.Locale.ROOT), tool);
    }

    public fr.maw.tool.Tool getTool(String itemNamespace) {
        if (itemNamespace == null) return null;
        return boundTools.get(itemNamespace.toLowerCase(java.util.Locale.ROOT));
    }

    public void unbindTool(String itemNamespace) {
        if (itemNamespace == null) return;
        boundTools.remove(itemNamespace.toLowerCase(java.util.Locale.ROOT));
    }

    public void clearTools() {
        boundTools.clear();
    }

    public synchronized fr.maw.pattern.Mask getBrushMask() {
        return brushMask;
    }

    public synchronized void setBrushMask(fr.maw.pattern.Mask brushMask) {
        this.brushMask = brushMask;
    }

    public synchronized fr.maw.pattern.Mask getGlobalMask() {
        return globalMask;
    }

    public synchronized void setGlobalMask(fr.maw.pattern.Mask globalMask) {
        this.globalMask = globalMask;
    }

    public synchronized int getBrushSize() {
        return brushSize;
    }

    public synchronized void setBrushSize(int brushSize) {
        this.brushSize = Math.max(1, Math.min(brushSize, 50));
    }

    public synchronized int getBrushRange() {
        return brushRange;
    }

    public synchronized void setBrushRange(int brushRange) {
        this.brushRange = Math.max(1, Math.min(brushRange, 200));
    }

    public synchronized boolean isFastMode() {
        return fastMode;
    }

    public synchronized void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }
}
