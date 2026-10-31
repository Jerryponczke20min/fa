package pl.fakesb.mixin;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Kosmetyka: wpisywana komenda ".czek ..." jest w polu czatu zawsze zielona. */
@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
    // require = 0: gdyby metoda nazywala sie inaczej w innej wersji, mod nadal sie uruchomi (bez zielonego koloru)
    @Inject(method = "provideRenderText", at = @At("HEAD"), cancellable = true, require = 0)
    private void fakesb$green(String original, int firstCharacterIndex, CallbackInfoReturnable<OrderedText> cir) {
        if (firstCharacterIndex == 0 && original.toLowerCase().startsWith(".czek")) {
            cir.setReturnValue(OrderedText.styledForwardsVisitedString(original, Style.EMPTY.withColor(Formatting.GREEN)));
        }
    }
}
