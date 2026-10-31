package su.aurora.core;

public enum Category {
    RENDER("Render", "Визуал"),
    PARTICLES("Particles", "Частицы"),
    HUD("Interface", "Интерфейс"),
    PLAYER("Player", "Игрок"),
    MISC("Misc", "Разное");

    private final String displayName;
    private final String russianName;

    Category(String displayName, String russianName) {
        this.displayName = displayName;
        this.russianName = russianName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianName() {
        return russianName;
    }
}
