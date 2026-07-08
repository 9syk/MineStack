package link.syk9.mineStack.model;

public enum SortMode {
    DEFAULT("デフォルト"),
    COUNT("所持数"),
    ID_ASC("A→Z"),
    ID_DESC("Z→A");

    private final String displayName;

    SortMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public SortMode next() {
        SortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static SortMode fromName(String name) {
        try {
            return SortMode.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return DEFAULT;
        }
    }
}
