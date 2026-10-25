package dev.mixedvisuals;

public class Module {
    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;
    public float anim; // 0..1 toggle animation

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void toggle() {
        enabled = !enabled;
        Config.save();
    }
}
