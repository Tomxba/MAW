package fr.maw.tool.specialized;

import fr.maw.command.CommandHelper;
import fr.maw.tool.HandClickType;
import fr.maw.tool.Tool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public class InfoTool implements Tool {

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) {
            return false;
        }

        Block block = instance.getBlock(targetBlock.blockX(), targetBlock.blockY(), targetBlock.blockZ());
        String name = block.name();
        var properties = block.properties();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Block at (%d, %d, %d): %s",
                targetBlock.blockX(), targetBlock.blockY(), targetBlock.blockZ(), name));

        if (!properties.isEmpty()) {
            sb.append(" [");
            boolean first = true;
            for (var entry : properties.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("]");
        }

        player.sendMessage(Component.text("ℹ " + sb.toString(), NamedTextColor.AQUA));
        return true;
    }
}
