package dev.mixedpack;

import dev.mixedpack.gui.MixedMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MixedPackClient implements ClientModInitializer {
    public static final int VIOLET = 0xFF9D4DFF;
    public static final int VIOLET_LIGHT = 0xFFB388FF;

    private static KeyBinding menuKey;
    private static KeyBinding zoomKey;

    private static boolean nvApplied = false;
    private static boolean zoomApplied = false;
    private static boolean sprintApplied = false;
    private static int savedFov = 70;

    // damage indicator holati
    private static UUID dmgTarget = null;
    private static float dmgTargetHp = 0f;
    private static float lastOwnHp = -1f;
    private static final List<Pop> POPS = new ArrayList<>();

    private static class Pop {
        final float amount;
        final long time;
        final boolean own;
        Pop(float amount, boolean own) {
            this.amount = amount;
            this.own = own;
            this.time = System.currentTimeMillis();
        }
    }

    @Override
    public void onInitializeClient() {
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mixedpack.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DELETE, "key.category.mixedpack"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mixedpack.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.category.mixedpack"));

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudRenderCallback.EVENT.register(this::renderHud);

        // Crystal Optimizer: crystalga urilganda klientda darrov olib tashlaymiz
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && Modules.active(Modules.CRYSTAL) && entity instanceof EndCrystalEntity) {
                entity.discard();
            }
            return ActionResult.PASS;
        });
    }

    // ---------------- logic ----------------
    private void tick(MinecraftClient mc) {
        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new MixedMenuScreen());
        }

        GameOptions opt = mc.options;

        // Zoom
        boolean zooming = Modules.active(Modules.ZOOM) && zoomKey.isPressed() && mc.currentScreen == null && mc.player != null;
        if (zooming) {
            if (!zoomApplied) {
                savedFov = opt.getFov().getValue();
                zoomApplied = true;
            }
            if (opt.getFov().getValue() != 30) opt.getFov().setValue(30);
        } else if (zoomApplied) {
            opt.getFov().setValue(savedFov);
            zoomApplied = false;
        }

        PlayerEntity player = mc.player;
        if (player == null) {
            nvApplied = false;
            sprintApplied = false;
            dmgTarget = null;
            lastOwnHp = -1f;
            POPS.clear();
            return;
        }

        // Fullbright
        if (Modules.active(Modules.FULLBRIGHT)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
            nvApplied = true;
        } else if (nvApplied) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            nvApplied = false;
        }

        // Auto Sprint
        if (Modules.active(Modules.SPRINT)) {
            opt.sprintKey.setPressed(opt.forwardKey.isPressed());
            sprintApplied = true;
        } else if (sprintApplied) {
            opt.sprintKey.setPressed(false);
            sprintApplied = false;
        }

        // Damage Indicator
        if (Modules.active(Modules.DAMAGE)) {
            Entity t = mc.targetedEntity;
            if (t instanceof LivingEntity le) {
                float hp = le.getHealth() + le.getAbsorptionAmount();
                if (le.getUuid().equals(dmgTarget)) {
                    if (hp < dmgTargetHp - 0.01f) POPS.add(new Pop(dmgTargetHp - hp, false));
                } else {
                    dmgTarget = le.getUuid();
                }
                dmgTargetHp = hp;
            } else {
                dmgTarget = null;
            }

            float own = player.getHealth() + player.getAbsorptionAmount();
            if (lastOwnHp >= 0f && own < lastOwnHp - 0.01f) POPS.add(new Pop(lastOwnHp - own, true));
            lastOwnHp = own;
        } else {
            dmgTarget = null;
            lastOwnHp = -1f;
            POPS.clear();
        }
    }

    // ---------------- HUD ----------------
    private void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        if (!Modules.master) return;

        if (Modules.INFO.enabled) {
            int y = 6;
            ctx.drawText(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 6, y, VIOLET_LIGHT, true);
            y += 12;
            ctx.drawText(mc.textRenderer,
                    "XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(),
                    6, y, VIOLET_LIGHT, true);
        }

        if (Modules.KEYSTROKES.enabled) drawKeystrokes(ctx, mc);
        if (Modules.ARMOR.enabled) drawArmor(ctx, mc);
        if (Modules.POTION.enabled) drawPotions(ctx, mc);
        if (Modules.ARROW.enabled) drawArrows(ctx, mc);

        if (mc.currentScreen == null && mc.options.getPerspective().isFirstPerson()) {
            PlayerEntity target = targetPlayer(mc);
            if (Modules.CROSSHAIR.enabled && target != null) drawCrosshairIndicator(ctx);
            if (Modules.TARGET_HP.enabled && target != null) drawTargetHp(ctx, mc, target);
            if (Modules.DAMAGE.enabled) drawPops(ctx, mc);
        }
    }

    private static PlayerEntity targetPlayer(MinecraftClient mc) {
        if (mc.crosshairTarget instanceof EntityHitResult r
                && r.getEntity() instanceof PlayerEntity p
                && p != mc.player) {
            return p;
        }
        return null;
    }

    private void drawCrosshairIndicator(DrawContext ctx) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int glow = 0x449D4DFF;
        int col = VIOLET_LIGHT;
        // glow
        ctx.fill(cx - 11, cy - 2, cx - 4, cy + 2, glow);
        ctx.fill(cx + 4, cy - 2, cx + 11, cy + 2, glow);
        ctx.fill(cx - 2, cy - 11, cx + 2, cy - 4, glow);
        ctx.fill(cx - 2, cy + 4, cx + 2, cy + 11, glow);
        // bars
        ctx.fill(cx - 10, cy - 1, cx - 5, cy + 1, col);
        ctx.fill(cx + 5, cy - 1, cx + 10, cy + 1, col);
        ctx.fill(cx - 1, cy - 10, cx + 1, cy - 5, col);
        ctx.fill(cx - 1, cy + 5, cx + 1, cy + 10, col);
    }

    private void drawTargetHp(DrawContext ctx, MinecraftClient mc, PlayerEntity p) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        float hp = p.getHealth() + p.getAbsorptionAmount();
        float max = Math.max(1f, p.getMaxHealth());
        float pct = p.getHealth() / max;
        int color = pct > 0.5f ? VIOLET_LIGHT : (pct > 0.25f ? 0xFFFFCC55 : 0xFFFF5555);
        String text = p.getName().getString() + "  " + fmt(hp) + " HP";
        int w = mc.textRenderer.getWidth(text);
        ctx.drawText(mc.textRenderer, text, cx - w / 2, cy + 16, color, true);
    }

    private void drawPops(DrawContext ctx, MinecraftClient mc) {
        long now = System.currentTimeMillis();
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        POPS.removeIf(p -> now - p.time > 1000);
        for (Pop p : POPS) {
            float age = (now - p.time) / 1000f;
            int alpha = (int) (255 * (1f - age));
            if (alpha < 12) continue;
            int rgb = p.own ? 0xFF5555 : 0xFFCC55;
            int color = (alpha << 24) | rgb;
            String t = "-" + fmt(p.amount);
            int w = mc.textRenderer.getWidth(t);
            int x = p.own ? cx - 40 - w : cx + 40;
            int y = cy - 6 - (int) (age * 18);
            ctx.drawText(mc.textRenderer, t, x, y, color, true);
        }
    }

    private void drawKeystrokes(DrawContext ctx, MinecraftClient mc) {
        GameOptions o = mc.options;
        int s = 20;
        int g = 2;
        int x = 8;
        int y = ctx.getScaledWindowHeight() / 2 - 30;

        key(ctx, mc, "W", x + s + g, y, s, s, o.forwardKey.isPressed());
        key(ctx, mc, "A", x, y + s + g, s, s, o.leftKey.isPressed());
        key(ctx, mc, "S", x + s + g, y + s + g, s, s, o.backKey.isPressed());
        key(ctx, mc, "D", x + (s + g) * 2, y + s + g, s, s, o.rightKey.isPressed());
        key(ctx, mc, "SPACE", x, y + (s + g) * 2, s * 3 + g * 2, 14, o.jumpKey.isPressed());
    }

    private void key(DrawContext ctx, MinecraftClient mc, String label, int x, int y, int w, int h, boolean down) {
        ctx.fill(x, y, x + w, y + h, down ? 0xCC9D4DFF : 0x990C0A14);
        int border = down ? 0xFFB388FF : 0xFF3A3150;
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        ctx.fill(x, y, x + 1, y + h, border);
        ctx.fill(x + w - 1, y, x + w, y + h, border);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawText(mc.textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFFFFFFF, false);
    }

    private void drawArmor(DrawContext ctx, MinecraftClient mc) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int x = ctx.getScaledWindowWidth() - 26;
        int y = ctx.getScaledWindowHeight() / 2 - 36;
        for (int i = 0; i < slots.length; i++) {
            ItemStack st = mc.player.getEquippedStack(slots[i]);
            if (st.isEmpty()) continue;
            int iy = y + i * 18;
            ctx.drawItem(st, x, iy);
            if (st.isDamageable()) {
                int max = st.getMaxDamage();
                int pct = (max - st.getDamage()) * 100 / Math.max(1, max);
                String t = pct + "%";
                int color = pct > 50 ? 0xFFB388FF : (pct > 20 ? 0xFFFFCC55 : 0xFFFF5555);
                ctx.drawText(mc.textRenderer, t, x - 4 - mc.textRenderer.getWidth(t), iy + 4, color, true);
            }
        }
    }

    private void drawPotions(DrawContext ctx, MinecraftClient mc) {
        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        int y = 6;
        int right = ctx.getScaledWindowWidth() - 6;
        for (StatusEffectInstance e : effects) {
            String name = e.getEffectType().value().getName().getString();
            if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
            String time;
            if (e.isInfinite()) {
                time = "**";
            } else {
                int s = e.getDuration() / 20;
                time = (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
            }
            String line = name + "  " + time;
            int w = mc.textRenderer.getWidth(line);
            ctx.drawText(mc.textRenderer, line, right - w, y, VIOLET_LIGHT, true);
            y += 11;
        }
    }

    private void drawArrows(DrawContext ctx, MinecraftClient mc) {
        PlayerEntity p = mc.player;
        boolean bow = p.getMainHandStack().isOf(Items.BOW) || p.getMainHandStack().isOf(Items.CROSSBOW)
                || p.getOffHandStack().isOf(Items.BOW) || p.getOffHandStack().isOf(Items.CROSSBOW);
        if (!bow) return;
        int count = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack st = p.getInventory().getStack(i);
            if (st.isOf(Items.ARROW) || st.isOf(Items.SPECTRAL_ARROW) || st.isOf(Items.TIPPED_ARROW)) {
                count += st.getCount();
            }
        }
        int x = ctx.getScaledWindowWidth() - 48;
        int y = ctx.getScaledWindowHeight() / 2 + 44;
        ctx.drawItem(new ItemStack(Items.ARROW), x, y);
        ctx.drawText(mc.textRenderer, "x" + count, x + 18, y + 4, count > 0 ? VIOLET_LIGHT : 0xFFFF5555, true);
    }

    private static String fmt(float v) {
        float r = Math.round(v * 10f) / 10f;
        return r == (int) r ? String.valueOf((int) r) : String.valueOf(r);
    }
}
