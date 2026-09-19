package fr.maw.command;

import fr.maw.MawConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class WandCommand extends Command {

    public WandCommand(MawConfig config) {
        super("//wand", "/wand", "wand");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                CommandHelper.sendError(sender, "Only players can use the wand command.");
                return;
            }

            Material mat = Material.fromKey(config.wandItemNamespace());
            if (mat == null) mat = Material.WOODEN_AXE;

            ItemStack wand = ItemStack.of(mat);

            player.getInventory().addItemStack(wand);
            CommandHelper.sendSuccess(player, "Left click: select pos #1; Right click: select pos #2");
        });
    }
}
