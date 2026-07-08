package link.syk9.mineStack.listener;

import link.syk9.mineStack.gui.MenuService;
import link.syk9.mineStack.model.StoreResult;
import link.syk9.mineStack.service.MineStackService;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerListener implements Listener {
    private final MineStackService service;
    private final MenuService menuService;

    public PlayerListener(MineStackService service, MenuService menuService) {
        this.service = service;
        this.menuService = menuService;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        ItemStack item = itemEntity.getItemStack();
        StoreResult result = service.storePickup(player, item);
        if (result == StoreResult.NONE) {
            return;
        }

        event.setCancelled(true);
        itemEntity.remove();
        if (result == StoreResult.SHARED) {
            menuService.refreshSharedViewers();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        event.setCancelled(true);
        menuService.openCategories(event.getPlayer());
    }
}
