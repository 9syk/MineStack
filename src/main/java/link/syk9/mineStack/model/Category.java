package link.syk9.mineStack.model;

import org.bukkit.Material;

public enum Category {
    BUILDING("建築ブロック", Material.BRICKS) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return material.isBlock() && (name.endsWith("PLANKS") || name.endsWith("STONE") || name.endsWith("BRICKS")
                    || name.endsWith("SLAB") || name.endsWith("STAIRS") || name.endsWith("WALL")
                    || name.endsWith("FENCE") || name.endsWith("FENCE_GATE") || name.endsWith("PURPUR")
                    || name.endsWith("QUARTZ") || name.contains("COPPER") || name.contains("DEEPSLATE"));
        }
    },
    COLORED("色付きブロック", Material.CYAN_WOOL) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return hasColorPrefix(name) && (name.endsWith("WOOL") || name.endsWith("CARPET")
                    || name.endsWith("TERRACOTTA") || name.endsWith("CONCRETE")
                    || name.endsWith("CONCRETE_POWDER") || name.endsWith("STAINED_GLASS")
                    || name.endsWith("STAINED_GLASS_PANE") || name.endsWith("CANDLE")
                    || name.endsWith("BANNER") || name.endsWith("BED") || name.endsWith("SHULKER_BOX"));
        }
    },
    NATURAL("自然ブロック", Material.GRASS_BLOCK) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return material.isBlock() && (name.contains("LOG") || name.contains("WOOD") || name.contains("LEAVES")
                    || name.contains("SAPLING") || name.contains("DIRT") || name.contains("SAND")
                    || name.contains("ORE") || name.contains("CORAL") || name.contains("MUSHROOM")
                    || name.contains("FLOWER") || name.contains("ROOTS") || name.contains("NYLIUM")
                    || name.equals("GRASS_BLOCK") || name.equals("GRAVEL") || name.equals("CLAY")
                    || name.equals("ICE") || name.equals("SNOW_BLOCK") || name.equals("NETHERRACK")
                    || name.equals("END_STONE"));
        }
    },
    FUNCTIONAL("機能ブロック", Material.CRAFTING_TABLE) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return material.isBlock() && (name.contains("CHEST") || name.contains("FURNACE") || name.contains("TABLE")
                    || name.contains("ANVIL") || name.contains("BED") || name.contains("BANNER")
                    || name.contains("SIGN") || name.contains("DOOR") || name.contains("TRAPDOOR")
                    || name.contains("BARREL") || name.contains("HOPPER") || name.contains("LECTERN")
                    || name.contains("BOOKSHELF") || name.contains("CAULDRON") || name.contains("POT")
                    || name.contains("HEAD") || name.contains("SKULL") || name.contains("LIGHT")
                    || name.contains("SPAWNER") || name.equals("BELL"));
        }
    },
    REDSTONE("レッドストーン", Material.REDSTONE) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return name.contains("REDSTONE") || name.contains("PISTON") || name.contains("RAIL")
                    || name.contains("BUTTON") || name.contains("LEVER") || name.contains("PRESSURE_PLATE")
                    || name.contains("OBSERVER") || name.contains("DISPENSER") || name.contains("DROPPER")
                    || name.contains("COMPARATOR") || name.contains("REPEATER") || name.contains("SCULK_SENSOR")
                    || name.contains("TRIPWIRE");
        }
    },
    TOOLS_AND_COMBAT("道具と戦闘", Material.IRON_PICKAXE) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return name.endsWith("SWORD") || name.endsWith("PICKAXE") || name.endsWith("AXE") || name.endsWith("SHOVEL")
                    || name.endsWith("HOE") || name.endsWith("HELMET") || name.endsWith("CHESTPLATE")
                    || name.endsWith("LEGGINGS") || name.endsWith("BOOTS") || name.equals("BOW") || name.equals("CROSSBOW")
                    || name.equals("TRIDENT") || name.equals("SHIELD") || name.equals("MACE") || name.equals("SHEARS")
                    || name.equals("FLINT_AND_STEEL") || name.equals("FISHING_ROD") || name.equals("BRUSH")
                    || name.equals("COMPASS") || name.equals("CLOCK") || name.equals("ELYTRA") || name.endsWith("MINECART")
                    || name.endsWith("BOAT") || name.endsWith("RAFT") || name.equals("ARROW") || name.equals("SPECTRAL_ARROW");
        }
    },
    FOOD("食べ物と飲み物", Material.APPLE) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return material.isEdible() || name.contains("POTION") || name.equals("MILK_BUCKET");
        }
    },
    INGREDIENTS("材料", Material.IRON_INGOT) {
        @Override
        public boolean matches(Material material) {
            String name = material.name();
            return name.endsWith("SPAWN_EGG") || name.endsWith("INGOT") || name.endsWith("NUGGET")
                    || name.endsWith("DUST") || name.endsWith("GEM") || name.endsWith("SHARD")
                    || name.endsWith("SCRAP") || name.endsWith("ROD") || name.endsWith("STRING")
                    || name.endsWith("LEATHER") || name.endsWith("FEATHER") || name.endsWith("BONE")
                    || name.endsWith("PEARL") || name.endsWith("DYE") || name.equals("DIAMOND")
                    || name.equals("EMERALD") || name.equals("COAL") || name.equals("CHARCOAL")
                    || name.equals("STICK") || name.equals("PAPER") || name.equals("BOOK")
                    || (!material.isBlock() && !material.isEdible() && !TOOLS_AND_COMBAT.matches(material));
        }
    };

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

    public abstract boolean matches(Material material);

    private static boolean hasColorPrefix(String name) {
        return name.startsWith("WHITE_") || name.startsWith("LIGHT_GRAY_") || name.startsWith("GRAY_")
                || name.startsWith("BLACK_") || name.startsWith("BROWN_") || name.startsWith("RED_")
                || name.startsWith("ORANGE_") || name.startsWith("YELLOW_") || name.startsWith("LIME_")
                || name.startsWith("GREEN_") || name.startsWith("CYAN_") || name.startsWith("LIGHT_BLUE_")
                || name.startsWith("BLUE_") || name.startsWith("PURPLE_") || name.startsWith("MAGENTA_")
                || name.startsWith("PINK_");
    }
}
