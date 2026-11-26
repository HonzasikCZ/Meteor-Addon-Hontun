package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.screen.server.ServerListPanel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerPanelMixin {
    @Unique private ServerListPanel hontun$panel;

    @Inject(method = "init", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void hontun$addPanel(CallbackInfo ci) {
        try {
            Screen self = (Screen) (Object) this;
            if (hontun$panel == null) hontun$panel = new ServerListPanel((JoinMultiplayerScreen) (Object) this);
            ((ScreenInvoker) self).hontun$addRenderableWidget(hontun$panel);
            List<GuiEventListener> children = (List<GuiEventListener>) self.children();
            children.remove(hontun$panel);
            children.add(0, hontun$panel);
        } catch (Throwable ignored) {
        }
    }
}
