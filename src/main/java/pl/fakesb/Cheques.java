package pl.fakesb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Cheques {
    public static final String KEY = "fakesb_cheque";

    private static Text line(String s, Formatting f) {
        return Text.literal(s).styled(st -> st.withItalic(false)).formatted(f);
    }

    private static Text arrow(String body, Formatting f) {
        return Text.empty()
                .append(line("» ", Formatting.GRAY))
                .append(line(body, f));
    }

    /** Buduje przedmiot-czek (nazwa, opis i znacznik z kwota w komponencie custom_data). */
    public static ItemStack make(Item item, long amount, String creator, String date) {
        ItemStack st = new ItemStack(item, 1);
        st.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Czek pieniężny - $" + amount)
                        .styled(s -> s.withItalic(false).withBold(true)).formatted(Formatting.GREEN));

        List<Text> lore = new ArrayList<>();
        lore.add(arrow("Ten papier wartościowy został utworzony", Formatting.WHITE));
        lore.add(arrow("na kwotę początkową +" + amount + "$!", Formatting.WHITE));
        lore.add(Text.empty());
        lore.add(Text.empty().append(line("» ", Formatting.GRAY))
                .append(line("UWAGA! ", Formatting.GREEN).copy().styled(s -> s.withBold(true)))
                .append(line("Nowe czeki tracą swoją wartość", Formatting.GRAY)));
        lore.add(arrow("o 2% ich startowej wartości z każdą godziną.", Formatting.GRAY));
        lore.add(Text.empty());
        lore.add(arrow("Wytworzył: " + creator + " (" + date + ")", Formatting.GRAY));
        lore.add(arrow("Kliknij PRAWYM, aby wykorzystać.", Formatting.GREEN));
        st.set(DataComponentTypes.LORE, new LoreComponent(lore));

        NbtCompound tag = new NbtCompound();
        tag.putLong(KEY, amount);
        st.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        return st;
    }

    /** Zwraca kwote czeku albo -1, jesli to nie jest czek z moda. */
    public static long amountOf(ItemStack st) {
        NbtComponent nc = st.get(DataComponentTypes.CUSTOM_DATA);
        if (nc == null) return -1;
        NbtCompound tag = nc.copyNbt();
        return tag.contains(KEY) ? tag.getLong(KEY) : -1;
    }

    /** Kwota z tekstu: 1500, 1k, 2.5k, 1m. Zwraca -1 przy bledzie. */
    public static long parseAmount(String s) {
        try {
            s = s.trim().toLowerCase(Locale.ROOT).replace(" ", "");
            if (s.endsWith("k")) return (long) (Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000);
            if (s.endsWith("m")) return (long) (Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000_000);
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Tworzy czek z przedmiotu w rece (pusta reka = papier), odejmuje kwote od stanu konta na SB.
     * Dziala tylko na wbudowanym serwerze (singleplayer). Zwraca tekst bledu albo null, gdy sie udalo.
     */
    public static String create(MinecraftClient mc, long amt) {
        SbConfig c = FakeSbClient.CFG;
        if (amt < 1000) return "Minimalna kwota to 1000";
        if (c.money < amt) return "Za mało środków na koncie";
        MinecraftServer srv = mc.getServer();
        if (srv == null || mc.player == null) return "Tylko w singleplayerze";

        ItemStack held = mc.player.getMainHandStack();
        Item item = held.isEmpty() ? Items.PAPER : held.getItem();
        String date = c.chequeDate.isBlank()
                ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss"))
                : c.chequeDate;
        ItemStack cheque = make(item, amt, c.chequeCreator, date);

        c.money -= amt;
        FakeSbClient.save();
        UUID uuid = mc.player.getUuid();
        srv.execute(() -> {
            ServerPlayerEntity sp = srv.getPlayerManager().getPlayer(uuid);
            if (sp != null) sp.giveItemStack(cheque);
        });
        return null;
    }
}
