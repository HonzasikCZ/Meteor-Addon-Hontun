package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.gui.renderer.Scissor;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import it.unimi.dsi.fastutil.Stack;

import meteordevelopment.meteorclient.renderer.Texture;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(value = GuiRenderer.class, remap = false)
public abstract class GuiRendererMixin {
    @Shadow private static Texture TEXTURE;

    @Shadow @Final private Renderer2D r;
    @Shadow @Final private Renderer2D rTex;
    @Shadow @Final private List<TextOperation> texts;
    @Shadow @Final private Pool<TextOperation> textPool;
    @Shadow public GuiTheme theme;

    @Shadow private GuiGraphicsExtractor graphics;

    @Inject(method = "init", at = @At("HEAD"))
    private static void hontun$init(CallbackInfo ci) {
        HontunBuiltinIcons.init();
    }

    @Inject(method = "beginRender", at = @At("HEAD"))
    private void hontun$beginRender(CallbackInfo ci) {
        if (!isHontunActive()) return;
        renderer().begin();
    }

    @Inject(

            method = "endRender(Lmeteordevelopment/meteorclient/gui/renderer/Scissor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$endRender(

            Scissor scissor,
            CallbackInfo ci
    ) {
        if (!isHontunActive()) return;

        if (scissor != null) scissor.push();

        r.end();
        rTex.end();

        render();

        if (Config.get().customFont.get()) {
            renderer().renderText(

                    graphics
            );

        } else {
            theme.textRenderer().begin(

                    graphics,
                    theme.scale(1)
            );

            for (TextOperation text : texts) {
                if (!text.title) text.run(textPool);
            }
            theme.textRenderer().end();

            theme.textRenderer().begin(

                    graphics,
                    theme.scale(1.25)
            );

            for (TextOperation text : texts) {
                if (text.title) text.run(textPool);
            }
            theme.textRenderer().end();
        }

        texts.clear();

        if (scissor != null) scissor.pop();

        ci.cancel();
    }

    @Inject(method = "text", at = @At("HEAD"), cancellable = true)
    private void hontun$text(String text, double x, double y, Color color, boolean title, CallbackInfo ci) {
        if (!isHontunActive() || !Config.get().customFont.get()) return;

        renderer().text(RichText.of(text).boldIf(title), x, y, color);
        ci.cancel();
    }

    @Inject(method = "scissorStart", at = @At("TAIL"))
    private void hontun$scissorStart(double x, double y, double width, double height, CallbackInfo ci) {
        if (!isHontunActive()) return;
        updateClipFromStack();
    }

    @Inject(method = "scissorEnd", at = @At("TAIL"))
    private void hontun$scissorEnd(CallbackInfo ci) {
        if (!isHontunActive()) return;
        updateClipFromStack();
    }

    @Unique
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isHontunActive() {
        return theme instanceof HontunGuiTheme;
    }

    @Unique
    private HontunRenderer renderer() {
        if (HontunRenderer.guiRenderer == null)
            HontunRenderer.guiRenderer = (GuiRenderer) (Object) this;

        return HontunRenderer.get();
    }

    @Unique
    private void render() {
        HontunRenderer renderer = renderer();

        renderer.end();
        r.render();

        rTex.render("u_Texture", TEXTURE.getTextureView(), TEXTURE.getSampler());
    }

    @Unique
    private void updateClipFromStack() {
        Stack<Scissor> stack = ((GuiRendererAccessor) this).hontun$getScissorStack();
        if (stack == null || stack.isEmpty()) {
            renderer().clearClipRect();
            return;
        }

        Scissor top = peekScissor(stack);
        renderer().setClipRect(top.x, top.y, top.x + top.width, top.y + top.height);
    }

    @Unique
    private static Scissor peekScissor(Stack<Scissor> stack) {
        return stack.top();
    }
}
