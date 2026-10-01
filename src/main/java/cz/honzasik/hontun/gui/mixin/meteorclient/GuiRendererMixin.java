package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.route.PrimitiveRouter;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.gui.renderer.Scissor;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
        renderer().setFrame(graphics, TEXTURE);
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

        HontunGuiTheme hontun = (HontunGuiTheme) theme;

        if (scissor != null) scissor.push();

        r.end();
        rTex.end();

        render();

        if (hontun.router().skipTextPass()) {
            for (TextOperation text : texts) textPool.free(text);
        } else if (hontun.richText()) {
            renderer().renderText(

                    graphics
            );

        } else {
            HontunRenderer.setFlatText(hontun.light());
            HontunRenderer.setClickGuiTextPass(true);
            try {
                double textScale = hontun.mcTextScale();

                theme.textRenderer().begin(

                        graphics,
                        textScale
                );

                for (TextOperation text : texts) {
                    if (!text.title) text.run(textPool);
                }
                theme.textRenderer().end();

                theme.textRenderer().begin(

                        graphics,
                        textScale
                );

                for (TextOperation text : texts) {
                    if (text.title) text.run(textPool);
                }
                theme.textRenderer().end();
            } finally {
                HontunRenderer.setFlatText(false);
                HontunRenderer.setClickGuiTextPass(false);
            }
        }

        texts.clear();

        if (scissor != null) scissor.pop();

        ci.cancel();
    }

    @Inject(method = "text", at = @At("HEAD"), cancellable = true)
    private void hontun$text(String text, double x, double y, Color color, boolean title, CallbackInfo ci) {
        if (!(theme instanceof HontunGuiTheme hontun)) return;

        if (hontun.router().meteorText((GuiRenderer) (Object) this, text, x, y, color, title)) {
            ci.cancel();
            return;
        }

        if (!hontun.richText()) return;

        renderer().text(RichText.of(text).boldIf(title), x, y, color);
        ci.cancel();
    }

    @Inject(
            method = "quad(DDDDLmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft, CallbackInfo ci) {
        PrimitiveRouter router = router();
        if (router != null && router.quad4((GuiRenderer) (Object) this, x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft)) ci.cancel();
    }

    @Inject(
            method = "quad(DDDDLmeteordevelopment/meteorclient/gui/renderer/packer/GuiTexture;Lmeteordevelopment/meteorclient/utils/render/color/Color;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$texQuad(double x, double y, double width, double height, GuiTexture texture, Color color, CallbackInfo ci) {
        PrimitiveRouter router = router();
        if (router != null && router.texQuad((GuiRenderer) (Object) this, x, y, width, height, texture, color)) ci.cancel();
    }

    @Inject(
            method = "rotatedQuad(DDDDDLmeteordevelopment/meteorclient/gui/renderer/packer/GuiTexture;Lmeteordevelopment/meteorclient/utils/render/color/Color;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color, CallbackInfo ci) {
        PrimitiveRouter router = router();
        if (router != null && router.rotatedTexQuad((GuiRenderer) (Object) this, x, y, width, height, rotation, texture, color)) ci.cancel();
    }

    @Inject(
            method = "triangle(DDDDDDLmeteordevelopment/meteorclient/utils/render/color/Color;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color, CallbackInfo ci) {
        PrimitiveRouter router = router();
        if (router != null && router.triangle((GuiRenderer) (Object) this, x1, y1, x2, y2, x3, y3, color)) ci.cancel();
    }

    @Inject(
            method = "texture(DDDDDLmeteordevelopment/meteorclient/renderer/Texture;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hontun$texture(double x, double y, double width, double height, double rotation, Texture texture, CallbackInfo ci) {
        PrimitiveRouter router = router();
        if (router != null && router.texture((GuiRenderer) (Object) this, x, y, width, height, rotation, texture)) ci.cancel();
    }

    @ModifyVariable(method = "setAlpha(D)V", at = @At("HEAD"), argsOnly = true)
    private double hontun$setAlpha(double a) {
        if (!isHontunActive()) return a;
        return HontunRenderer.get().onSetAlpha(a);
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
    private PrimitiveRouter router() {
        return theme instanceof HontunGuiTheme hontun ? hontun.router() : null;
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
