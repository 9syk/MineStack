package link.syk9.mineStack.config;

import link.syk9.mineStack.model.Category;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CategoryConfig {
    private final JavaPlugin plugin;
    private final Map<Category, List<Material>> materialsByCategory = new EnumMap<>(Category.class);
    private final Map<Category, Map<Material, Integer>> orderByCategory = new EnumMap<>(Category.class);
    private final Map<Material, Integer> firstOrder = new EnumMap<>(Material.class);

    public CategoryConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(List<Material> storableMaterials) {
        File file = new File(plugin.getDataFolder(), "categories.yml");
        if (!file.isFile()) {
            plugin.saveResource("categories.yml", false);
        }

        materialsByCategory.clear();
        orderByCategory.clear();
        firstOrder.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int globalIndex = 0;
        for (Category category : Category.values()) {
            List<Material> materials = new ArrayList<>();
            Map<Material, Integer> order = new EnumMap<>(Material.class);
            int index = 0;
            for (String materialName : config.getStringList(category.name())) {
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning("Unknown MineStack category item in categories.yml: " + materialName);
                    continue;
                }
                materials.add(material);
                order.putIfAbsent(material, index++);
                firstOrder.putIfAbsent(material, globalIndex++);
            }
            materialsByCategory.put(category, List.copyOf(materials));
            orderByCategory.put(category, order);
        }

        logUncategorized(storableMaterials);
    }

    public List<Material> materials(Category category) {
        return materialsByCategory.getOrDefault(category, List.of());
    }

    public int order(Category category, Material material) {
        return orderByCategory.getOrDefault(category, Map.of()).getOrDefault(material, Integer.MAX_VALUE);
    }

    public int firstOrder(Material material) {
        return firstOrder.getOrDefault(material, Integer.MAX_VALUE);
    }

    private void logUncategorized(List<Material> storableMaterials) {
        Set<Material> categorized = new HashSet<>();
        for (List<Material> materials : materialsByCategory.values()) {
            categorized.addAll(materials);
        }

        List<String> missing = storableMaterials.stream()
                .filter(material -> !categorized.contains(material))
                .map(Enum::name)
                .sorted()
                .toList();
        if (missing.isEmpty()) {
            return;
        }

        plugin.getLogger().warning("MineStack categories.yml has uncategorized storable items: " + String.join(", ", missing));
    }
}
