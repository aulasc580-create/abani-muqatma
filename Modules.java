package dev.mixedpack;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();

    /** Asosiy (master) tugma: o'chiq bo'lsa hamma modul to'xtaydi. */
    public static boolean master = false;

    public static final Module INFO = add("Info HUD", "FPS va koordinatalarni ko'rsatadi", true);
    public static final Module FULLBRIGHT = add("Fullbright", "Hamma joy yorug' (qorong'ida ham ko'rinadi)", true);
    public static final Module ZOOM = add("Zoom", "C tugmasini bosib turing - yaqinlashtiradi", true);
    public static final Module KEYSTROKES = add("Keystrokes", "W A S D va Space tugmalarini ko'rsatadi", true);
    public static final Module ARMOR = add("Armor HUD", "Zirh va uning chidamliligi (o'ng tomonda)", true);
    public static final Module SPRINT = add("Auto Sprint", "Oldinga yurganda avtomatik yuguradi", true);

    public static final Module CROSSHAIR = add("Crosshair Indicator", "Nishon o'yinchi ustida bo'lsa rangi o'zgaradi", true);
    public static final Module TARGET_HP = add("Target HP", "Nishondagi o'yinchining HP si", true);
    public static final Module DAMAGE = add("Damage Indicator", "Zarar raqamlari nishon yonida uchib chiqadi", true);
    public static final Module POTION = add("Potion HUD", "Faol effektlar va qolgan vaqti", true);
    public static final Module ARROW = add("Arrow HUD", "Kamon qo'lda bo'lsa o'qlar soni", true);
    public static final Module CRYSTAL = add("Crystal Optimizer", "Crystalga urganda darrov yo'qoladi (ba'zi serverlarda taqiqlangan!)", false);

    public static boolean active(Module m) {
        return master && m.enabled;
    }

    private static Module add(String name, String desc, boolean def) {
        Module m = new Module(name, desc);
        m.enabled = def;
        m.anim = def ? 1f : 0f;
        ALL.add(m);
        return m;
    }

    private Modules() {}
}
