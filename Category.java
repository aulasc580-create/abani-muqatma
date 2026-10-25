package dev.mixedvisuals;

public enum Category {
    VISUALS("Visuals", "V"),
    HUD("HUD", "H"),
    UTILITIES("Utilities", "U"),
    COMBAT("Combat", "C");

    public final String title;
    public final String icon;

    Category(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }
}
