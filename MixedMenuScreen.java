package dev.mixedpack.gui;

import dev.mixedpack.Config;
import dev.mixedpack.Module;
import dev.mixedpack.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MixedMenuScreen extends Screen {
    private static final int VIOLET = 0xFF9D4DFF;
    private static final int VIOLET_LIGHT = 0xFFB388FF;
    private static final int PANEL = 0xE60C0A14;
    private static final int CARD_ON = 0xFF1B1630;
    private static final int CARD_OFF = 0xFF120F1C;
    private static final int CARD_HOVER = 0xFF221B3A;
    private static final int TEXT_ON = 0xFFFFFFFF;
    private static final int TEXT_OFF = 0xFF5E5875;
    private static final int TRACK_OFF = 0xFF2A2538;

    private static final int CARD_H = 34;
    private static final int GAP = 8;
    private static final int TOP = 52;     // header balandligi
    private static final int BOTTOM = 38;  // footer balandligi

    private float masterAnim = Modules.master ? 1f : 0f;
    private float scroll = 0f;
    private float scrollTarget = 0f;

    public MixedMenuScreen() {
        super(Text.literal("Mixed Pack"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---------- layout ----------
    private int rows() { return (Modules.ALL.size() + 1) / 2; }
    private int contentH() { return rows() * (CARD_H + GAP) - GAP; }
    private int fullPh() { return TOP + contentH() + BOTTOM; }

    private int pw() { return Math.min(480, width - 40); }
    private int px() { return (width - pw()) / 2; }
    private int py() {
        int full = fullPh();
        return (full + 64 + 10 <= height) ? Math.max(64, (height - full) / 2) : 64;
    }
    private int ph() { return Math.max(140, Math.min(fullPh(), height - py() - 10)); }
    private int lx() { return px() + 16; }
    private int ly() { return py() + TOP; }
    private int lw() { return pw() - 32; }
    private int lh() { return ph() - TOP - BOTTOM; }
    private int colW() { return (lw() - 10) / 2; }
    private int maxScroll() { return Math.max(0, contentH() - lh()); }

    private int masterX() { return px() + pw() - 16 - 36; }
    private int masterY() { return py() + 14; }

    private boolean inList(double mx, double my) {
        return mx >= lx() && mx < lx() + lw() && my >= ly() && my < ly() + lh();
    }

    // ---------- render ----------
    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);

        masterAnim += ((Modules.master ? 1f : 0f) - masterAnim) * 0.25f;
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll()));
        scroll += (scrollTarget - scroll) * 0.3f;

        drawTitle(c);

        for (int i = 6; i >= 1; i--) {
            rrect(c, px() - i, py() - i, pw() + i * 2, ph() + i * 2, 14 + i, 0x079D4DFF);
        }
        rrect(c, px(), py(), pw(), ph(), 14, PANEL);

        drawHeader(c);
        String hoverDesc = drawCards(c, mx, my);
        drawScrollbar(c);
        drawFooter(c, hoverDesc);
    }

    private void drawTitle(DrawContext c) {
        int cx = width / 2;
        int ty = py() - 40;
        String a = "Mixed";
        String b = "Pack";
        float s = 1.6f;
        int wa = textRenderer.getWidth(a);
        int wb = textRenderer.getWidth(b);
        int gap = 4;
        int totalW = (int) ((wa + gap + wb) * s);
        int startX = cx - totalW / 2 + 14;

        int lx = startX - 36;
        int ly = ty + 8;
        int[][] pts = {{0, 0}, {10, 0}, {14, -8}, {19, 12}, {24, -12}, {28, 0}, {34, 0}};
        for (int i = 0; i < pts.length - 1; i++) {
            line(c, lx + pts[i][0], ly + pts[i][1], lx + pts[i + 1][0], ly + pts[i + 1][1], VIOLET);
        }

        c.getMatrices().push();
        c.getMatrices().translate(startX, ty, 0);
        c.getMatrices().scale(s, s, 1f);
        c.drawText(textRenderer, a, 0, 0, TEXT_ON, true);
        c.drawText(textRenderer, b, wa + gap, 0, VIOLET_LIGHT, true);
        c.getMatrices().pop();
    }

    private void drawHeader(DrawContext c) {
        c.drawText(textRenderer, "Mixed Pack  /  Modules", px() + 16, py() + 19, 0xFF8A83A3, false);

        int tw = 36, th = 18;
        int tx = masterX(), ty = masterY();
        if (masterAnim > 0.05f) {
            rrect(c, tx - 3, ty - 3, tw + 6, th + 6, 12, ((int) (masterAnim * 0x40) << 24) | 0x9D4DFF);
        }
        rrect(c, tx, ty, tw, th, 9, lerp(TRACK_OFF, VIOLET, masterAnim));
        int kx = tx + 2 + Math.round((tw - th) * masterAnim);
        rrect(c, kx, ty + 2, th - 4, th - 4, 7, lerp(0xFF6B6580, 0xFFFFFFFF, masterAnim));

        String label = Modules.master ? "ON" : "OFF";
        int lw = textRenderer.getWidth(label);
        c.drawText(textRenderer, label, tx - 8 - lw, ty + 5, Modules.master ? VIOLET_LIGHT : TEXT_OFF, false);
    }

    private String drawCards(DrawContext c, int mx, int my) {
        List<Module> mods = Modules.ALL;
        int cw = colW();
        String desc = null;
        float dim = 0.4f + 0.6f * masterAnim;

        c.enableScissor(lx() - 4, ly(), lx() + lw() + 4, ly() + lh());
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % 2, row = i / 2;
            int x = lx() + col * (cw + 10);
            int y = ly() + row * (CARD_H + GAP) - Math.round(scroll);

            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.25f;
            float k = m.anim * dim;

            boolean hover = inList(mx, my) && mx >= x && mx < x + cw && my >= y && my < y + CARD_H;
            if (hover) desc = m.description;

            int bg = hover ? CARD_HOVER : lerp(CARD_OFF, CARD_ON, k);
            if (k > 0.3f) rrect(c, x - 1, y - 1, cw + 2, CARD_H + 2, 9, 0x259D4DFF);
            rrect(c, x, y, cw, CARD_H, 8, bg);

            c.drawText(textRenderer, m.name, x + 14, y + (CARD_H - 8) / 2, lerp(TEXT_OFF, TEXT_ON, k), false);

            int tw = 28, th = 14;
            int tx = x + cw - 14 - tw;
            int ty = y + (CARD_H - th) / 2;
            if (k > 0.05f) rrect(c, tx - 2, ty - 2, tw + 4, th + 4, 9, ((int) (k * 0x30) << 24) | 0x9D4DFF);
            rrect(c, tx, ty, tw, th, 7, lerp(TRACK_OFF, VIOLET, k));
            int kx = tx + 2 + Math.round((tw - th) * m.anim);
            rrect(c, kx, ty + 2, th - 4, th - 4, 5, lerp(0xFF6B6580, 0xFFFFFFFF, k));
        }
        c.disableScissor();
        return desc;
    }

    private void drawScrollbar(DrawContext c) {
        int max = maxScroll();
        if (max <= 0) return;
        int trackX = px() + pw() - 8;
        int trackH = lh();
        int barH = Math.max(20, trackH * trackH / contentH());
        int barY = ly() + (int) ((trackH - barH) * (scroll / max));
        rrect(c, trackX, barY, 3, barH, 2, 0xAA9D4DFF);
    }

    private void drawFooter(DrawContext c, String hoverDesc) {
        String t = hoverDesc != null ? hoverDesc : "Delete - yopish   |   Zoom: C ni bosib turing";
        int w = textRenderer.getWidth(t);
        c.drawText(textRenderer, t, px() + (pw() - w) / 2, py() + ph() - 22, 0xFF8A83A3, false);
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int tx = masterX(), ty = masterY();
            if (mx >= tx - 4 && mx < tx + 40 && my >= ty - 4 && my < ty + 22) {
                Modules.master = !Modules.master;
                Config.save();
                return true;
            }
            if (inList(mx, my)) {
                int cw = colW();
                for (int i = 0; i < Modules.ALL.size(); i++) {
                    int col = i % 2, row = i / 2;
                    int x = lx() + col * (cw + 10);
                    int y = ly() + row * (CARD_H + GAP) - Math.round(scroll);
                    if (mx >= x && mx < x + cw && my >= y && my < y + CARD_H) {
                        Modules.ALL.get(i).toggle();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scrollTarget -= (float) vAmount * 24f;
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll()));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_DELETE) {
            close();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void removed() {
        Config.save();
    }

    // ---------- draw helpers ----------
    private static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 255, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private static void rrect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int j = i < r ? i : (i >= h - r ? h - 1 - i : -1);
            int inset = 0;
            if (j >= 0) {
                double d = r - j - 0.5;
                inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - d * d)));
            }
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
        }
    }

    private static void line(DrawContext c, int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps == 0) steps = 1;
        for (int i = 0; i <= steps; i++) {
            int x = x0 + (x1 - x0) * i / steps;
            int y = y0 + (y1 - y0) * i / steps;
            c.fill(x, y, x + 2, y + 2, color);
        }
    }
}
