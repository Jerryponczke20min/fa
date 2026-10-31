# Custom SB & Cheques (demo mod)

Klientowy mod Fabric (Minecraft 1.21.4): konfigurowalny scoreboard HUD i czeki z dowolnego przedmiotu.

**Dziala tylko w singleplayerze / na wlasnym swiecie.** Na zewnetrznych serwerach HUD i czeki sa wylaczone
(`FakeSbClient.allowed()`).

## Funkcje
- GUI otwierane klawiszem (domyslnie `F`, zmienisz w Sterowanie -> "Custom SB (demo)")
- Zakladka **Scoreboard**: nick, klan, serca, czas gry, stan konta, zabojstwa, smierci, tytul + suwaki: rozmiar,
  przezroczystosc tla, pozycja X/Y
- Zakladka **Kolory**: osobny kolor (HEX, np. `55FFFF`) dla kazdej linii - osobno nazwa i wartosc, plus tytul
- Zakladka **Czek**: kwota (min 1000, mozna `1k`), tworca, data; tworzy czek z przedmiotu trzymanego w rece
  (pusta reka = papier). Utworzenie czeku odejmuje kwote ze stanu konta, uzycie (prawy klik) dodaje ja z powrotem
  i pokazuje komunikat "Na twoje konto zostalo dodane X$"
- Nad SB stale wyswietla sie animowany, teczowy napis **FAKE DAILY LIVE** (nie ma opcji jego wylaczenia)
- Ustawienia zapisuja sie w `config/fakesb.json`

## Budowanie
1. Zainstaluj JDK 21.
2. W folderze projektu wykonaj `gradle wrapper` (albo otworz projekt w IntelliJ IDEA - sam skonfiguruje Gradle).
3. `./gradlew build` -> gotowy plik w `build/libs/fakesb-1.0.0.jar`.
4. Wrzuc jar do `mods/` razem z Fabric Loader i Fabric API.
5. Test w IDE: `./gradlew runClient`.

Inna wersja Minecrafta: zmien wartosci w `gradle.properties` (kod moze wymagac drobnych poprawek API).
