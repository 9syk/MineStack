package link.syk9.mineStack.command;

import link.syk9.mineStack.config.CategoryConfig;
import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.gui.MenuService;
import link.syk9.mineStack.service.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MineStackCommand implements TabExecutor {
    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final CategoryConfig categoryConfig;
    private final ItemRegistry itemRegistry;
    private final MenuService menuService;

    public MineStackCommand(JavaPlugin plugin, PluginConfig config, CategoryConfig categoryConfig, ItemRegistry itemRegistry, MenuService menuService) {
        this.plugin = plugin;
        this.config = config;
        this.categoryConfig = categoryConfig;
        this.itemRegistry = itemRegistry;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("MineStack: プレイヤーのみ実行できます。");
                return true;
            }
            if (!config.enableMineStackCommand()) {
                player.sendMessage("MineStack: /minestack は無効です。");
                return true;
            }
            menuService.openCategories(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("stick")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("MineStack: プレイヤーのみ実行できます。");
                return true;
            }
            if (!config.enableStickCommand()) {
                player.sendMessage("MineStack: /minestack stick は無効です。");
                return true;
            }
            player.getInventory().addItem(new ItemStack(Material.STICK));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) {
                sender.sendMessage("MineStack: このコマンドを実行する権限がありません。");
                return true;
            }
            plugin.reloadConfig();
            itemRegistry.collect();
            categoryConfig.reload(itemRegistry.storableMaterials());
            sender.sendMessage("MineStack: config.yml と categories.yml を再読み込みしました。");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return List.of("stick", "reload").stream()
                    .filter(value -> value.startsWith(input))
                    .toList();
        }
        return Collections.emptyList();
    }
}
