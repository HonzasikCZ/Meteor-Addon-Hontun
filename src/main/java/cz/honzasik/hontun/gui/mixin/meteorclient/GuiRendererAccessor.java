package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.Scissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.Stack;

@Mixin(value = GuiRenderer.class, remap = false)
public interface GuiRendererAccessor {
    @Accessor("scissorStack")
    Stack<Scissor> hontun$getScissorStack();
}
