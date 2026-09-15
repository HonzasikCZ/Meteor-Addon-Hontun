package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.screen.HontunVersionScreen;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.VfpBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DirectJoinServerScreen.class)
public abstract class DirectJoinVersionButtonMixin {
    @Shadow @Final private ServerData serverData;
    @Shadow private EditBox ipEdit;

    @Unique private String hontun$savedIp;

    @Inject(method = "init", at = @At("TAIL"))
    private void hontun$replaceVersionButton(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        if (hontun$savedIp != null && ipEdit != null) { ipEdit.setValue(hontun$savedIp); hontun$savedIp = null; }

        if (!HontunTheme.restyleEnabled() || !VfpBridge.available()) return;

        hontun$removeVfpButton(self);
        if (serverData == null || !VfpBridge.perServerAvailable(serverData)) return;

        Object forced = VfpBridge.forcedVersion(serverData);
        String label = forced == null ? "Version: global" : "Version: " + VfpBridge.nameOf(forced);

        Button b = Button.builder(Component.literal(label), btn -> {
            if (ipEdit != null) hontun$savedIp = ipEdit.getValue();
            Minecraft.getInstance().gui.setScreen(new HontunVersionScreen(self, "direct connect",
                    () -> VfpBridge.forcedVersion(serverData),
                    handle -> VfpBridge.setForcedVersion(serverData, handle)));
        }).bounds(self.width / 2 - 100, self.height / 4 + 60, 200, 20).build();

        ((ScreenInvoker) self).hontun$addRenderableWidget(b);
    }

    @Unique
    private void hontun$removeVfpButton(Screen self) {
        List<GuiEventListener> doomed = new ArrayList<>();
        for (GuiEventListener child : self.children()) {
            if (!(child instanceof AbstractWidget w)) continue;
            if (w.getWidth() != 98 || w.getHeight() != 20) continue;
            int x = w.getX(), y = w.getY();
            if ((x == 5 || x == self.width - 103) && (y == 5 || y == self.height - 25)) doomed.add(child);
        }
        for (GuiEventListener d : doomed) ((ScreenInvoker) self).hontun$removeWidget(d);
    }
}
