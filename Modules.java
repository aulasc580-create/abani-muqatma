package dev.mixedvisuals;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();

    // Ishlaydigan modullar (hozircha)
    public static final Module FPS = add("FPS Counter", "Ekranda FPS", Category.HUD);
    public static final Module COORDS = add("Coordinates", "Ekranda koordinatalar", Category.HUD);

    static {
        // Visuals
        add("Hit Color", "Urilganda rang berish", Category.VISUALS);
        add("Totem Pop", "Totem effektlari", Category.VISUALS);
        add("Low Fire", "Past olov", Category.VISUALS);
        add("Custom Crosshair", "Maxsus nishon", Category.VISUALS);
        add("Particles", "Zarrachalar", Category.VISUALS);
        add("Damage Indicator", "Zarar ko'rsatkichi", Category.VISUALS);
        add("Fullbright", "To'liq yorug'lik", Category.VISUALS);
        add("Zoom", "Yaqinlashtirish", Category.VISUALS);
        // HUD
        add("Armor HUD", "Zirh holati", Category.HUD);
        add("Keystrokes", "Tugmalar ko'rsatkichi", Category.HUD);
        add("Potion Status", "Effektlar ro'yxati", Category.HUD);
        add("Target HUD", "Dushman ma'lumoti", Category.HUD);
        // Utilities
        add("Free Look", "Erkin qarash", Category.UTILITIES);
        add("Cooldowns", "Kuldaunlar", Category.UTILITIES);
        add("Chat Helper", "Chat yordamchisi", Category.UTILITIES);
        add("Item Highlight", "Buyum ajratish", Category.UTILITIES);
        add("Command Safe", "Buyruq himoyasi", Category.UTILITIES);
        add("Fast Swap", "Tez almashtirish", Category.UTILITIES);
        // Combat
        add("Swing Animation", "Urish animatsiyasi", Category.COMBAT);
        add("Block Animation", "Qalqon animatsiyasi", Category.COMBAT);
        add("Hit Sound", "Urish tovushi", Category.COMBAT);
        add("Kill Effect", "Kill effekti", Category.COMBAT);
    }

    private static Module add(String name, String desc, Category cat) {
        Module m = new Module(name, desc, cat);
        ALL.add(m);
        return m;
    }

    public static List<Module> of(Category cat) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == cat) out.add(m);
        return out;
    }

    private Modules() {}
}
