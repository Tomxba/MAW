package fr.maw;

import fr.maw.clipboard.Clipboard;
import fr.maw.schematic.SchematicManager;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reading the Sponge .schem format (WorldEdit's), so downloaded schematics load, not only MAW's own .maw. */
@DisplayName("Sponge schematic reading")
public class SpongeSchematicTest {

    /** Writes a minimal gzipped Sponge v2 schematic: a 2×2×1 box with stone at two corners, air elsewhere. */
    private static Path writeSpongeV2(Path dir, String name) throws Exception {
        CompoundBinaryTag palette = CompoundBinaryTag.builder()
                .putInt("minecraft:air", 0)
                .putInt("minecraft:stone", 1)
                .build();
        // YZX order over width=2, length=1, height=2: (0,0,0)=stone, (1,0,0)=air, (0,1,0)=air, (1,1,0)=stone
        byte[] blockData = {1, 0, 0, 1};
        CompoundBinaryTag root = CompoundBinaryTag.builder()
                .putInt("Version", 2)
                .putShort("Width", (short) 2)
                .putShort("Height", (short) 2)
                .putShort("Length", (short) 1)
                .put("Palette", palette)
                .putByteArray("BlockData", blockData)
                .build();
        Path file = dir.resolve(name);
        try (OutputStream out = Files.newOutputStream(file)) {
            BinaryTagIO.writer().write(root, out, BinaryTagIO.Compression.GZIP);
        }
        return file;
    }

    @Test
    @DisplayName("A .schem is listed, loaded and its blocks placed in YZX order")
    public void should_read_a_sponge_schem(@TempDir Path dir) throws Exception {
        writeSpongeV2(dir, "house.schem");
        SchematicManager manager = new SchematicManager(dir);

        assertTrue(manager.list().contains("house"), "the .schem is listed without its extension");

        Clipboard clipboard = manager.load("house");
        assertEquals(2, clipboard.getWidth());
        assertEquals(2, clipboard.getHeight());
        assertEquals(1, clipboard.getLength());
        assertEquals(4, clipboard.getBlockCount(), "every cell of the box, air included");
        assertEquals(Block.STONE, clipboard.getBlocks().get(new Vec(0, 0, 0)));
        assertEquals(Block.STONE, clipboard.getBlocks().get(new Vec(1, 1, 0)));
        assertEquals(Block.AIR, clipboard.getBlocks().get(new Vec(1, 0, 0)));
    }

    @Test
    @DisplayName("A version-3 .schem (blocks under Schematic/Blocks) is read too")
    public void should_read_a_sponge_v3(@TempDir Path dir) throws Exception {
        CompoundBinaryTag palette = CompoundBinaryTag.builder()
                .putInt("minecraft:air", 0)
                .putInt("minecraft:oak_log", 1)
                .build();
        CompoundBinaryTag blocks = CompoundBinaryTag.builder()
                .put("Palette", palette)
                .putByteArray("Data", new byte[]{1, 1, 0, 0}) // oak_log at (0,0,0) and (1,0,0)
                .build();
        CompoundBinaryTag schematic = CompoundBinaryTag.builder()
                .putInt("Version", 3)
                .putShort("Width", (short) 2).putShort("Height", (short) 2).putShort("Length", (short) 1)
                .put("Blocks", blocks)
                .build();
        CompoundBinaryTag root = CompoundBinaryTag.builder().put("Schematic", schematic).build();
        Path file = dir.resolve("tree.schem");
        try (OutputStream out = Files.newOutputStream(file)) {
            BinaryTagIO.writer().write(root, out, BinaryTagIO.Compression.GZIP);
        }

        Clipboard clipboard = new SchematicManager(dir).load("tree");
        assertEquals(4, clipboard.getBlockCount());
        assertEquals(Block.OAK_LOG, clipboard.getBlocks().get(new Vec(0, 0, 0)));
        assertEquals(Block.OAK_LOG, clipboard.getBlocks().get(new Vec(1, 0, 0)));
    }

    @Test
    @DisplayName("A legacy MCEdit .schematic (no palette) is refused with a clear message")
    public void should_refuse_legacy_mcedit(@TempDir Path dir) throws Exception {
        CompoundBinaryTag legacy = CompoundBinaryTag.builder()
                .putShort("Width", (short) 1).putShort("Height", (short) 1).putShort("Length", (short) 1)
                .putByteArray("Blocks", new byte[]{1})
                .putByteArray("Data", new byte[]{0})
                .build();
        Path file = dir.resolve("old.schematic");
        try (OutputStream out = Files.newOutputStream(file)) {
            BinaryTagIO.writer().write(legacy, out, BinaryTagIO.Compression.GZIP);
        }
        SchematicManager manager = new SchematicManager(dir);

        Exception error = assertThrows(Exception.class, () -> manager.load("old"));
        assertTrue(error.getMessage().toLowerCase().contains("palette") || error.getMessage().toLowerCase().contains("mcedit"),
                "the message explains the legacy format: " + error.getMessage());
    }
}
