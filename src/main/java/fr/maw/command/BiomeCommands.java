package fr.maw.command;

import fr.maw.selection.Selection;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BiomeCommands {

    private BiomeCommands() {}

    public static List<Command> create(SessionManager sessionManager) {
        List<Command> commands = new ArrayList<>();

        // 1. //setbiome <biome>
        Command setbiome = new Command("//setbiome", "/setbiome", "setbiome");
        setbiome.setDefaultExecutor((sender, context) -> CommandHelper.sendError(sender, "Usage: //setbiome <biome>"));
        var biomeArg = ArgumentType.String("biome");
        setbiome.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            PlayerSession session = sessionManager.getSession(player);
            if (!session.isSelectionComplete()) {
                CommandHelper.sendError(player, "Make a selection first.");
                return;
            }

            String raw = context.get(biomeArg).toLowerCase(Locale.ROOT);
            if (!raw.contains(":")) raw = "minecraft:" + raw;

            net.minestom.server.registry.RegistryKey<Biome> targetBiome = net.minestom.server.registry.RegistryKey.unsafeOf(raw);

            Selection sel = session.getSelection();
            long count = 0;
            // Biomes in Minecraft are sampled at 4x4 intervals, but setBiome sets the coordinate
            for (int x = sel.getMinX(); x <= sel.getMaxX(); x += 4) {
                for (int z = sel.getMinZ(); z <= sel.getMaxZ(); z += 4) {
                    for (int y = sel.getMinY(); y <= sel.getMaxY(); y += 4) {
                        instance.setBiome(x, y, z, targetBiome);
                        count++;
                    }
                }
            }

            CommandHelper.sendSuccess(player, String.format("Set biome to %s across %,d biome points.", raw, count));
        }, biomeArg);
        commands.add(setbiome);

        // 2. //biomeinfo
        Command biomeinfo = new Command("//biomeinfo", "/biomeinfo", "biomeinfo");
        biomeinfo.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();
            if (instance == null) return;

            Point pos = player.getPosition();
            net.minestom.server.registry.RegistryKey<Biome> b = instance.getBiome(pos.blockX(), pos.blockY(), pos.blockZ());
            String name = b != null ? b.name() : "unknown";

            player.sendMessage(Component.text(String.format("Biome at (%d, %d, %d): %s",
                    pos.blockX(), pos.blockY(), pos.blockZ(), name), NamedTextColor.AQUA));
        });
        commands.add(biomeinfo);

        return commands;
    }
}
