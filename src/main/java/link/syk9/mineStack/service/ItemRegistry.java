package link.syk9.mineStack.service;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
            if (material.name().endsWith("SHULKER_BOX")) {
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
        return !item.hasItemMeta();
    }
}
