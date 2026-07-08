package link.syk9.mineStack.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {
    private final JavaPlugin plugin;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean enableStickCommand() {
        return plugin.getConfig().getBoolean("enable-stick-command", true);
    }

    public boolean enableMineStackCommand() {
        return plugin.getConfig().getBoolean("enable-minestack-command", true);
    }

    public boolean enableSharedStorage() {
        return plugin.getConfig().getBoolean("enable-shared-storage", true);
    }

    public int backupIntervalMinutes() {
        return Math.max(1, plugin.getConfig().getInt("backup-interval-minutes", 30));
    }

    public int backupRetention() {
        return Math.max(1, plugin.getConfig().getInt("backup-retention", 48));
    }
}
