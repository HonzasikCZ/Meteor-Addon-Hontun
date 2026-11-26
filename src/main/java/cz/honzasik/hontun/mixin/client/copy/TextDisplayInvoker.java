package cz.honzasik.hontun.mixin.client.copy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayInvoker {
    @Invoker("getText")
    Component hontun$text();

    @Invoker("getLineWidth")
    int hontun$lineWidth();

    @Invoker("getBackgroundColor")
    int hontun$background();

    @Invoker("getTextOpacity")
    byte hontun$opacity();

    @Invoker("getFlags")
    byte hontun$flags();
}
