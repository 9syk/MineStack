package link.syk9.mineStack.model;

import org.bukkit.Material;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerStore {
    private AutoStoreMode autoStoreMode = AutoStoreMode.PERSONAL;
    private SortMode sortMode = SortMode.DEFAULT;
    private final Map<Material, BigInteger> items = new EnumMap<>(Material.class);
    private final LinkedHashSet<Material> recent = new LinkedHashSet<>();

    public AutoStoreMode autoStoreMode() {
        return autoStoreMode;
    }

    public void setAutoStoreMode(AutoStoreMode autoStoreMode) {
        this.autoStoreMode = autoStoreMode;
    }

    public SortMode sortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public Map<Material, BigInteger> items() {
        return items;
    }

    public Set<Material> recent() {
        return recent;
    }

    public void add(Material material, int amount) {
        items.merge(material, BigInteger.valueOf(amount), BigInteger::add);
    }

    public void put(Material material, BigInteger amount) {
        if (amount.signum() > 0) {
            items.put(material, amount);
        }
    }

    public void remove(Material material, int amount) {
        BigInteger next = count(material).subtract(BigInteger.valueOf(amount));
        if (next.signum() <= 0) {
            items.remove(material);
        } else {
            items.put(material, next);
        }
    }

    public BigInteger count(Material material) {
        return items.getOrDefault(material, BigInteger.ZERO);
    }

    public void markRecent(Material material) {
        Set<Material> copy = new LinkedHashSet<>(recent);
        recent.clear();
        recent.add(material);
        for (Material existing : copy) {
            if (recent.size() >= 16) {
                break;
            }
            if (existing != material) {
                recent.add(existing);
            }
        }
    }
}
