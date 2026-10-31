package pl.fakesb;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

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
}
