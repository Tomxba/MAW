package fr.maw.schematic;

import fr.maw.clipboard.Clipboard;
import fr.maw.pattern.PatternParser;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the <b>Sponge schematic format</b> (versions 2 and 3), the {@code .schem} files WorldEdit and most
 * tools produce today — so a builder can drop a downloaded schematic in the folder and {@code //schem load}
 * it, not only the {@code .maw} files MAW saves itself.
 * <p>
 * The format is gzipped NBT: a block <b>palette</b> (block-state string → index) and a <b>varint-packed</b>
 * array of palette indices in {@code YZX} order, plus the width, height and length. In version 3 everything
 * lives under a {@code Schematic} compound and the blocks under {@code Blocks}; in version 2 the palette and
 * {@code BlockData} sit at the root. An index whose block this Minecraft version does not know becomes air
 * rather than failing the whole load. The legacy MCEdit {@code .schematic} format (numeric ids, pre-1.13)
 * has no palette and is refused with a clear message.
 */
public final class SpongeSchematic {

    private SpongeSchematic() {
    }

    public static Clipboard read(Path file) throws IOException {
        CompoundBinaryTag root = readNbt(file);
        // Version 3 wraps everything in a "Schematic" compound; version 2 keeps it at the root.
        CompoundBinaryTag wrapped = root.getCompound("Schematic");
        CompoundBinaryTag data = wrapped.keySet().isEmpty() ? root : wrapped;

        int width = number(data, "Width");
        int height = number(data, "Height");
        int length = number(data, "Length");
        if (width <= 0 || height <= 0 || length <= 0) {
            throw new IOException("Not a readable schematic (no size): " + file.getFileName());
        }

        CompoundBinaryTag palette;
        byte[] blockData;
        CompoundBinaryTag blocks = data.getCompound("Blocks");
        if (!blocks.keySet().isEmpty()) {
            // Version 3: Blocks { Palette, Data }
            palette = blocks.getCompound("Palette");
            blockData = blocks.getByteArray("Data");
        } else {
            // Version 2: Palette + BlockData at the root
            palette = data.getCompound("Palette");
            blockData = data.getByteArray("BlockData");
        }
        if (palette.keySet().isEmpty() || blockData.length == 0) {
            throw new IOException("Unsupported schematic (no block palette — a legacy MCEdit .schematic? "
                    + "re-export it as .schem from WorldEdit): " + file.getFileName());
        }

        Map<Integer, Block> byIndex = decodePalette(palette);
        Map<Point, Block> result = new HashMap<>();
        int area = width * length;
        int cursor = 0;
        int count = 0;
        while (cursor < blockData.length) {
            int value = 0;
            int shift = 0;
            while (true) {
                byte b = blockData[cursor++];
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    break;
                }
                shift += 7;
            }
            int y = count / area;
            int z = (count % area) / width;
            int x = count % width;
            Block block = byIndex.getOrDefault(value, Block.AIR);
            result.put(new Vec(x, y, z), block);
            count++;
        }

        // The blocks are kept relative to the min corner (0,0,0), so //paste puts that corner at the player.
        return new Clipboard(result, Vec.ZERO, width, height, length);
    }

    private static Map<Integer, Block> decodePalette(CompoundBinaryTag palette) {
        Map<Integer, Block> byIndex = new HashMap<>();
        for (String state : palette.keySet()) {
            byIndex.put(palette.getInt(state), parse(state));
        }
        return byIndex;
    }

    /** A palette entry to a block; an unknown block (another version, a mod) becomes air instead of failing. */
    private static Block parse(String state) {
        try {
            return PatternParser.parseBlock(state);
        } catch (RuntimeException unknownState) {
            int bracket = state.indexOf('[');
            if (bracket > 0) {
                try {
                    return PatternParser.parseBlock(state.substring(0, bracket)); // keep the block, drop the odd property
                } catch (RuntimeException unknownBlock) {
                    return Block.AIR;
                }
            }
            return Block.AIR;
        }
    }

    private static CompoundBinaryTag readNbt(Path file) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            return BinaryTagIO.unlimitedReader().read(in, BinaryTagIO.Compression.GZIP);
        } catch (IOException gzipFailed) {
            // Some tools write uncompressed NBT: try again without gzip before giving up.
            try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
                return BinaryTagIO.unlimitedReader().read(in, BinaryTagIO.Compression.NONE);
            }
        }
    }

    /** Width/Height/Length are shorts in the format; read a number whatever integer tag carries it. */
    private static int number(CompoundBinaryTag tag, String key) {
        BinaryTag value = tag.get(key);
        if (value instanceof ShortBinaryTag s) {
            return s.value();
        }
        if (value instanceof IntBinaryTag i) {
            return i.value();
        }
        if (value instanceof ByteBinaryTag b) {
            return b.value();
        }
        return 0;
    }
}
