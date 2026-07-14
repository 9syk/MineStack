package link.syk9.mineStack.service;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemRegistry {
    private final List<Material> storableMaterials = new ArrayList<>();

    public void collect() {
        storableMaterials.clear();
        for (Material material : Material.values()) {
            if (!material.isItem() || material == Material.AIR) {
                continue;
            }
            storableMaterials.add(material);
        }
    }

    public List<Material> storableMaterials() {
        return storableMaterials;
    }

    public boolean isStorable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.getType().isItem()) {
            return false;
        }
        if (!storableMaterials.contains(item.getType())) {
            return false;
        }
        if (isShulkerBox(item.getType())) {
            return isEmptyPlainShulkerBox(item);
        }
        return !item.hasItemMeta();
    }

    private boolean isShulkerBox(Material material) {
        return material.name().endsWith("SHULKER_BOX");
    }

    private boolean isEmptyPlainShulkerBox(ItemStack item) {
        if (!item.hasItemMeta()) {
            return true;
        }
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) {
            return false;
        }
        if (meta.hasDisplayName() || meta.hasItemName() || meta.hasLore() || !meta.getEnchants().isEmpty()) {
            return false;
        }
        if (!(meta.getBlockState() instanceof ShulkerBox shulkerBox)) {
            return false;
        }
        return shulkerBox.getInventory().isEmpty();
    }
}
