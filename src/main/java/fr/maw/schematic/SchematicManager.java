package fr.maw.schematic;

import fr.maw.clipboard.Clipboard;
import fr.maw.pattern.PatternParser;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * High-performance schematic manager for saving and loading clipboards from disk.
 */
public final class SchematicManager {

    private final Path directory;

    public SchematicManager(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory cannot be null");
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            // Ignore
        }
    }

    public static SchematicManager defaultManager() {
        return new SchematicManager(Paths.get("schematics"));
    }

    public void save(Clipboard clipboard, String name) throws IOException {
        Objects.requireNonNull(clipboard, "clipboard cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        String safeName = sanitizeName(name);
        Path file = directory.resolve(safeName + ".maw");

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            Point origin = clipboard.getOrigin();
            writer.write(String.format("MAW-SCHEM;1;%d;%d;%d;%d;%d;%d\n",
                    origin.blockX(), origin.blockY(), origin.blockZ(),
                    clipboard.getWidth(), clipboard.getHeight(), clipboard.getLength()
            ));

            for (var entry : clipboard.getBlocks().entrySet()) {
                Point p = entry.getKey();
                Block b = entry.getValue();

                StringBuilder blockStr = new StringBuilder(b.name());
                var props = b.properties();
                if (!props.isEmpty()) {
                    blockStr.append("[");
                    boolean first = true;
                    for (var prop : props.entrySet()) {
                        if (!first) blockStr.append(",");
                        blockStr.append(prop.getKey()).append("=").append(prop.getValue());
                        first = false;
                    }
                    blockStr.append("]");
                }

                writer.write(String.format("%d;%d;%d;%s\n", p.blockX(), p.blockY(), p.blockZ(), blockStr));
            }
        }
    }

    public Clipboard load(String name) throws IOException {
        String safeName = sanitizeName(name);
        Path file = directory.resolve(safeName + ".maw");

        if (!Files.exists(file)) {
            throw new FileNotFoundException("Schematic not found: " + safeName);
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || !header.startsWith("MAW-SCHEM;1;")) {
                throw new IOException("Invalid or unsupported schematic format in: " + file);
            }

            String[] headerParts = header.split(";");
            int ox = Integer.parseInt(headerParts[2]);
            int oy = Integer.parseInt(headerParts[3]);
            int oz = Integer.parseInt(headerParts[4]);
            int w = Integer.parseInt(headerParts[5]);
            int h = Integer.parseInt(headerParts[6]);
            int l = Integer.parseInt(headerParts[7]);

            Map<Point, Block> blocks = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(";", 4);
                if (parts.length < 4) continue;

                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                String blockName = parts[3];

                Block block = PatternParser.parseBlock(blockName);
                blocks.put(new Vec(x, y, z), block);
            }

            return new Clipboard(blocks, new Vec(ox, oy, oz), w, h, l);
        }
    }

    public List<String> list() {
        try {
            if (!Files.exists(directory)) return Collections.emptyList();
            try (var stream = Files.list(directory)) {
                return stream
                        .filter(p -> p.getFileName().toString().endsWith(".maw"))
                        .map(p -> {
                            String fName = p.getFileName().toString();
                            return fName.substring(0, fName.length() - 4);
                        })
                        .sorted()
                        .toList();
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public boolean delete(String name) {
        String safeName = sanitizeName(name);
        Path file = directory.resolve(safeName + ".maw");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            return false;
        }
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
