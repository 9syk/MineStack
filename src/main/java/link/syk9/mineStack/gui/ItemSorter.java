package link.syk9.mineStack.gui;

import link.syk9.mineStack.model.PlayerStore;
import link.syk9.mineStack.model.SortMode;
import org.bukkit.Material;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public final class ItemSorter {
    public List<Material> sort(List<Material> materials, PlayerStore countStore, SortMode sortMode, ToIntFunction<Material> defaultOrder) {
        return switch (sortMode) {
            case DEFAULT -> materials.stream()
                    .sorted(Comparator.comparingInt(defaultOrder).thenComparingInt(Enum::ordinal))
                    .toList();
            case COUNT -> materials.stream()
                    .sorted(Comparator.comparing((Material material) -> countStore.count(material)).reversed()
                            .thenComparingInt(defaultOrder)
                            .thenComparingInt(Enum::ordinal))
                    .toList();
            case ID_ASC -> materials.stream()
                    .sorted(Comparator.comparing(Enum::name))
                    .toList();
            case ID_DESC -> materials.stream()
                    .sorted(Comparator.comparing(Enum::name, Comparator.reverseOrder()))
                    .toList();
        };
    }
}
