package link.syk9.mineStack.listener;

import link.syk9.mineStack.gui.MenuService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class MenuListener implements Listener {
    private final MenuService menuService;

    public MenuListener(MenuService menuService) {
        this.menuService = menuService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        menuService.handleClick(event);
    }
}
