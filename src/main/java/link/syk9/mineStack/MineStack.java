package link.syk9.mineStack;

import link.syk9.mineStack.command.MineStackCommand;
import link.syk9.mineStack.config.CategoryConfig;
import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.gui.MenuService;
import link.syk9.mineStack.listener.MenuListener;
import link.syk9.mineStack.listener.PlayerListener;
import link.syk9.mineStack.service.ItemRegistry;
import link.syk9.mineStack.service.MineStackService;
import link.syk9.mineStack.storage.StorageRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineStack extends JavaPlugin {
    private StorageRepository storageRepository;
    private MineStackService mineStackService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginConfig pluginConfig = new PluginConfig(this);
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.collect();
        CategoryConfig categoryConfig = new CategoryConfig(this);
        categoryConfig.reload(itemRegistry.storableMaterials());

        storageRepository = new StorageRepository(this, pluginConfig);
        storageRepository.init();

        mineStackService = new MineStackService(pluginConfig, storageRepository, itemRegistry);
        mineStackService.load();

        MenuService menuService = new MenuService(this, pluginConfig, categoryConfig, mineStackService);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(mineStackService, menuService), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(menuService), this);

        MineStackCommand commandExecutor = new MineStackCommand(this, pluginConfig, categoryConfig, itemRegistry, menuService);
        PluginCommand command = getCommand("minestack");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        scheduleBackups(pluginConfig);
    }

    @Override
    public void onDisable() {
        if (mineStackService != null) {
            mineStackService.saveAll();
        }
        if (storageRepository != null) {
            storageRepository.close();
        }
    }

    private void scheduleBackups(PluginConfig config) {
        long ticks = config.backupIntervalMinutes() * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            mineStackService.saveAll();
            storageRepository.createBackup();
        }, ticks, ticks);
    }
}
