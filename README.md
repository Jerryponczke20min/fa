# Custom SB & Cheques (demo mod)

Klientowy mod Fabric (Minecraft 1.21.4): konfigurowalny scoreboard HUD i czeki z dowolnego przedmiotu.

**Dziala tylko w singleplayerze / na wlasnym swiecie.** Na zewnetrznych serwerach HUD i czeki sa wylaczone
(`FakeSbClient.allowed()`).

## Funkcje
- GUI otwierane klawiszem (domyslnie `F`, zmienisz w Sterowanie -> "Custom SB (demo)")
- Zakladka **Scoreboard**: nick, klan, serca, czas gry, stan konta, zabojstwa, smierci, kanal, tytul (wysrodkowany) + suwaki: rozmiar,
  przezroczystosc tla, pozycja X/Y
- Zakladka **Kolory**: suwaki RGB (bez kodow HEX) - strzalkami `<` `>` wybierasz element: kazda litera tytulu, ikona/nazwa/wartosc kazdej linii, stopka ("Jestes na KANAL (/ch)")
- Zakladka **Czek**: kwota (min 1000, mozna `1k`), tworca, data; tworzy czek z przedmiotu trzymanego w rece
  (pusta reka = papier). Utworzenie czeku odejmuje kwote ze stanu konta, uzycie (prawy klik) dodaje ja z powrotem
  i pokazuje komunikat "Na twoje konto zostalo dodane X$"
- Komenda czatu `.czek <kwota>` (np. `.czek 5k`) tworzy czek tak samo jak GUI (tylko singleplayer, nie jest wysylana na czat; tekst komendy jest w polu czatu zielony)
- Przycisk wlaczajacy/wylaczajacy SB, GUI w stylu granatowo-wisniowym, szybka paleta kolorow, przycisk "Tecza w tytule"
- Nad SB stale wyswietla sie animowany, teczowy napis **FAKE DAILY LIVE** (nie ma opcji jego wylaczenia)
- Ustawienia zapisuja sie w `config/fakesb.json`

## Budowanie
1. Zainstaluj JDK 21.
2. W folderze projektu wykonaj `gradle wrapper` (albo otworz projekt w IntelliJ IDEA - sam skonfiguruje Gradle).
3. `./gradlew build` -> gotowy plik w `build/libs/fakesb-1.0.0.jar`.
4. Wrzuc jar do `mods/` razem z Fabric Loader i Fabric API.
5. Test w IDE: `./gradlew runClient`.

Inna wersja Minecrafta: zmien wartosci w `gradle.properties` (kod moze wymagac drobnych poprawek API).
