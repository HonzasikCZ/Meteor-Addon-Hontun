package cz.honzasik.hontun.gui.render.text;

import meteordevelopment.meteorclient.utils.render.MeteorMcGuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class FallbackTextLayer {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/FallbackText");

    private static FeatureRenderDispatcher features;
    private static List<PictureInPictureRenderer<?>> pictureInPicture;
    private static GuiRenderState state;
    private static GuiRenderer renderer;
    private static boolean failed;

    private FallbackTextLayer() {}

    public static void capture(FeatureRenderDispatcher f, List<PictureInPictureRenderer<?>> pip) {
        if (features != null) return;
        features = f;
        pictureInPicture = pip;
    }

    public static boolean available() {
        if (renderer != null) return true;
        if (failed || features == null) return false;
        try {
            state = new GuiRenderState();
            renderer = new MeteorMcGuiRenderer(state, features, pictureInPicture);
            return true;
        } catch (Throwable t) {
            fail("could not be created", t);
            return false;
        }
    }

    public static GuiGraphicsExtractor graphics() {
        return new GuiGraphicsExtractor(Minecraft.getInstance(), state, 0, 0);
    }

    public static void render() {
        try {
            renderer.render();
        } catch (Throwable t) {
            fail("failed while rendering", t);
        }
    }

    private static void fail(String what, Throwable t) {
        failed = true;
        renderer = null;
        state = null;
        LOG.warn("[Hontun] fallback text layer {}; non-Latin text is drawn deferred for this session", what, t);
    }
}
