package pl.fakesb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SbHud {
    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        SbConfig c = FakeSbClient.CFG;
        TextRenderer tr = mc.textRenderer;

        String[][] rows = {
                {"Gracz: ", c.nick},
                {"Klan: ", c.clan},
                {"Serca: ", c.hearts},
                {"Czas gry: ", c.playtime},
                {"Stan konta: ", c.moneyText()},
                {"Zabójstwa: ", c.kills},
                {"Śmierci: ", c.deaths}
        };

        int w = tr.getWidth(c.title);
        for (String[] r : rows) w = Math.max(w, tr.getWidth(r[0] + r[1]));

        float px = c.x * ctx.getScaledWindowWidth();
        float py = c.y * ctx.getScaledWindowHeight();

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(px, py, 0);
        m.scale(c.scale, c.scale, 1f);

        int bg = ((int) (c.alpha * 255) & 0xFF) << 24;
        ctx.fill(-3, -3, w + 3, (rows.length + 1) * 10 + 1, bg);

        drawBanner(ctx, tr, w);

        ctx.drawText(tr, c.title, (w - tr.getWidth(c.title)) / 2, 0, 0xFF000000 | c.titleColor, true);
        for (int i = 0; i < rows.length; i++) {
            int y = (i + 1) * 10;
            ctx.drawText(tr, rows[i][0], 0, y, 0xFF000000 | c.labelColors[i], true);
            ctx.drawText(tr, rows[i][1], tr.getWidth(rows[i][0]), y, 0xFF000000 | c.valueColors[i], true);
        }
        m.pop();
    }

    /** Napis "FAKE DAILY LIVE" nad SB - kazda litera plynnie zmienia kolor (teczowa animacja). */
    private static void drawBanner(DrawContext ctx, TextRenderer tr, int panelWidth) {
        final String tag = "FAKE DAILY LIVE";
        long t = System.currentTimeMillis();
        int tw = tr.getWidth(tag);

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(panelWidth / 2f, -20, 0);   // wysrodkowany nad panelem
        m.scale(1.5f, 1.5f, 1f);                // wiekszy, zeby bylo jak najlepiej widac
        ctx.fill(-tw / 2 - 3, -2, tw / 2 + 3, 10, 0xCC000000);

        int x = -tw / 2;
        for (int i = 0; i < tag.length(); i++) {
            String ch = String.valueOf(tag.charAt(i));
            float hue = ((t / 8 + i * 25L) % 360) / 360f;   // przesuniecie koloru w czasie i po literach
            int rgb = MathHelper.hsvToRgb(hue, 1f, 1f);
            ctx.drawText(tr, ch, x, 0, 0xFF000000 | (rgb & 0xFFFFFF), true);
            x += tr.getWidth(ch);
        }
        m.pop();
    }
}
