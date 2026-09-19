package fr.maw.history;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages Undo and Redo histories for a player session.
 */
public final class HistoryManager {

    private final int maxHistory;
    private final Deque<ChangeSet> undoStack = new ArrayDeque<>();
    private final Deque<ChangeSet> redoStack = new ArrayDeque<>();

    public HistoryManager(int maxHistory) {
        this.maxHistory = Math.max(1, maxHistory);
    }

    public synchronized void record(ChangeSet changeSet) {
        if (changeSet == null || changeSet.isEmpty()) return;

        undoStack.push(changeSet);
        if (undoStack.size() > maxHistory) {
            undoStack.removeLast();
        }
        redoStack.clear();
    }

    public synchronized ChangeSet popUndo() {
        if (undoStack.isEmpty()) return null;
        ChangeSet set = undoStack.pop();
        redoStack.push(set);
        if (redoStack.size() > maxHistory) {
            redoStack.removeLast();
        }
        return set;
    }

    public synchronized ChangeSet popRedo() {
        if (redoStack.isEmpty()) return null;
        ChangeSet set = redoStack.pop();
        undoStack.push(set);
        if (undoStack.size() > maxHistory) {
            undoStack.removeLast();
        }
        return set;
    }

    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public synchronized int getUndoCount() {
        return undoStack.size();
    }

    public synchronized int getRedoCount() {
        return redoStack.size();
    }
}
