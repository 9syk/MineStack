package link.syk9.mineStack.model;

import org.bukkit.Material;

public enum Category {
    BUILDING("建築ブロック", Material.BRICKS),
    COLORED("色付きブロック", Material.CYAN_WOOL),
    NATURAL("自然ブロック", Material.GRASS_BLOCK),
    FUNCTIONAL("機能ブロック", Material.CRAFTING_TABLE),
    REDSTONE("レッドストーン", Material.REDSTONE),
    TOOLS_AND_COMBAT("道具と戦闘", Material.IRON_PICKAXE),
    FOOD("食べ物と飲み物", Material.APPLE),
    INGREDIENTS("材料", Material.IRON_INGOT);

    private final String displayName;
    private final Material icon;

    Category(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }
}
