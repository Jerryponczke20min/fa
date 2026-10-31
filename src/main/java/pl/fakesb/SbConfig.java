package pl.fakesb;

import java.util.Arrays;
import java.util.Locale;

/** Ustawienia moda (zapisywane do config/fakesb.json). */
public class SbConfig {
    public static final int TITLE_MAX = 32;
    /** Ikony przed liniami: olowek, flaga, serce, zegar, dolar, miecze, czaszka (znaki Unicode). */
    public static final String[] ICONS = {"\u270E", "\u2691", "\u2764", "\u231A", "$", "\u2694", "\u2620"};

    public boolean enabled = true;   // wlacznik / wylacznik paska

    // Teksty scoreboardu
    public String title = "MOJ SERWER";
    public String nick = "Gracz";
    public String clan = "-";
    public String hearts = "20";
    public String playtime = "0d 0h";
    public String kills = "0";
    public String deaths = "0";
    public String channel = "SPAWN01";
    public double money = 1000.0;

    // Kolory (RGB) - kazdy ustawiany suwakami w zakladce Kolory
    public int[] titleColors = filled(TITLE_MAX, 0xFFFFFF);   // osobny kolor kazdej literki tytulu
    // linie: Gracz, Klan, Serca, Czas gry, Stan konta, Zabojstwa, Smierci
    public int[] iconColors  = {0x55FFFF, 0xFFAA00, 0xFF5555, 0xFFFF55, 0x55FF55, 0xFF5555, 0xAAAAAA};
    public int[] labelColors = {0xFFFFFF, 0xFFFFFF, 0xFF5555, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xAAAAAA};
    public int[] valueColors = {0x55FFFF, 0xFFAA00, 0xFF5555, 0xFFFF55, 0x55FF55, 0xFF5555, 0xAAAAAA};
    public int[] footerColors = {0xFFFFFF, 0x55FFFF, 0xAAAAAA}; // "Jestes na" / kanal / "(/ch)"

    // Wyglad
    public float scale = 1.0f;
    public float alpha = 0.15f;       // przezroczystosc tla
    public float x = 0.78f;           // pozycja jako ulamek ekranu
    public float y = 0.35f;

    // Czek
    public long chequeAmount = 1000;
    public String chequeCreator = "Gracz";
    public String chequeDate = "";    // puste = aktualna data

    public String moneyText() {
        return String.format(Locale.US, "%.2f$", money);
    }

    private static int[] filled(int n, int v) {
        int[] a = new int[n];
        Arrays.fill(a, v);
        return a;
    }
}
