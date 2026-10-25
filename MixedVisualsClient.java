package dev.mixedvisuals;

import dev.mixedvisuals.gui.MixedMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MixedVisualsClient implements ClientModInitializer {
    public static final int VIOLET = 0xFF9D4DFF;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mixedvisuals.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.category.mixedvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MixedMenuScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            int y = 6;
            if (Modules.FPS.enabled) {
                ctx.drawText(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 6, y, VIOLET, true);
                y += 12;
            }
            if (Modules.COORDS.enabled) {
                ctx.drawText(mc.textRenderer,
                        "XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(),
                        6, y, VIOLET, true);
            }
        });
    }
}
