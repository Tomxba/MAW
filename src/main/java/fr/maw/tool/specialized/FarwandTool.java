package fr.maw.tool.specialized;

import fr.maw.command.CommandHelper;
import fr.maw.session.PlayerSession;
import fr.maw.session.SessionManager;
import fr.maw.tool.HandClickType;
import fr.maw.tool.Tool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;

public class FarwandTool implements Tool {

    private final SessionManager sessionManager;

    public FarwandTool(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean execute(Player player, Instance instance, Point targetBlock, BlockFace face, HandClickType clickType) {
        if (targetBlock == null) {
            targetBlock = CommandHelper.getTargetBlock(player, 200);
        }
        if (targetBlock == null) {
            CommandHelper.sendError(player, "No block in sight within 200 blocks.");
            return true;
        }

        PlayerSession session = sessionManager.getSession(player);
        if (clickType == HandClickType.LEFT_CLICK) {
            session.setPos1(targetBlock);
            String volumeInfo = session.isSelectionComplete()
                    ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                    : "";
            player.sendMessage(Component.text(String.format("First position set to (%d, %d, %d)%s",
                    targetBlock.blockX(), targetBlock.blockY(), targetBlock.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
        } else {
            session.setPos2(targetBlock);
            String volumeInfo = session.isSelectionComplete()
                    ? String.format(" (%,d blocks)", session.getSelection().getVolume())
                    : "";
            player.sendMessage(Component.text(String.format("Second position set to (%d, %d, %d)%s",
                    targetBlock.blockX(), targetBlock.blockY(), targetBlock.blockZ(), volumeInfo), NamedTextColor.LIGHT_PURPLE));
        }

        return true;
    }
}
