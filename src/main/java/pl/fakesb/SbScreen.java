package pl.fakesb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;

public class SbScreen extends Screen {
    private int tab = 0;
    private final SbConfig c = FakeSbClient.CFG;
    private final Map<String, TextFieldWidget> fields = new LinkedHashMap<>();
    private final List<Object[]> labels = new ArrayList<>(); // {text, x, y}
    private String status = "";

    public SbScreen() {
        super(Text.literal("Custom SB"));
    }

    @Override
    protected void init() {
        fields.clear();
        labels.clear();
        int cx = width / 2, left = cx - 150;

        addDrawableChild(ButtonWidget.builder(Text.literal("Scoreboard"), b -> { apply(); tab = 0; clearAndInit(); })
                .dimensions(left, 10, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Kolory"), b -> { apply(); tab = 2; clearAndInit(); })
                .dimensions(left + 102, 10, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Czek"), b -> { apply(); tab = 1; clearAndInit(); })
                .dimensions(left + 204, 10, 96, 20).build());

        if (tab == 0) initSb(left, cx);
        else if (tab == 1) initCheque(left);
        else initColors(left);

        addDrawableChild(ButtonWidget.builder(Text.literal("Zapisz i zamknij"), b -> { apply(); close(); })
                .dimensions(cx - 60, height - 30, 120, 20).build());
    }

    private void initSb(int left, int cx) {
        int y = 45;
        field("title", "Tytuł", c.title, left, y); y += 22;
        field("nick", "Nick", c.nick, left, y); y += 22;
        field("clan", "Klan", c.clan, left, y); y += 22;
        field("hearts", "Serca", c.hearts, left, y); y += 22;
        field("playtime", "Czas gry", c.playtime, left, y); y += 22;
        field("money", "Stan konta", String.format(Locale.US, "%.2f", c.money), left, y); y += 22;
        field("kills", "Zabójstwa", c.kills, left, y); y += 22;
        field("deaths", "Śmierci", c.deaths, left, y);

        int sx = cx + 10, sy = 45, sw = 140;
        addDrawableChild(new Sl(sx, sy, sw, "Rozmiar", 0.5, 3.0, c.scale, v -> c.scale = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Przezr. tła", 0, 1, c.alpha, v -> c.alpha = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Pozycja X", 0, 1, c.x, v -> c.x = (float) v)); sy += 22;
        addDrawableChild(new Sl(sx, sy, sw, "Pozycja Y", 0, 1, c.y, v -> c.y = (float) v));
    }

    private static final String[] ROWS = {"Tytuł", "Gracz", "Klan", "Serca", "Czas gry", "Stan konta", "Zabójstwa", "Śmierci"};

    private void initColors(int left) {
        labels.add(new Object[]{"Napis", left, 38});
        labels.add(new Object[]{"Kolor nazwy (HEX)", left + 75, 38});
        labels.add(new Object[]{"Kolor wartości (HEX)", left + 190, 38});
        for (int i = 0; i < ROWS.length; i++) {
            int y = 50 + i * 21;
            labels.add(new Object[]{ROWS[i], left, y + 5});
            colorField("lc" + i, i == 0 ? c.titleColor : c.labelColors[i - 1], left + 75, y);
            if (i > 0) colorField("vc" + i, c.valueColors[i - 1], left + 190, y);
        }
    }

    private void colorField(String key, int rgb, int x, int y) {
        TextFieldWidget t = new TextFieldWidget(textRenderer, x, y, 80, 18, Text.literal(key));
        t.setMaxLength(7);
        t.setText(String.format("%06X", rgb & 0xFFFFFF));
        addDrawableChild(t);
        fields.put(key, t);
    }

    private static int hex(String s, int fallback) {
        try { return Integer.parseInt(s.replace("#", "").trim(), 16) & 0xFFFFFF; }
        catch (Exception e) { return fallback; }
    }

    private void initCheque(int left) {
        int y = 50;
        field("amount", "Kwota (min 1000, np. 1k)", String.valueOf(c.chequeAmount), left + 100, y); y += 26;
        field("creator", "Twórca", c.chequeCreator, left + 100, y); y += 26;
        field("date", "Data (puste = teraz)", c.chequeDate, left + 100, y); y += 34;
        addDrawableChild(ButtonWidget.builder(Text.literal("Utwórz czek z przedmiotu w ręce"), b -> createCheque())
                .dimensions(left, y, 300, 20).build());
    }

    private void field(String key, String label, String val, int x, int y) {
        int fx = (tab == 0) ? x + 70 : x;
        int fw = (tab == 0) ? 75 : 120;
        TextFieldWidget t = new TextFieldWidget(textRenderer, fx, y, fw, 18, Text.literal(label));
        t.setMaxLength(64);
        t.setText(val);
        addDrawableChild(t);
        fields.put(key, t);
        labels.add(new Object[]{label, tab == 0 ? x : x - 100, y + 5});
    }

    private String get(String k) { return fields.containsKey(k) ? fields.get(k).getText() : null; }

    /** Przepisuje pola tekstowe do configu i zapisuje plik. */
    private void apply() {
        if (get("title") != null) c.title = get("title");
        if (get("nick") != null) c.nick = get("nick");
        if (get("clan") != null) c.clan = get("clan");
        if (get("hearts") != null) c.hearts = get("hearts");
        if (get("playtime") != null) c.playtime = get("playtime");
        if (get("kills") != null) c.kills = get("kills");
        if (get("deaths") != null) c.deaths = get("deaths");
        if (get("money") != null) {
            try { c.money = Double.parseDouble(get("money").replace(',', '.')); } catch (Exception ignored) {}
        }
        if (get("creator") != null) c.chequeCreator = get("creator");
        if (get("date") != null) c.chequeDate = get("date");
        if (get("amount") != null) {
            long a = parseAmount(get("amount"));
            if (a > 0) c.chequeAmount = a;
        }
        if (get("lc0") != null) c.titleColor = hex(get("lc0"), c.titleColor);
        for (int i = 1; i <= 7; i++) {
            if (get("lc" + i) != null) c.labelColors[i - 1] = hex(get("lc" + i), c.labelColors[i - 1]);
            if (get("vc" + i) != null) c.valueColors[i - 1] = hex(get("vc" + i), c.valueColors[i - 1]);
        }
        FakeSbClient.save();
    }

    private static long parseAmount(String s) {
        try {
            s = s.trim().toLowerCase(Locale.ROOT).replace(" ", "");
            if (s.endsWith("k")) return (long) (Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000);
            if (s.endsWith("m")) return (long) (Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000_000);
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1;
        }
    }

    private void createCheque() {
        apply();
        MinecraftClient mc = MinecraftClient.getInstance();
        long amt = parseAmount(get("amount"));
        if (amt < 1000) { status = "Minimalna kwota to 1000"; return; }
        if (c.money < amt) { status = "Za mało środków na koncie"; return; }
        MinecraftServer srv = mc.getServer();
        if (srv == null || mc.player == null) { status = "Tylko w singleplayerze"; return; }

        ItemStack held = mc.player.getMainHandStack();
        Item item = held.isEmpty() ? Items.PAPER : held.getItem();
        String date = c.chequeDate.isBlank()
                ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss"))
                : c.chequeDate;
        ItemStack cheque = Cheques.make(item, amt, c.chequeCreator, date);

        c.money -= amt;
        FakeSbClient.save();
        var uuid = mc.player.getUuid();
        srv.execute(() -> {
            ServerPlayerEntity sp = srv.getPlayerManager().getPlayer(uuid);
            if (sp != null) sp.giveItemStack(cheque);
        });
        status = "Czek utworzony: $" + amt;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        for (Object[] l : labels)
            ctx.drawText(textRenderer, (String) l[0], (int) l[1], (int) l[2], 0xFFFFFFFF, true);
        if (!status.isEmpty())
            ctx.drawCenteredTextWithShadow(textRenderer, status, width / 2, height - 45, 0xFFFFFF55);
    }

    @Override
    public void close() {
        apply();
        super.close();
    }

    private static class Sl extends SliderWidget {
        private final String name;
        private final double min, max;
        private final DoubleConsumer cb;

        Sl(int x, int y, int w, String name, double min, double max, double cur, DoubleConsumer cb) {
            super(x, y, w, 20, Text.empty(), (cur - min) / (max - min));
            this.name = name; this.min = min; this.max = max; this.cb = cb;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(name + ": " + String.format(Locale.US, "%.2f", min + value * (max - min))));
        }

        @Override
        protected void applyValue() {
            cb.accept(min + value * (max - min));
        }
    }
}
