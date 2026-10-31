package pl.fakesb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;

/**
 * GUI w stylu granatowo-wisniowym. Zakladki: 0 = Scoreboard, 1 = Czek, 2 = Kolory.
 * Wszystkie widgety (przyciski, suwaki, pola) maja wlasne rysowanie.
 */
public class SbScreen extends Screen {
    // Paleta GUI
    private static final int CHERRY = 0xFFB3204A, CHERRY_LIGHT = 0xFFD63A66, CHERRY_DARK = 0xFF5E1230;
    private static final int NAVY_LINE = 0xFF34417F, NAVY_BTN = 0xFF1B2658, NAVY_HOVER = 0xFF2C3A7A, NAVY_FIELD = 0xFF0A0E26;
    private static final int TEXT = 0xFFE8ECFF, MUTED = 0xFF9AA3C7;
    // 16 kolorow Minecrafta jako szybka paleta
    private static final int[] PALETTE = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};
    private static final String[] ROWS = {"Tytuł", "Gracz", "Klan", "Serca", "Czas gry", "Stan konta", "Zabójstwa", "Śmierci"};

    private int tab = 0;
    private int target = 0;            // edytowany element w zakladce Kolory
    private String currentName = "";
    private final SbConfig c = FakeSbClient.CFG;
    private final Map<String, TextFieldWidget> fields = new LinkedHashMap<>();
    private final List<Object[]> labels = new ArrayList<>();  // {tekst, x, y}
    private final List<int[]> boxes = new ArrayList<>();      // ramki pod polami tekstowymi {x, y, w, h}
    private String status = "";

    public SbScreen() {
        super(Text.literal("Custom SB"));
    }

    @Override
    public boolean shouldPause() {
        return false;   // gra nie jest zatrzymywana, zeby czek pojawil sie od razu
    }

    @Override
    protected void init() {
        fields.clear();
        labels.clear();
        boxes.clear();
        int cx = width / 2, left = cx - 150;

        FB t0 = new FB(left, 12, 96, 20, "Scoreboard", b -> { apply(); tab = 0; clearAndInit(); });
        FB t2 = new FB(left + 102, 12, 96, 20, "Kolory", b -> { apply(); tab = 2; clearAndInit(); });
        FB t1 = new FB(left + 204, 12, 96, 20, "Czek", b -> { apply(); tab = 1; clearAndInit(); });
        t0.selected = tab == 0; t2.selected = tab == 2; t1.selected = tab == 1;
        addDrawableChild(t0); addDrawableChild(t2); addDrawableChild(t1);

        if (tab == 0) initSb(left, cx);
        else if (tab == 1) initCheque(left);
        else initColors(left);

        addDrawableChild(new FB(cx - 70, height - 28, 140, 20, "Zapisz i zamknij", b -> { apply(); close(); }));
    }

    // ---------------- Tlo i panel ----------------

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        super.renderBackground(ctx, mx, my, delta);
        int px = width / 2 - 170, pw = 340, py = 4, ph = height - 8;
        ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, CHERRY);                    // ramka
        ctx.fillGradient(px, py, px + pw, py + ph, 0xF00B1233, 0xF02A0F2C);            // granat -> wisnia
        ctx.fillGradient(px, py, px + pw, py + 38, 0x40D63A66, 0x00000000);            // poswiata pod zakladkami
        for (int[] b : boxes) {
            ctx.fill(b[0] - 1, b[1] - 1, b[0] + b[2] + 1, b[1] + b[3] + 1, CHERRY_DARK);
            ctx.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], NAVY_FIELD);
        }
    }

    // ---------------- Zakladka Scoreboard ----------------

    private void initSb(int left, int cx) {
        int y = 42;
        field("title", "Tytuł", c.title, left, y); y += 19;
        field("nick", "Nick", c.nick, left, y); y += 19;
        field("clan", "Klan", c.clan, left, y); y += 19;
        field("hearts", "Serca", c.hearts, left, y); y += 19;
        field("playtime", "Czas gry", c.playtime, left, y); y += 19;
        field("money", "Stan konta", String.format(Locale.US, "%.2f", c.money), left, y); y += 19;
        field("kills", "Zabójstwa", c.kills, left, y); y += 19;
        field("deaths", "Śmierci", c.deaths, left, y); y += 19;
        field("channel", "Kanał", c.channel, left, y);

        int sx = cx + 10, sy = 42, sw = 140;
        addDrawableChild(new Sl(sx, sy, sw, "Rozmiar", 0.5, 3.0, c.scale, CHERRY_LIGHT, v -> c.scale = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Przezr. tła", 0, 1, c.alpha, CHERRY_LIGHT, v -> c.alpha = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Pozycja X", 0, 1, c.x, CHERRY_LIGHT, v -> c.x = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Pozycja Y", 0, 1, c.y, CHERRY_LIGHT, v -> c.y = (float) v)); sy += 28;
        addDrawableChild(new FB(sx, sy, sw, 20, c.enabled ? "SB: WŁĄCZONY" : "SB: WYŁĄCZONY", b -> {
            c.enabled = !c.enabled;
            b.setMessage(Text.literal(c.enabled ? "SB: WŁĄCZONY" : "SB: WYŁĄCZONY"));
        }));
    }

    // ---------------- Zakladka Kolory ----------------
    // Strzalkami < > wybierasz element: litery tytulu, potem ikona/nazwa/wartosc kazdej linii, na koncu stopka.

    private int titleLen() { return Math.min(c.title.length(), SbConfig.TITLE_MAX); }

    private int targetCount() { return titleLen() + 21 + 3; }

    private int[] arrOf(int t) {
        int L = titleLen();
        if (t < L) return c.titleColors;
        int k = t - L;
        if (k < 21) { int part = k % 3; return part == 0 ? c.iconColors : part == 1 ? c.labelColors : c.valueColors; }
        return c.footerColors;
    }

    private int idxOf(int t) {
        int L = titleLen();
        if (t < L) return t;
        int k = t - L;
        return k < 21 ? k / 3 : k - 21;
    }

    private String nameOf(int t) {
        int L = titleLen();
        if (t < L) return "Tytuł: litera " + (t + 1) + " \"" + c.title.charAt(t) + "\"";
        int k = t - L;
        if (k < 21) return ROWS[k / 3 + 1] + ": " + new String[]{"ikona", "nazwa", "wartość"}[k % 3];
        return new String[]{"Stopka: \"Jesteś na\"", "Stopka: kanał", "Stopka: \"(/ch)\""}[k - 21];
    }

    private void initColors(int left) {
        int count = targetCount();
        if (target >= count) target = 0;
        currentName = nameOf(target);

        addDrawableChild(new FB(left, 46, 30, 20, "<", b -> { target = (target + count - 1) % count; clearAndInit(); }));
        addDrawableChild(new FB(left + 270, 46, 30, 20, ">", b -> { target = (target + 1) % count; clearAndInit(); }));

        final int[] arr = arrOf(target);
        final int idx = idxOf(target);
        int cur = arr[idx];
        addDrawableChild(new Sl(left, 80, 300, "R", 0, 255, (cur >> 16) & 255, 0xFFFF5555,
                v -> arr[idx] = (arr[idx] & 0x00FFFF) | ((int) Math.round(v) << 16)));
        addDrawableChild(new Sl(left, 102, 300, "G", 0, 255, (cur >> 8) & 255, 0xFF55FF55,
                v -> arr[idx] = (arr[idx] & 0xFF00FF) | ((int) Math.round(v) << 8)));
        addDrawableChild(new Sl(left, 124, 300, "B", 0, 255, cur & 255, 0xFF5599FF,
                v -> arr[idx] = (arr[idx] & 0xFFFF00) | (int) Math.round(v)));

        // Szybka paleta - klik ustawia kolor
        for (int i = 0; i < PALETTE.length; i++) {
            final int col = PALETTE[i];
            addDrawableChild(new Swatch(left + 7 + i * 18, 150, col, b -> { arr[idx] = col; clearAndInit(); }));
        }

        if (target < titleLen()) {
            addDrawableChild(new FB(left, 196, 148, 20, "Cały tytuł = ten kolor", b -> {
                int col = c.titleColors[idx];
                for (int i = 0; i < titleLen(); i++) c.titleColors[i] = col;
                clearAndInit();
            }));
            addDrawableChild(new FB(left + 152, 196, 148, 20, "Tęcza w tytule", b -> {
                int L = Math.max(1, titleLen());
                for (int i = 0; i < titleLen(); i++)
                    c.titleColors[i] = MathHelper.hsvToRgb(i / (float) L, 1f, 1f) & 0xFFFFFF;
                clearAndInit();
            }));
        }
    }

    /** Nazwa edytowanego elementu, pasek aktualnego koloru i podglad tytulu z zaznaczona litera. */
    private void renderColorPreview(DrawContext ctx) {
        int cx = width / 2, left = cx - 150;
        ctx.drawCenteredTextWithShadow(textRenderer, currentName, cx, 52, TEXT);
        int col = arrOf(target)[idxOf(target)];
        ctx.fill(left + 34, 70, left + 266, 74, 0xFF000000 | col);

        int L = titleLen(), tw = 0;
        for (int i = 0; i < L; i++) tw += textRenderer.getWidth(String.valueOf(c.title.charAt(i)));
        int x = cx - tw / 2;
        for (int i = 0; i < L; i++) {
            String ch = String.valueOf(c.title.charAt(i));
            int cw = textRenderer.getWidth(ch);
            ctx.drawText(textRenderer, ch, x, 174, 0xFF000000 | c.titleColors[i], true);
            if (target == i) ctx.fill(x, 184, x + cw, 185, CHERRY_LIGHT);
            x += cw;
        }
    }

    // ---------------- Zakladka Czek ----------------

    private void initCheque(int left) {
        int y = 46;
        field("amount", "Kwota", String.valueOf(c.chequeAmount), left + 100, y); y += 26;
        field("creator", "Twórca", c.chequeCreator, left + 100, y); y += 26;
        field("date", "Data", c.chequeDate, left + 100, y); y += 30;
        labels.add(new Object[]{"Kwota min. 1000 (np. 1k). Data pusta = aktualna.", left, y - 4});
        y += 14;
        addDrawableChild(new FB(left, y, 300, 22, "Utwórz czek z przedmiotu w ręce", b -> {
            apply();
            long amt = Cheques.parseAmount(get("amount"));
            String err = Cheques.create(MinecraftClient.getInstance(), amt);
            status = err != null ? err : "Czek utworzony: $" + amt;
        }));
        labels.add(new Object[]{"Szybko z czatu:  .czek 5k", left, y + 32});
    }

    // ---------------- Wspolne ----------------

    private void field(String key, String label, String val, int x, int y) {
        int bx = (tab == 0) ? x + 70 : x;
        int bw = (tab == 0) ? 80 : 120;
        boxes.add(new int[]{bx, y, bw, 18});
        TextFieldWidget t = new TextFieldWidget(textRenderer, bx + 4, y + 5, bw - 8, 12, Text.literal(label));
        t.setDrawsBackground(false);
        t.setEditableColor(TEXT);
        t.setMaxLength(64);
        t.setText(val);
        addDrawableChild(t);
        fields.put(key, t);
        labels.add(new Object[]{label, tab == 0 ? x : x - 100, y + 5});
    }

    private String get(String k) { return fields.containsKey(k) ? fields.get(k).getText() : null; }

    /** Przepisuje pola tekstowe do configu i zapisuje plik. */
    private void apply() {
        if (get("title") != null) {
            c.title = get("title");
            if (c.title.length() > SbConfig.TITLE_MAX) c.title = c.title.substring(0, SbConfig.TITLE_MAX);
        }
        if (get("nick") != null) c.nick = get("nick");
        if (get("clan") != null) c.clan = get("clan");
        if (get("hearts") != null) c.hearts = get("hearts");
        if (get("playtime") != null) c.playtime = get("playtime");
        if (get("kills") != null) c.kills = get("kills");
        if (get("deaths") != null) c.deaths = get("deaths");
        if (get("channel") != null) c.channel = get("channel");
        if (get("money") != null) {
            try { c.money = Double.parseDouble(get("money").replace(',', '.')); } catch (Exception ignored) {}
        }
        if (get("creator") != null) c.chequeCreator = get("creator");
        if (get("date") != null) c.chequeDate = get("date");
        if (get("amount") != null) {
            long a = Cheques.parseAmount(get("amount"));
            if (a > 0) c.chequeAmount = a;
        }
        FakeSbClient.save();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        for (Object[] l : labels)
            ctx.drawText(textRenderer, (String) l[0], (int) l[1], (int) l[2], MUTED, true);
        if (tab == 2) renderColorPreview(ctx);
        if (!status.isEmpty())
            ctx.drawCenteredTextWithShadow(textRenderer, status, width / 2, height - 42, 0xFFFFD866);
    }

    @Override
    public void close() {
        apply();
        super.close();
    }

    // ---------------- Wlasne widgety ----------------

    /** Przycisk w stylu granat/wisnia; "selected" podswietla aktywna zakladke. */
    private static class FB extends ButtonWidget {
        boolean selected;

        FB(int x, int y, int w, int h, String text, ButtonWidget.PressAction action) {
            super(x, y, w, h, Text.literal(text), action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean on = selected || isHovered();
            ctx.fill(x, y, x + w, y + h, on ? CHERRY_LIGHT : NAVY_LINE);
            ctx.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1,
                    selected ? CHERRY : isHovered() ? NAVY_HOVER : NAVY_BTN,
                    selected ? CHERRY_DARK : NAVY_FIELD);
            ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                    x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }

    /** Kwadracik z kolorem z palety. */
    private static class Swatch extends ButtonWidget {
        private final int color;

        Swatch(int x, int y, int color, ButtonWidget.PressAction action) {
            super(x, y, 16, 16, Text.empty(), action, DEFAULT_NARRATION_SUPPLIER);
            this.color = color;
        }

        @Override
        public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY();
            ctx.fill(x - 1, y - 1, x + 17, y + 17, isHovered() ? 0xFFFFFFFF : NAVY_LINE);
            ctx.fill(x, y, x + 16, y + 16, 0xFF000000 | color);
        }
    }

    /** Suwak z wypelnieniem w kolorze akcentu. */
    private static class Sl extends SliderWidget {
        private final String name;
        private final double min, max;
        private final int accent;
        private final DoubleConsumer cb;

        Sl(int x, int y, int w, String name, double min, double max, double cur, int accent, DoubleConsumer cb) {
            super(x, y, w, 20, Text.empty(), (cur - min) / (max - min));
            this.name = name; this.min = min; this.max = max; this.accent = accent; this.cb = cb;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            String fmt = max > 10 ? "%.0f" : "%.2f";
            setMessage(Text.literal(name + ": " + String.format(Locale.US, fmt, min + value * (max - min))));
        }

        @Override
        protected void applyValue() {
            cb.accept(min + value * (max - min));
        }

        @Override
        public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            ctx.fill(x, y, x + w, y + h, isHovered() ? CHERRY_LIGHT : NAVY_LINE);
            ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, NAVY_FIELD);
            int fillW = (int) ((w - 2) * value);
            ctx.fillGradient(x + 1, y + 1, x + 1 + fillW, y + h - 1, accent, 0xFF000000 | ((accent & 0xFEFEFE) >> 1));   // gora jasniej, dol ciemniej
            ctx.fill(x + 1 + fillW - 1, y, x + 1 + fillW + 1, y + h, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                    x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }
}
