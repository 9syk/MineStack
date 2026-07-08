package link.syk9.mineStack.gui;

import link.syk9.mineStack.model.Category;
import link.syk9.mineStack.model.View;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MineStackHolder implements InventoryHolder {
    private final View view;
    private final Category category;
    private final int page;

    public MineStackHolder(View view, Category category, int page) {
        this.view = view;
        this.category = category;
        this.page = page;
    }

    public View view() {
        return view;
    }

    public Category category() {
        return category;
    }

    public int page() {
        return page;
    }

    public MineStackHolder withPage(int page) {
        return new MineStackHolder(view, category, page);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
