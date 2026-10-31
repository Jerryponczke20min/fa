package pl.fakesb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SbHud {
    private static final int ICON_COL = 11;   // szerokosc kolumny z ikona

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        SbConfig c = FakeSbClient.CFG;
        if (!c.enabled) return;
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
        String f1 = "Jesteś na ", f2 = c.channel, f3 = " (/ch)";

        // Szerokosc calego bloku = najszersza linia
        int titleLen = Math.min(c.title.length(), SbConfig.TITLE_MAX);
        int titleW = 0;
        for (int i = 0; i < titleLen; i++) titleW += tr.getWidth(String.valueOf(c.title.charAt(i)));
        int w = Math.max(titleW, tr.getWidth(f1 + f2 + f3));
        for (String[] r : rows) w = Math.max(w, ICON_COL + tr.getWidth(r[0] + r[1]));

        float px = c.x * ctx.getScaledWindowWidth();
        float py = c.y * ctx.getScaledWindowHeight();

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(px, py, 0);
        m.scale(c.scale, c.scale, 1f);

        int a = (int) (c.alpha * 255) & 0xFF;
        if (a > 0) ctx.fill(-3, -3, w + 3, (rows.length + 2) * 10 + 1, a << 24);

        drawBanner(ctx, tr, w);

        // Tytul wysrodkowany, kazda literka we wlasnym kolorze
        int x = (w - titleW) / 2;
        for (int i = 0; i < titleLen; i++) {
            String ch = String.valueOf(c.title.charAt(i));
            ctx.drawText(tr, ch, x, 0, 0xFF000000 | c.titleColors[i], true);
            x += tr.getWidth(ch);
        }

        // Linie: ikona | nazwa | wartosc
        for (int i = 0; i < rows.length; i++) {
            int y = (i + 1) * 10;
            ctx.drawText(tr, SbConfig.ICONS[i], 0, y, 0xFF000000 | c.iconColors[i], true);
            ctx.drawText(tr, rows[i][0], ICON_COL, y, 0xFF000000 | c.labelColors[i], true);
            ctx.drawText(tr, rows[i][1], ICON_COL + tr.getWidth(rows[i][0]), y, 0xFF000000 | c.valueColors[i], true);
        }

        // Stopka: "Jestes na KANAL (/ch)"
        int fy = (rows.length + 1) * 10;
        int fx = 0;
        ctx.drawText(tr, f1, fx, fy, 0xFF000000 | c.footerColors[0], true);
        fx += tr.getWidth(f1);
        ctx.drawText(tr, f2, fx, fy, 0xFF000000 | c.footerColors[1], true);
        fx += tr.getWidth(f2);
        ctx.drawText(tr, f3, fx, fy, 0xFF000000 | c.footerColors[2], true);

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
