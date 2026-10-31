package pl.fakesb;

import java.util.Locale;

public class SbConfig {
    // Scoreboard
    public String title = "MOJ SERWER";
    public String nick = "Gracz";
    public String clan = "-";
    public String hearts = "20";
    public String playtime = "0d 0h";
    public String kills = "0";
    public String deaths = "0";
    public double money = 1000.0;

    // Wyglad
    public int titleColor = 0xFFFFFF;
    // kolory per linia: Gracz, Klan, Serca, Czas gry, Stan konta, Zabojstwa, Smierci
    public int[] labelColors = {0x55FFFF, 0xFFAA00, 0xFF5555, 0xFFFF55, 0x55FF55, 0xFF5555, 0xAAAAAA};
    public int[] valueColors = {0x55FFFF, 0xFFAA00, 0xFF5555, 0xFFFF55, 0x55FF55, 0xFF5555, 0xAAAAAA};
    public float scale = 1.0f;
    public float alpha = 0.35f;       // przezroczystosc tla
    public float x = 0.78f;           // pozycja jako ulamek ekranu
    public float y = 0.35f;

    // Czek
    public long chequeAmount = 1000;
    public String chequeCreator = "Gracz";
    public String chequeDate = "";    // puste = aktualna data

    public String moneyText() {
        return String.format(Locale.US, "%.2f$", money);
    }
}
