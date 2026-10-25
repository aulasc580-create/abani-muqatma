package dev.mixedvisuals.gui;

import dev.mixedvisuals.Category;
import dev.mixedvisuals.Config;
import dev.mixedvisuals.Module;
import dev.mixedvisuals.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MixedMenuScreen extends Screen {
    // Neon Violet palette
    private static final int VIOLET = 0xFF9D4DFF;
    private static final int VIOLET_LIGHT = 0xFFB388FF;
    private static final int PANEL = 0xE60C0A14;
    private static final int CARD_ON = 0xFF1B1630;
    private static final int CARD_OFF = 0xFF120F1C;
    private static final int CARD_HOVER = 0xFF221B3A;
    private static final int TEXT_ON = 0xFFFFFFFF;
    private static final int TEXT_OFF = 0xFF5E5875;
    private static final int TRACK_OFF = 0xFF2A2538;

    private static final int CARD_H = 32;
    private static final int GAP = 8;
    private static final int TAB = 28;

    private Category category = Category.UTILITIES;
    private String query = "";
    private float scroll = 0f;
    private float scrollTarget = 0f;

    public MixedMenuScreen() {
        super(Text.literal("Mixed Visuals"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---------- layout ----------
    private int pw() { return Math.min(560, width - 40); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return 64; }
    private int ph() { return Math.max(120, Math.min(340, height - py() - 70)); }
    private int lx() { return px() + 16; }
    private int ly() { return py() + 46; }
    private int lw() { return pw() - 32; }
    private int lh() { return ph() - 46 - 12; }
    private int colW() { return (lw() - 10) / 2; }

    private List<Module> visible() {
        List<Module> list = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Module m : Modules.ALL) {
            if (q.isEmpty()) {
                if (m.category == category) list.add(m);
            } else if (m.name.toLowerCase().contains(q)) {
                list.add(m);
            }
        }
        return list;
    }

    private int maxScroll(int count) {
        int rows = (count + 1) / 2;
        int total = rows * (CARD_H + GAP) - GAP;
        return Math.max(0, total - lh());
    }

    private int dockX() {
        int n = Category.values().length;
        int total = n * TAB + (n - 1) * 8;
        return (width - total) / 2;
    }

    private int dockY() { return py() + ph() + 14; }

    // ---------- render ----------
    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);

        List<Module> mods = visible();
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll(mods.size())));
        scroll += (scrollTarget - scroll) * 0.3f;

        drawTitle(c);

        // panel + glow
        for (int i = 6; i >= 1; i--) {
            rrect(c, px() - i, py() - i, pw() + i * 2, ph() + i * 2, 14 + i, 0x079D4DFF);
        }
        rrect(c, px(), py(), pw(), ph(), 14, PANEL);

        drawHeader(c, mx, my);
        drawCards(c, mods, mx, my);
        drawDock(c, mx, my);
    }

    private void drawTitle(DrawContext c) {
        int cx = width / 2;
        int ty = 20;
        String a = "Mixed";
        String b = "Visuals";
        float s = 1.6f;
        int wa = textRenderer.getWidth(a);
        int wb = textRenderer.getWidth(b);
        int gap = 4;
        int totalW = (int) ((wa + gap + wb) * s);
        int startX = cx - totalW / 2 + 14;

        // pulse logo
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

    private void drawHeader(DrawContext c, int mx, int my) {
        String crumb = "Mixed  /  " + category.title;
        c.drawText(textRenderer, crumb, px() + 16, py() + 17, 0xFF8A83A3, false);

        int sw = 140, sh = 20;
        int sx = px() + pw() - 16 - sw;
        int sy = py() + 12;
        rrect(c, sx, sy, sw, sh, 6, 0xFF151221);
        String shown = query.isEmpty() ? "Search..." : query;
        int col = query.isEmpty() ? 0xFF5E5875 : TEXT_ON;
        if (!query.isEmpty() && (System.currentTimeMillis() / 500) % 2 == 0) shown += "_";
        c.drawText(textRenderer, shown, sx + 8, sy + 6, col, false);
    }

    private void drawCards(DrawContext c, List<Module> mods, int mx, int my) {
        int ax = lx(), ay = ly(), aw = lw(), ah = lh();
        c.enableScissor(ax - 2, ay, ax + aw + 2, ay + ah);

        int cw = colW();
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % 2, row = i / 2;
            int x = ax + col * (cw + 10);
            int y = ay + row * (CARD_H + GAP) - Math.round(scroll);

            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.25f;

            boolean hover = inList(mx, my) && mx >= x && mx < x + cw && my >= y && my < y + CARD_H;
            int bg = hover ? CARD_HOVER : (m.enabled ? CARD_ON : CARD_OFF);
            if (m.enabled) rrect(c, x - 1, y - 1, cw + 2, CARD_H + 2, 9, 0x259D4DFF);
            rrect(c, x, y, cw, CARD_H, 8, bg);

            int tc = m.enabled ? TEXT_ON : TEXT_OFF;
            c.drawText(textRenderer, m.name, x + 14, y + (CARD_H - 8) / 2, tc, false);

            // toggle switch
            int tw = 28, th = 14;
            int tx = x + cw - 14 - tw;
            int ty = y + (CARD_H - th) / 2;
            if (m.anim > 0.05f) rrect(c, tx - 2, ty - 2, tw + 4, th + 4, 9, ((int) (m.anim * 0x30) << 24) | 0x9D4DFF);
            rrect(c, tx, ty, tw, th, 7, lerp(TRACK_OFF, VIOLET, m.anim));
            int kx = tx + 2 + Math.round((tw - th) * m.anim);
            rrect(c, kx, ty + 2, th - 4, th - 4, 5, lerp(0xFF6B6580, 0xFFFFFFFF, m.anim));
        }
        c.disableScissor();

        // description tooltip for hovered card
        if (inList(mx, my)) {
            for (int i = 0; i < mods.size(); i++) {
                int col = i % 2, row = i / 2;
                int x = ax + col * (cw + 10);
                int y = ay + row * (CARD_H + GAP) - Math.round(scroll);
                if (mx >= x && mx < x + cw && my >= y && my < y + CARD_H) {
                    c.drawTooltip(textRenderer, Text.literal(mods.get(i).description), mx, my);
                    break;
                }
            }
        }
    }

    private void drawDock(DrawContext c, int mx, int my) {
        Category[] cats = Category.values();
        int dx = dockX(), dy = dockY();
        int total = cats.length * TAB + (cats.length - 1) * 8;
        rrect(c, dx - 10, dy - 8, total + 20, TAB + 16, 14, 0xE60C0A14);

        for (int i = 0; i < cats.length; i++) {
            int x = dx + i * (TAB + 8);
            boolean sel = cats[i] == category && query.isEmpty();
            boolean hover = mx >= x && mx < x + TAB && my >= dy && my < dy + TAB;
            if (sel) rrect(c, x - 2, dy - 2, TAB + 4, TAB + 4, 10, 0x409D4DFF);
            rrect(c, x, dy, TAB, TAB, 8, sel ? VIOLET : (hover ? 0xFF241E38 : 0xFF17131F));
            int w = textRenderer.getWidth(cats[i].icon);
            c.drawText(textRenderer, cats[i].icon, x + (TAB - w) / 2, dy + (TAB - 8) / 2,
                    sel ? 0xFFFFFFFF : 0xFF8A83A3, false);
        }
    }

    // ---------- input ----------
    private boolean inList(double mx, double my) {
        return mx >= lx() && mx < lx() + lw() && my >= ly() && my < ly() + lh();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            // dock
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int x = dockX() + i * (TAB + 8);
                if (mx >= x && mx < x + TAB && my >= dockY() && my < dockY() + TAB) {
                    category = cats[i];
                    query = "";
                    scroll = 0;
                    scrollTarget = 0;
                    return true;
                }
            }
            // cards
            if (inList(mx, my)) {
                List<Module> mods = visible();
                int cw = colW();
                for (int i = 0; i < mods.size(); i++) {
                    int col = i % 2, row = i / 2;
                    int x = lx() + col * (cw + 10);
                    int y = ly() + row * (CARD_H + GAP) - Math.round(scroll);
                    if (mx >= x && mx < x + cw && my >= y && my < y + CARD_H) {
                        mods.get(i).toggle();
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
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll(visible().size())));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
            scrollTarget = 0;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char chr, int mods) {
        if (chr >= 32 && chr != 127 && query.length() < 20) {
            query += chr;
            scrollTarget = 0;
            return true;
        }
        return super.charTyped(chr, mods);
    }

    @Override
    public void removed() {
        Config.save();
    }

    // ---------- draw helpers ----------
    private static int lerp(int a, int b, float t) {
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
