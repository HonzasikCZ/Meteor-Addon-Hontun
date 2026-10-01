package cz.honzasik.hontun.mixin.client.multiplayer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.honzasik.hontun.gui.widget.HontunServerForm;
import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin({ManageServerScreen.class, DirectJoinServerScreen.class})
public abstract class ServerFormLabelMixin {
    @Unique private int hontun$labelIndex;

    @Unique
    private List<EditBox> hontun$fields() {
        List<EditBox> out = new ArrayList<>();
        for (GuiEventListener child : ((Screen) (Object) this).children()) {
            if (child instanceof EditBox box) out.add(box);
        }
        return out;
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void hontun$label(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, font, text, x, y, color);
            return;
        }
        List<EditBox> fields = hontun$fields();
        int i = hontun$labelIndex++;
        if (i < fields.size()) {
            EditBox box = fields.get(i);
            x = box.getX() + 1;
            y = box.getY() - HontunServerForm.LABEL_GAP;
        }
        op.call(g, font, HontunFont.apply(text), x, y, HontunTheme.argb(0xFF, HontunTheme.textDim()));
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void hontun$title(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, font, text, x, y, color);
            return;
        }
        hontun$labelIndex = 0;
        List<EditBox> fields = hontun$fields();
        if (!fields.isEmpty()) y = HontunServerForm.titleY(fields.get(0));
        op.call(g, font, HontunFont.apply(text), x, y, HontunTheme.argb(0xFF, HontunTheme.textLight()));
    }
}
