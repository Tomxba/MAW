package fr.maw.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates all block changes applied in a single WorldEdit operation.
 */
public final class ChangeSet {

    private final List<BlockChange> changes;
    private final long timestamp;

    public ChangeSet(List<BlockChange> changes) {
        this.changes = Collections.unmodifiableList(new ArrayList<>(changes));
        this.timestamp = System.currentTimeMillis();
    }

    public List<BlockChange> getChanges() {
        return changes;
    }

    public int size() {
        return changes.size();
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Creates an inverse ChangeSet for Undo operations.
     */
    public ChangeSet inverse() {
        List<BlockChange> inverted = new ArrayList<>(changes.size());
        for (int i = changes.size() - 1; i >= 0; i--) {
            inverted.add(changes.get(i).inverse());
        }
        return new ChangeSet(inverted);
    }
}
