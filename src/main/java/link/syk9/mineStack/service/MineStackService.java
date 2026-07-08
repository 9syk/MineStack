package link.syk9.mineStack.service;

import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.model.AutoStoreMode;
import link.syk9.mineStack.model.PlayerStore;
import link.syk9.mineStack.model.StoreResult;
import link.syk9.mineStack.storage.StorageRepository;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MineStackService {
    private final PluginConfig config;
    private final StorageRepository storage;
    private final ItemRegistry itemRegistry;
    private final Map<UUID, PlayerStore> stores = new HashMap<>();
    private final PlayerStore sharedStore = new PlayerStore();

    public MineStackService(PluginConfig config, StorageRepository storage, ItemRegistry itemRegistry) {
        this.config = config;
        this.storage = storage;
        this.itemRegistry = itemRegistry;
    }

    public void load() {
        storage.load(stores, sharedStore);
    }

    public void saveAll() {
        storage.saveAll(stores, sharedStore);
    }

    public PlayerStore store(Player player) {
        return stores.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStore());
    }

    public PlayerStore sharedStore() {
        return sharedStore;
    }

    public ItemRegistry itemRegistry() {
        return itemRegistry;
    }

    public StoreResult storePickup(Player player, ItemStack item) {
        PlayerStore target = autoStoreTarget(store(player));
        if (target == null || !itemRegistry.isStorable(item)) {
            return StoreResult.NONE;
        }

        target.add(item.getType(), item.getAmount());
        if (target == sharedStore) {
            storage.saveShared(sharedStore);
            return StoreResult.SHARED;
        } else {
            storage.savePlayer(player.getUniqueId(), target);
            return StoreResult.PERSONAL;
        }
    }

    public boolean withdraw(Player player, PlayerStore store, Material material, int requested) {
        BigInteger available = store.count(material);
        if (available.signum() <= 0) {
            return false;
        }

        int amount = available.min(BigInteger.valueOf(requested)).intValue();
        ItemStack item = new ItemStack(material, amount);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        int added = amount - leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        if (added <= 0) {
            player.sendMessage("MineStack: インベントリに空きがありません。");
            return false;
        }

        store.remove(material, added);
        store.markRecent(material);
        return true;
    }

    public void persistPlayer(Player player) {
        storage.savePlayer(player.getUniqueId(), store(player));
    }

    public void persistShared() {
        storage.saveShared(sharedStore);
    }

    public void toggleAutoStore(Player player) {
        PlayerStore store = store(player);
        store.setAutoStoreMode(store.autoStoreMode().next(config.enableSharedStorage()));
        persistPlayer(player);
    }

    public void cycleSort(Player player) {
        PlayerStore store = store(player);
        store.setSortMode(store.sortMode().next());
        persistPlayer(player);
    }

    private PlayerStore autoStoreTarget(PlayerStore playerStore) {
        if (playerStore.autoStoreMode() == AutoStoreMode.SHARED && config.enableSharedStorage()) {
            return sharedStore;
        }
        if (playerStore.autoStoreMode() == AutoStoreMode.PERSONAL) {
            return playerStore;
        }
        return null;
    }
}
