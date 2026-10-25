package dev.mixedpack;

public class Module {
    public final String name;
    public final String description;
    public boolean enabled = true;
    public float anim = 1f;

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void toggle() {
        enabled = !enabled;
        Config.save();
    }
}
