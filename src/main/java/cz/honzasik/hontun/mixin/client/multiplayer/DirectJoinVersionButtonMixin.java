package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.screen.HontunVersionScreen;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.VfpBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(DirectJoinServerScreen.class)
public abstract class DirectJoinVersionButtonMixin {
    @Shadow @Final private ServerData serverData;
    @Shadow private EditBox ipEdit;

    @Unique private String hontun$savedIp;

    @Inject(method = "init", at = @At("TAIL"))
    private void hontun$replaceVersionButton(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        if (hontun$savedIp != null && ipEdit != null) { ipEdit.setValue(hontun$savedIp); hontun$savedIp = null; }

        if (!HontunTheme.restyleEnabled()) return;
        if (!VfpBridge.available() || serverData == null || !VfpBridge.perServerAvailable(serverData)) return;

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

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void hontun$stripVfpButton(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Screen self = (Screen) (Object) this;
        for (GuiEventListener child : new ArrayList<>(self.children())) {
            if (!(child instanceof Button b)) continue;
            boolean vfpBySize = b.getWidth() == 98 && b.getHeight() == 20;
            boolean vfpByKey = b.getMessage() != null
                    && b.getMessage().getContents() instanceof TranslatableContents tc
                    && "base.viafabricplus.set_version".equals(tc.getKey());
            if (vfpBySize || vfpByKey) ((ScreenInvoker) self).hontun$removeWidget(b);
        }
    }
}
