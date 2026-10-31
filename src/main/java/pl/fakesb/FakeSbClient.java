package pl.fakesb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FakeSbClient implements ClientModInitializer {
    public static KeyBinding openKey;
    public static SbConfig CFG = new SbConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fakesb.json");

    // Zezwala na działanie moda w każdym trybie gry (również na serwerach multiplayer)
    public static boolean allowed(MinecraftClient c) {
        return true;
    }

    private static boolean bad(int[] a, int n) {
        return a == null || a.length != n;
    }

    public static void load() {
        try (Reader r = Files.newBufferedReader(FILE)) {
            SbConfig loaded = GSON.fromJson(r, SbConfig.class);
            if (loaded != null) {
                SbConfig d = new SbConfig();
                if (bad(loaded.titleColors, SbConfig.TITLE_MAX)) loaded.titleColors = d.titleColors;
                if (bad(loaded.iconColors, 7)) loaded.iconColors = d.iconColors;
                if (bad(loaded.labelColors, 7)) loaded.labelColors = d.labelColors;
                if (bad(loaded.valueColors, 7)) loaded.valueColors = d.valueColors;
                if (bad(loaded.footerColors, 3)) loaded.footerColors = d.footerColors;
                CFG = loaded;
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(CFG, w);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInitializeClient() {
        load();

        // Domyslnie F (w Sterowaniu mozesz zmienic na dowolny klawisz)
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.fakesb.open", GLFW.GLFW_KEY_F, "category.fakesb"));

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            while (openKey.wasPressed()) {
                if (allowed(c)) c.setScreen(new SbScreen());
            }
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            if (allowed(MinecraftClient.getInstance())) SbHud.render(ctx);
        });

        // Komenda czatu "./czek <kwota>" - przechwytywana lokalnie, działa na multiplayerze
        ClientSendMessageEvents.ALLOW_CHAT.register(msg -> {
            String t = msg.trim();
            String low = t.toLowerCase(Locale.ROOT);
            
            // Sprawdzenie czy gracz wpisał ./czek lub ./czek [coś]
            if (!(low.equals("./czek") || low.startsWith("./czek "))) return true;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return false;

            // Sprawiamy, że wpisana komenda wyświetla się na zielono na czacie
            mc.player.sendMessage(Text.literal(t).formatted(Formatting.GREEN), false);

            String text;
            Formatting color = Formatting.GREEN;
            
            // Pobieranie kwoty po komendzie ./czek (wycinamy pierwsze 6 znaków razem ze spacją)
            long amt = Cheques.parseAmount(t.substring(6).trim());
            String err = amt < 0 ? "Użycie: ./czek <kwota>, np. ./czek 5k" : Cheques.create(mc, amt);
            if (err == null) {
                text = "Czek na " + amt + "$ został utworzony.";
            } else {
                text = err;
                color = Formatting.YELLOW;
            }
            
            mc.player.sendMessage(Text.literal(text).formatted(color), false);
            return false;   // Blokuje wysłanie tej wiadomości na publiczny czat serwera
        });

        // Uzycie czeku (prawy klik) - mały szarawy napis tuż nad celownikiem
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) return ActionResult.PASS;
            ItemStack st = player.getStackInHand(hand);
            long amt = Cheques.amountOf(st);
            if (amt < 0) return ActionResult.PASS;

            CFG.money += amt;
            save();
            st.decrement(1);

            // Wyświetlanie szarego napisu minimalnie nad celownikiem (jako Subtitle bez głównego Title)
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.inGameHud != null) {
                mc.inGameHud.setTitleTicks(5, 40, 5); // Krótki czas pojawiania i zniknięcia
                
                String fullText = String.format(Locale.US, "Na twoje konto zostało dodane %d$", amt);
                
                // Pusty główny tytuł sprawia, że podtytuł (Subtitle) wędruje wyżej, tuż nad celownik
                mc.inGameHud.setTitle(Text.literal(""));
                mc.inGameHud.setSubtitle(Text.literal(fullText).formatted(Formatting.GRAY));
            }

            return ActionResult.SUCCESS;
        });
    }
}
