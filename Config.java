package dev.mixedvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("mixedvisuals.json");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Map<String, Boolean> map = GSON.fromJson(Files.readString(f),
                    new TypeToken<Map<String, Boolean>>() {}.getType());
            if (map == null) return;
            for (Module m : Modules.ALL) {
                Boolean v = map.get(m.name);
                if (v != null) {
                    m.enabled = v;
                    m.anim = v ? 1f : 0f;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Map<String, Boolean> map = new HashMap<>();
            for (Module m : Modules.ALL) map.put(m.name, m.enabled);
            Files.writeString(file(), GSON.toJson(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Config() {}
}
