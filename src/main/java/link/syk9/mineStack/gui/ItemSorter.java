package link.syk9.mineStack.gui;

import link.syk9.mineStack.model.PlayerStore;
import link.syk9.mineStack.model.SortMode;
import org.bukkit.Material;
import org.bukkit.inventory.CreativeCategory;

import java.util.Comparator;
import java.util.List;

public final class ItemSorter {
    public List<Material> sort(List<Material> materials, PlayerStore countStore, SortMode sortMode) {
        return switch (sortMode) {
            case DEFAULT -> materials.stream()
                    .sorted(Comparator.comparingInt(this::creativeOrder).thenComparingInt(Enum::ordinal))
                    .toList();
            case COUNT -> materials.stream()
                    .sorted(Comparator.comparing((Material material) -> countStore.count(material)).reversed()
                            .thenComparing(Enum::name))
                    .toList();
            case ID_ASC -> materials.stream()
                    .sorted(Comparator.comparing(Enum::name))
                    .toList();
            case ID_DESC -> materials.stream()
                    .sorted(Comparator.comparing(Enum::name, Comparator.reverseOrder()))
                    .toList();
        };
    }

    private int creativeOrder(Material material) {
        CreativeCategory category = material.getCreativeCategory();
        if (category == null) {
            return Integer.MAX_VALUE;
        }
        return switch (category) {
            case BUILDING_BLOCKS -> 0;
            case DECORATIONS -> 1;
            case REDSTONE -> 2;
            case TRANSPORTATION -> 3;
            case MISC -> 4;
            case FOOD -> 5;
            case TOOLS -> 6;
            case COMBAT -> 7;
            case BREWING -> 8;
        };
    }
}
