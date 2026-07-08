package link.syk9.mineStack.command;

import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.gui.MenuService;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MineStackCommand implements TabExecutor {
    private final PluginConfig config;
    private final MenuService menuService;

    public MineStackCommand(PluginConfig config, MenuService menuService) {
        this.config = config;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("MineStack: プレイヤーのみ実行できます。");
            return true;
        }
        if (args.length == 0) {
            if (!config.enableMineStackCommand()) {
                player.sendMessage("MineStack: /minestack は無効です。");
                return true;
            }
            menuService.openCategories(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("stick")) {
            if (!config.enableStickCommand()) {
                player.sendMessage("MineStack: /minestack stick は無効です。");
                return true;
            }
            player.getInventory().addItem(new ItemStack(Material.STICK));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && "stick".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("stick");
        }
        return Collections.emptyList();
    }
}
