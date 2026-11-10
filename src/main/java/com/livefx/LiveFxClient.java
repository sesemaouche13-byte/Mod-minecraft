package com.livefx;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Live FX : chaque touche (rebindable dans Options > Controles > Live FX)
 * declenche un effet plein ecran + des commandes optionnelles (sons, particules, mobs).
 */
public class LiveFxClient implements ClientModInitializer {
    public static final String MOD_ID = "livefx";

    // ======================= REGLAGES =======================
    /** Texte du gros popup. */
    static final String POPUP_TEXT = "GG !";
    /** false = effets visuels uniquement, aucune commande envoyee au jeu. */
    static final boolean RUN_COMMANDS = true;
    // ========================================================

    interface Painter {
        void paint(GuiGraphics g, int w, int h, float t, long ms);
    }

    /** t va de 0 a 1 pendant la duree de l'effet. */
    record Fx(String id, int defaultKey, long durationMs, Painter painter, String... commands) {}

    static final class Active {
        final Fx fx;
        final long start;

        Active(Fx fx, long start) {
            this.fx = fx;
            this.start = start;
        }
    }

    static final List<Fx> FX = new ArrayList<>();
    static final List<KeyMapping> KEYS = new ArrayList<>();
    static final List<Active> ACTIVE = new ArrayList<>();
    static KeyMapping stopKey;

