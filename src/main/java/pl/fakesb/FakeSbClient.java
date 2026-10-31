package pl.fakesb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

    /** Mod dziala TYLKO w singleplayerze (swiat z wbudowanym serwerem, bez wpisu serwera). */
    public static boolean allowed(MinecraftClient c) {
        return c.world != null && c.isInSingleplayer() && c.getCurrentServerEntry() == null;
    }

    public static void load() {
        try (Reader r = Files.newBufferedReader(FILE)) {
            SbConfig loaded = GSON.fromJson(r, SbConfig.class);
            if (loaded != null) {
                SbConfig d = new SbConfig();
                if (loaded.labelColors == null || loaded.labelColors.length != 7) loaded.labelColors = d.labelColors;
                if (loaded.valueColors == null || loaded.valueColors.length != 7) loaded.valueColors = d.valueColors;
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

        // Uzycie czeku (prawy klik) - obslugiwane po stronie wbudowanego serwera
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!allowed(MinecraftClient.getInstance())) return ActionResult.PASS;
            ItemStack st = player.getStackInHand(hand);
            long amt = Cheques.amountOf(st);
            if (amt < 0) return ActionResult.PASS;

            CFG.money += amt;
            save();
            st.decrement(1);
            player.sendMessage(Text.literal(String.format(Locale.US,
                    "Na twoje konto zostało dodane %d$", amt)).formatted(Formatting.GREEN), false);
            return ActionResult.SUCCESS;
        });
    }
}
