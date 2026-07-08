package link.syk9.mineStack.model;

public enum AutoStoreMode {
    PERSONAL("個人"),
    SHARED("共有"),
    OFF("オフ");

    private final String displayName;

    AutoStoreMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public AutoStoreMode next(boolean sharedEnabled) {
        if (sharedEnabled) {
            return switch (this) {
                case PERSONAL -> SHARED;
                case SHARED -> OFF;
                case OFF -> PERSONAL;
            };
        }
        return this == PERSONAL ? OFF : PERSONAL;
    }

    public static AutoStoreMode fromName(String name) {
        try {
            return AutoStoreMode.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return PERSONAL;
        }
    }
}