    @Override
    public void onInitializeClient() {
        registerEffects();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        for (Fx fx : FX) {
            KEYS.add(KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key." + MOD_ID + "." + fx.id(),
                    InputConstants.Type.KEYSYM,
                    fx.defaultKey(),
                    category)));
        }
        stopKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + MOD_ID + ".stop",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_END,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (int i = 0; i < KEYS.size(); i++) {
                while (KEYS.get(i).consumeClick()) {
                    trigger(client, FX.get(i));
                }
            }
            while (stopKey.consumeClick()) {
                ACTIVE.clear();
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "overlay"),
                (graphics, deltaTracker) -> render(graphics));
    }

    // ----------------------------------------------------------------
    // Effets : touche par defaut, duree, rendu, commandes
    // ----------------------------------------------------------------
    static void registerEffects() {
        FX.add(new Fx("flash", GLFW.GLFW_KEY_H, 900, LiveFxClient::flash,
                "playsound minecraft:entity.lightning_bolt.thunder master @s ~ ~ ~ 2 1"));

        FX.add(new Fx("boom", GLFW.GLFW_KEY_J, 1800, LiveFxClient::boom,
                "playsound minecraft:entity.generic.explode master @s ~ ~ ~ 3 0.8",
                "particle minecraft:explosion_emitter ~ ~1 ~"));

        FX.add(new Fx("rainbow", GLFW.GLFW_KEY_K, 4000, LiveFxClient::rainbow,
                "playsound minecraft:entity.firework_rocket.launch master @s ~ ~ ~ 1 1"));

        FX.add(new Fx("glitch", GLFW.GLFW_KEY_U, 1600, LiveFxClient::glitch,
                "playsound minecraft:block.beacon.deactivate master @s ~ ~ ~ 1 0.5"));

        FX.add(new Fx("danger", GLFW.GLFW_KEY_I, 5000, LiveFxClient::danger,
                "playsound minecraft:block.bell.use master @s ~ ~ ~ 2 0.5",
                "summon minecraft:zombie ~4 ~ ~4",
                "summon minecraft:zombie ~-4 ~ ~4",
                "summon minecraft:zombie ~4 ~ ~-4"));

        FX.add(new Fx("popup", GLFW.GLFW_KEY_O, 3000, LiveFxClient::popup,
                "playsound minecraft:entity.player.levelup master @s ~ ~ ~ 1 1",
                "particle minecraft:totem_of_undying ~ ~1 ~ 0.5 1 0.5 0.5 60"));

        FX.add(new Fx("confetti", GLFW.GLFW_KEY_Y, 5000, LiveFxClient::confetti,
                "playsound minecraft:entity.firework_rocket.twinkle master @s ~ ~ ~ 1 1",
                "particle minecraft:firework ~ ~2 ~ 1 1 1 0.2 80"));

        FX.add(new Fx("freeze", GLFW.GLFW_KEY_N, 5000, LiveFxClient::freeze,
                "playsound minecraft:block.glass.break master @s ~ ~ ~ 1 0.7",
                "effect give @s minecraft:slowness 5 2"));

        FX.add(new Fx("retro", GLFW.GLFW_KEY_V, 6000, LiveFxClient::retro,
                "playsound minecraft:block.note_block.bit master @s ~ ~ ~ 1 1"));

        FX.add(new Fx("matrix", GLFW.GLFW_KEY_B, 6000, LiveFxClient::matrix));
    }

    // ----------------------------------------------------------------
    // Moteur
    // ----------------------------------------------------------------
    static void trigger(Minecraft mc, Fx fx) {
        ACTIVE.removeIf(a -> a.fx == fx);
        ACTIVE.add(new Active(fx, System.currentTimeMillis()));
        LocalPlayer player = mc.player;
        if (RUN_COMMANDS && player != null) {
            for (String command : fx.commands()) {
                player.connection.sendCommand(command);
            }
        }
    }

    static void render(GuiGraphics g) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        ACTIVE.removeIf(a -> now - a.start >= a.fx.durationMs());
        int w = g.guiWidth();
        int h = g.guiHeight();
        for (Active a : new ArrayList<>(ACTIVE)) {
            float t = (now - a.start) / (float) a.fx.durationMs();
            a.fx.painter().paint(g, w, h, t, now);
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    /** Couleur ARGB a partir d'un RGB et d'une opacite 0..1. */
    static int alpha(int rgb, float a) {
        int ai = Math.max(0, Math.min(255, (int) (a * 255f)));
        return (ai << 24) | (rgb & 0xFFFFFF);
    }

    /** Fondu entrant/sortant : monte vite, reste a 1, redescend vite. */
    static float env(float t) {
        return Math.min(1f, Math.min(t, 1f - t) * 8f);
    }

    static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        int i = (int) (h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float u = v * (1f - (1f - f) * s);
        float rr;
        float gg;
        float bb;
        switch (i % 6) {
            case 0 -> { rr = v; gg = u; bb = p; }
            case 1 -> { rr = q; gg = v; bb = p; }
            case 2 -> { rr = p; gg = v; bb = u; }
            case 3 -> { rr = p; gg = q; bb = v; }
            case 4 -> { rr = u; gg = p; bb = v; }
            default -> { rr = v; gg = p; bb = q; }
        }
        return ((int) (rr * 255f) << 16) | ((int) (gg * 255f) << 8) | (int) (bb * 255f);
    }

    /** Texte centre en (cx, cy), agrandi, avec rotation optionnelle (radians). */
    static void bigText(GuiGraphics g, String text, float cx, float cy, float scale, float rot, int rgb, float a) {
        if (a < 0.05f) {
            return; // evite l'opacite quasi nulle (interpretee comme opaque pour le texte)
        }
        Font font = Minecraft.getInstance().font;
        Matrix3x2fStack m = g.pose();
        m.pushMatrix();
        m.translate(cx, cy);
        m.rotate(rot);
        m.scale(scale, scale);
        int tw = font.width(text);
        g.drawString(font, text, -tw / 2, -font.lineHeight / 2, alpha(rgb, a), true);
        m.popMatrix();
    }

    /** Bordure degradee (rectangles empiles) sur les 4 bords. */
    static void edges(GuiGraphics g, int w, int h, int rgb, float strength, int steps, int size) {
        for (int i = 0; i < steps; i++) {
            float k = 1f - i / (float) steps;
            int c = alpha(rgb, k * k * strength);
            g.fill(i * size, 0, (i + 1) * size, h, c);
            g.fill(w - (i + 1) * size, 0, w - i * size, h, c);
            g.fill(0, i * size, w, (i + 1) * size, c);
            g.fill(0, h - (i + 1) * size, w, h - i * size, c);
        }
    }

    // ----------------------------------------------------------------
    // Les effets
    // ----------------------------------------------------------------
    static void flash(GuiGraphics g, int w, int h, float t, long ms) {
        float a = 1f - t;
        g.fill(0, 0, w, h, alpha(0xFFFFFF, a * a * 0.9f));
    }

    static void boom(GuiGraphics g, int w, int h, float t, long ms) {
        g.fill(0, 0, w, h, alpha(0xFF5500, (1f - t) * 0.5f));
        int cx = w / 2;
        int cy = h / 2;
        int max = Math.max(w, h);
        for (int i = 0; i < 3; i++) {
            float tt = t * 1.3f - i * 0.15f;
            if (tt <= 0f || tt >= 1f) {
                continue;
            }
            int r = (int) (tt * max * 0.7f);
            for (int k = 0; k < 3; k++) {
                g.renderOutline(cx - r - k, cy - r - k, (r + k) * 2, (r + k) * 2, alpha(0xFFCC66, 1f - tt));
            }
        }
        bigText(g, "BOOM !", cx, cy, 5f + (1f - t) * 3f, 0f, 0xFFDD00, 1f - t);
    }

    static void rainbow(GuiGraphics g, int w, int h, float t, long ms) {
        float e = env(t);
        for (int y = 0; y < h; y += 6) {
            float hue = (y / (float) h) + ms / 1500f;
            g.fill(0, y, w, y + 6, alpha(hsv(hue, 0.9f, 1f), 0.35f * e));
        }
    }

    static void glitch(GuiGraphics g, int w, int h, float t, long ms) {
        Random r = new Random(ms / 70);
        float fade = 1f - t;
        g.fill(0, 0, w, h, alpha(0x00FFFF, 0.06f * fade));
        for (int i = 0; i < 14; i++) {
            int y = r.nextInt(h);
            int bh = 3 + r.nextInt(Math.max(4, h / 14));
            int x = r.nextInt(Math.max(1, w / 3));
            int col = r.nextBoolean() ? 0xFF00FF : 0x00FFFF;
            g.fill(x, y, w - r.nextInt(Math.max(1, w / 3)), y + bh, alpha(col, 0.35f * fade));
        }
        if (r.nextInt(3) == 0) {
            int y = r.nextInt(h);
            g.fill(0, y, w, y + 2, alpha(0xFFFFFF, 0.6f * fade));
        }
    }

    static void danger(GuiGraphics g, int w, int h, float t, long ms) {
        float e = env(t);
        float pulse = 0.55f + 0.45f * (float) Math.sin(ms / 110.0);
        edges(g, w, h, 0xFF0000, 0.7f * pulse * e, 22, Math.max(4, w / 90));
        bigText(g, "DANGER", w / 2f, h / 6f, 4f + pulse, 0f, 0xFF3030, e);
    }

    static void popup(GuiGraphics g, int w, int h, float t, long ms) {
        float k = Math.min(1f, t / 0.15f);
        float scale = 9f * (k + 0.25f * (float) Math.sin(k * Math.PI));
        float a = t > 0.8f ? (1f - t) / 0.2f : 1f;
        float rot = (float) Math.sin(ms / 180.0) * 0.08f;
        bigText(g, POPUP_TEXT, w / 2f, h / 2f, scale, rot, hsv(ms / 600f, 0.8f, 1f), a);
    }

    static void confetti(GuiGraphics g, int w, int h, float t, long ms) {
        float sec = t * 5f;
        for (int i = 0; i < 160; i++) {
            Random r = new Random(i * 7919L + 13);
            int x0 = r.nextInt(Math.max(1, w));
            float speed = h * (0.18f + r.nextFloat() * 0.25f);
            float sway = 10 + r.nextInt(25);
            int size = 3 + r.nextInt(4);
            int col = hsv(r.nextFloat(), 0.85f, 1f);
            float delay = r.nextFloat() * 1.2f;
            float s = sec - delay;
            if (s < 0f) {
                continue;
            }
            int y = (int) (-10 + s * speed);
            if (y > h) {
                continue;
            }
            int x = x0 + (int) (Math.sin(s * 3 + i) * sway);
            g.fill(x, y, x + size, y + size + 2, alpha(col, 1f));
        }
    }

    static void freeze(GuiGraphics g, int w, int h, float t, long ms) {
        float e = env(t);
        g.fill(0, 0, w, h, alpha(0x66CCFF, 0.22f * e));
        edges(g, w, h, 0xE8F8FF, 0.6f * e, 18, Math.max(4, w / 100));
    }

    static void retro(GuiGraphics g, int w, int h, float t, long ms) {
        float e = env(t);
        g.fill(0, 0, w, h, alpha(0x00FFAA, 0.08f * e));
        for (int y = 0; y < h; y += 3) {
            g.fill(0, y, w, y + 1, alpha(0x000000, 0.35f * e));
        }
        int bar = (int) ((ms / 6) % (h + 60)) - 30;
        g.fillGradient(0, bar, w, bar + 30, alpha(0xFFFFFF, 0f), alpha(0xFFFFFF, 0.12f * e));
    }

    static void matrix(GuiGraphics g, int w, int h, float t, long ms) {
        float e = env(t);
        g.fill(0, 0, w, h, alpha(0x001400, 0.45f * e));
        if (e < 0.1f) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String chars = "01ABCDEFGHJKLMNPRSTUVWXYZ";
        int step = 14;
        int cols = w / step;
        for (int c = 0; c < cols; c++) {
            Random r = new Random(c * 104729L);
            float speed = 60 + r.nextInt(160);
            int offset = r.nextInt(Math.max(1, h));
            int head = (int) ((ms / 1000f * speed + offset) % (h + 120));
            for (int k = 0; k < 8; k++) {
                int y = head - k * 10;
                if (y < 0 || y > h) {
                    continue;
                }
                int idx = (int) ((c * 31L + k * 17L + ms / 90) % chars.length());
                String ch = String.valueOf(chars.charAt(idx));
                int col = k == 0 ? alpha(0xCCFFCC, e) : alpha(0x00FF55, (1f - k / 8f) * e);
                g.drawString(font, ch, c * step, y, col, false);
            }
        }
    }
}
