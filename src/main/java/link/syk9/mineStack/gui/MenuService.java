package link.syk9.mineStack.gui;

import link.syk9.mineStack.config.CategoryConfig;
import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.model.AutoStoreMode;
import link.syk9.mineStack.model.Category;
import link.syk9.mineStack.model.PlayerStore;
import link.syk9.mineStack.model.SortMode;
import link.syk9.mineStack.model.View;
import link.syk9.mineStack.service.MineStackService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MenuService {
    private static final int INVENTORY_SIZE = 54;
    private static final int BACK_SLOT = 48;
    private static final int TOGGLE_SLOT = 49;
    private static final int SORT_SLOT = 50;
    private static final int NEXT_SLOT = 53;
    private static final int PREVIOUS_SLOT = 45;
    private static final int SHARED_SLOT = 8;
    private static final int RECENT_START = 18;
    private static final int RECENT_END = 35;

    private final PluginConfig config;
    private final CategoryConfig categoryConfig;
    private final MineStackService service;
    private final ItemSorter sorter = new ItemSorter();
    private final NamespacedKey actionKey;

    public MenuService(JavaPlugin plugin, PluginConfig config, CategoryConfig categoryConfig, MineStackService service) {
        this.config = config;
        this.categoryConfig = categoryConfig;
        this.service = service;
        this.actionKey = new NamespacedKey(plugin, "action");
    }

    public void openCategories(Player player) {
        PlayerStore store = service.store(player);
        Inventory inventory = Bukkit.createInventory(new MineStackHolder(View.CATEGORIES, null, 0), INVENTORY_SIZE, "MineStack");

        int slot = 0;
        for (Category category : Category.values()) {
            inventory.setItem(slot++, categoryIcon(category));
        }
        if (config.enableSharedStorage()) {
            inventory.setItem(SHARED_SLOT, sharedIcon());
        }

        int recentSlot = RECENT_START;
        for (Material material : store.recent()) {
            if (recentSlot > RECENT_END) {
                break;
            }
            inventory.setItem(recentSlot++, storageIcon(material, store.count(material)));
        }

        inventory.setItem(TOGGLE_SLOT, toggleIcon(store.autoStoreMode()));
        player.openInventory(inventory);
    }

    public void openItems(Player player, Category category, int page) {
        PlayerStore store = service.store(player);
        List<Material> materials = categoryConfig.materials(category).stream()
                .filter(material -> service.itemRegistry().storableMaterials().contains(material))
                .toList();
        materials = sorter.sort(materials, store, store.sortMode(), material -> categoryConfig.order(category, material));

        int maxPage = Math.max(0, (materials.size() - 1) / 45);
        int safePage = Math.min(Math.max(0, page), maxPage);
        Inventory inventory = Bukkit.createInventory(new MineStackHolder(View.ITEMS, category, safePage), INVENTORY_SIZE,
                "MineStack - " + category.displayName());

        int start = safePage * 45;
        int end = Math.min(start + 45, materials.size());
        for (int i = start; i < end; i++) {
            Material material = materials.get(i);
            inventory.setItem(i - start, storageIcon(material, store.count(material)));
        }

        addNavigation(inventory, safePage, maxPage);
        inventory.setItem(TOGGLE_SLOT, toggleIcon(store.autoStoreMode()));
        inventory.setItem(SORT_SLOT, sortIcon(store.sortMode()));
        player.openInventory(inventory);
    }

    public void openShared(Player player, int page) {
        PlayerStore playerStore = service.store(player);
        PlayerStore sharedStore = service.sharedStore();
        List<Material> materials = service.itemRegistry().storableMaterials().stream()
                .filter(material -> sharedStore.count(material).signum() > 0)
                .toList();
        materials = sorter.sort(materials, sharedStore, playerStore.sortMode(), categoryConfig::firstOrder);

        int maxPage = Math.max(0, (materials.size() - 1) / 45);
        int safePage = Math.min(Math.max(0, page), maxPage);
        Inventory inventory = Bukkit.createInventory(new MineStackHolder(View.SHARED, null, safePage), INVENTORY_SIZE,
                "MineStack - 共有ストレージ");

        int start = safePage * 45;
        int end = Math.min(start + 45, materials.size());
        for (int i = start; i < end; i++) {
            Material material = materials.get(i);
            inventory.setItem(i - start, storageIcon(material, sharedStore.count(material)));
        }

        addNavigation(inventory, safePage, maxPage);
        inventory.setItem(TOGGLE_SLOT, toggleIcon(playerStore.autoStoreMode()));
        inventory.setItem(SORT_SLOT, sortIcon(playerStore.sortMode()));
        player.openInventory(inventory);
    }

    public void refreshSharedViewers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MineStackHolder holder
                    && holder.view() == View.SHARED) {
                openShared(player, holder.page());
            }
        }
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MineStackHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (holder.view() == View.CATEGORIES) {
            handleCategoryClick(player, clicked, event.getSlot());
        } else {
            handleStorageClick(player, holder, clicked, event.getSlot(), event.isRightClick());
        }
    }

    private void handleCategoryClick(Player player, ItemStack clicked, int slot) {
        PlayerStore store = service.store(player);
        String action = action(clicked);
        if ("toggle".equals(action)) {
            service.toggleAutoStore(player);
            openCategories(player);
            return;
        }
        if ("shared".equals(action) && config.enableSharedStorage()) {
            openShared(player, 0);
            return;
        }
        if (slot >= RECENT_START && slot <= RECENT_END) {
            Material material = clicked.getType();
            if (service.withdraw(player, store, material, clicked.getMaxStackSize())) {
                service.persistPlayer(player);
            }
            openCategories(player);
            return;
        }
        Category category = category(clicked);
        if (category != null) {
            openItems(player, category, 0);
        }
    }

    private void handleStorageClick(Player player, MineStackHolder holder, ItemStack clicked, int slot, boolean rightClick) {
        String action = action(clicked);
        if ("toggle".equals(action)) {
            service.toggleAutoStore(player);
            reopen(player, holder);
            return;
        }
        if ("sort".equals(action)) {
            service.cycleSort(player);
            if (holder.view() == View.SHARED) {
                openShared(player, 0);
            } else {
                openItems(player, holder.category(), 0);
            }
            return;
        }
        if ("back".equals(action)) {
            openCategories(player);
            return;
        }
        if ("previous".equals(action)) {
            reopen(player, holder.withPage(Math.max(0, holder.page() - 1)));
            return;
        }
        if ("next".equals(action)) {
            reopen(player, holder.withPage(holder.page() + 1));
            return;
        }
        if (slot >= 0 && slot < 45) {
            PlayerStore store = holder.view() == View.SHARED ? service.sharedStore() : service.store(player);
            int amount = rightClick ? 1 : clicked.getMaxStackSize();
            boolean changed = service.withdraw(player, store, clicked.getType(), amount);
            if (holder.view() == View.SHARED) {
                if (changed) {
                    service.persistShared();
                    refreshSharedViewers();
                }
            } else {
                if (changed) {
                    service.persistPlayer(player);
                }
                openItems(player, holder.category(), holder.page());
            }
        }
    }

    private void reopen(Player player, MineStackHolder holder) {
        if (holder.view() == View.SHARED) {
            openShared(player, holder.page());
        } else {
            openItems(player, holder.category(), holder.page());
        }
    }

    private void addNavigation(Inventory inventory, int page, int maxPage) {
        inventory.setItem(BACK_SLOT, actionIcon(Material.ARROW, "戻る", "back"));
        if (page > 0) {
            inventory.setItem(PREVIOUS_SLOT, actionIcon(Material.SPECTRAL_ARROW, "前のページ", "previous"));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_SLOT, actionIcon(Material.SPECTRAL_ARROW, "次のページ", "next"));
        }
    }

    private ItemStack categoryIcon(Category category) {
        ItemStack item = new ItemStack(category.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(name(category.displayName()));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "category:" + category.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack storageIcon(Material material, BigInteger count) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore(
                    "所持数: " + compact(count),
                    "左クリック: 1スタック取り出す",
                    "右クリック: 1個取り出す"
            ));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack sharedIcon() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(name("共有ストレージ"));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "shared");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack toggleIcon(AutoStoreMode mode) {
        ItemStack item = switch (mode) {
            case PERSONAL -> new ItemStack(Material.LIME_DYE);
            case SHARED -> new ItemStack(Material.ENDER_EYE);
            case OFF -> new ItemStack(Material.GRAY_DYE);
        };
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(name("自動収納: " + mode.displayName()));
            meta.setLore(lore("クリックで切り替え"));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "toggle");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack sortIcon(SortMode sortMode) {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(name("ソート: " + sortMode.displayName()));
            meta.setLore(lore("クリックで切り替え"));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "sort");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack actionIcon(Material material, String name, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(name(name));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String action(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return "";
        }
        return meta.getPersistentDataContainer().getOrDefault(actionKey, PersistentDataType.STRING, "");
    }

    private Category category(ItemStack item) {
        String action = action(item);
        if (!action.startsWith("category:")) {
            return null;
        }
        try {
            return Category.valueOf(action.substring("category:".length()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String name(String text) {
        return ChatColor.YELLOW + text;
    }

    private List<String> lore(String... lines) {
        List<String> output = new ArrayList<>(lines.length);
        for (String line : lines) {
            output.add(ChatColor.RESET.toString() + ChatColor.WHITE + line);
        }
        return output;
    }

    private String compact(BigInteger count) {
        if (count.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return NumberFormat.getIntegerInstance(Locale.US).format(count.longValue());
        }
        return count.toString();
    }
}
