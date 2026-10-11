package cz.honzasik.hontun.gui.render.text;

import cz.honzasik.hontun.gui.api.text.FontStyle;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderOperation;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public class HontunTextRenderer {
    private final Pool<RichTextOperation> textPool = new Pool<>(RichTextOperation::new);
    private final Map<StyleKey, List<RichTextOperation>> groupedOperations = new HashMap<>();
    private final List<VanillaRun> vanillaRuns = new ArrayList<>();
    private int vanillaCount;

    public void text(RichText text, double x, double y, Color color, HontunGuiTheme theme) {
        double segmentX = x;
        boolean legibility = theme.style().legibilityShadow(theme);

        for (RichTextSegment segment : text.getSegments()) {
            if (segment.getText() == null || segment.getText().isEmpty()) continue;

            if (GlyphSupport.needsVanilla(segment.getText())) {
                mixed(segment, segmentX, y, color, theme, legibility);
                segmentX += theme.textWidth(segment);
                continue;
            }

            RichTextSegment drawn = segment;
            if (legibility && !segment.hasShadow()) {
                drawn = new RichTextSegment(segment.getText());
                drawn.setStyle(segment.getStyle());
                drawn.setScale(segment.getScale());
                drawn.setShadow(true);
            }

            RichTextOperation operation = getOperation(textPool, segmentX, y, color)
                    .set(drawn, theme.richTextRenderer());

            StyleKey key = new StyleKey(operation.getStyle(), operation.getScale());

            groupedOperations
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(operation);

            segmentX += theme.textWidth(segment);
        }
    }

    private void mixed(RichTextSegment segment, double x, double y, Color color, HontunGuiTheme theme, boolean legibility) {
        String text = segment.getText();
        int end = text.length();
        boolean shadow = legibility || segment.hasShadow();
        double runX = x;
        int start = 0;

        while (start < end) {
            int stop = GlyphSupport.runEnd(text, start, end);
            boolean vanilla = GlyphSupport.vanillaRun(text, start);

            if (vanilla) {
                vanillaRun().set(text.substring(start, stop), runX, y + theme.vanillaTextOffset(segment.getScale()), color,
                        segment.getScale(), segment.getStyle(), shadow);
            } else {
                RichTextSegment part = new RichTextSegment(text.substring(start, stop));
                part.setStyle(segment.getStyle());
                part.setScale(segment.getScale());
                part.setShadow(shadow);

                RichTextOperation operation = getOperation(textPool, runX, y, color).set(part, theme.richTextRenderer());
                groupedOperations
                        .computeIfAbsent(new StyleKey(operation.getStyle(), operation.getScale()), k -> new ArrayList<>())
                        .add(operation);
            }

            runX += theme.runWidth(segment, start, stop, vanilla);
            start = stop;
        }
    }

    public void render(

            GuiGraphicsExtractor graphics,
            HontunGuiTheme theme
    ) {
        ClickStyle style = theme.style();
        RichTextRenderer.shadowStyle(style.textShadowColor(), style::textShadowOffset);

        try {
            for (Map.Entry<StyleKey, List<RichTextOperation>> entry : groupedOperations.entrySet()) {
                List<RichTextOperation> textOps = entry.getValue();

                if (textOps.isEmpty()) continue;

                StyleKey key = entry.getKey();

                theme.richTextRenderer().setFontStyle(key.style());
                theme.richTextRenderer().begin(

                        graphics,
                        theme.scale(key.scale())
                );

                for (RichTextOperation text : textOps) {
                    text.run(textPool);
                }

                theme.richTextRenderer().end();
                textOps.clear();
            }
        } finally {
            RichTextRenderer.resetShadowStyle();
        }

        renderVanilla(graphics, theme);
    }

    private void renderVanilla(GuiGraphicsExtractor graphics, HontunGuiTheme theme) {
        if (vanillaCount == 0) return;

        try {
            if (FallbackTextLayer.available()) drawLayer(theme);
            else drawDeferred(graphics, theme);
        } finally {
            for (int i = 0; i < vanillaCount; i++) vanillaRuns.get(i).clear();
            vanillaCount = 0;
        }
    }

    private void drawLayer(HontunGuiTheme theme) {
        Minecraft mc = Minecraft.getInstance();
        double gs = mc.getWindow().getGuiScale();
        HontunRenderer hr = HontunRenderer.get();
        GuiGraphicsExtractor g = FallbackTextLayer.graphics();

        boolean scissor = hr.isClipEnabled();
        if (scissor) {
            int left = (int) Math.ceil(hr.getClipMinX() / gs);
            int top = (int) Math.ceil(hr.getClipMinY() / gs);
            int right = (int) Math.floor(hr.getClipMaxX() / gs);
            int bottom = (int) Math.floor(hr.getClipMaxY() / gs);
            if (right <= left || bottom <= top) return;
            g.enableScissor(left, top, right, bottom);
        }

        RichTextRenderer rich = theme.richTextRenderer();
        double alpha = rich != null ? rich.alpha() : 1;
        ClickStyle style = theme.style();
        Color shadowBase = style.textShadowColor();
        Matrix3x2fStack pose = g.pose();
        float inv = (float) (1 / gs);

        for (int i = 0; i < vanillaCount; i++) {
            VanillaRun run = vanillaRuns.get(i);
            int a = (int) Math.round(run.color.a * alpha);
            if (a <= 0) continue;

            float unit = (float) theme.vanillaUnit(run.scale);
            float x = (float) Math.round(run.x);
            float y = (float) Math.round(run.y);

            pose.pushMatrix();
            pose.scale(inv, inv);

            if (run.shadow && rich != null) {
                double off = rich.shadowOffsetFor(run.style, theme.scale(run.scale), style::textShadowOffset);
                int sa = (int) Math.round(a / 255.0 * shadowBase.a);
                if (sa > 0) {
                    pose.pushMatrix();
                    pose.translate(x + (float) off, y + (float) off);
                    pose.scale(unit, unit);
                    g.text(mc.font, run.text, 0, 0, argb(sa, shadowBase), false);
                    pose.popMatrix();
                }
            }

            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(unit, unit);
            g.text(mc.font, run.text, 0, 0, argb(a, run.color), false);
            pose.popMatrix();

            pose.popMatrix();
        }

        if (scissor) g.disableScissor();
        FallbackTextLayer.render();
    }

    private static int argb(int a, Color c) {
        return (Math.clamp(a, 0, 255) << 24) | (c.r << 16) | (c.g << 8) | c.b;
    }

    private void drawDeferred(GuiGraphicsExtractor graphics, HontunGuiTheme theme) {
        VanillaTextRenderer vanilla = VanillaTextRenderer.INSTANCE;
        if (vanilla.isBuilding()) return;

        RichTextRenderer rich = theme.richTextRenderer();
        vanilla.setAlpha(rich != null ? rich.alpha() : 1);

        try {
            int done = 0;
            while (done < vanillaCount) {
                double scale = 1;
                for (int i = 0; i < vanillaCount; i++) {
                    VanillaRun run = vanillaRuns.get(i);
                    if (!run.drawn) {
                        scale = run.scale;
                        break;
                    }
                }

                vanilla.begin(graphics, theme.vanillaTextScale(scale));
                try {
                    for (int i = 0; i < vanillaCount; i++) {
                        VanillaRun run = vanillaRuns.get(i);
                        if (run.drawn || run.scale != scale) continue;
                        vanilla.render(run.text, run.x, run.y, run.color, run.shadow);
                        run.drawn = true;
                        done++;
                    }
                } finally {
                    vanilla.end();
                }
            }
        } finally {
            vanilla.setAlpha(1);
        }
    }

    private VanillaRun vanillaRun() {
        if (vanillaCount == vanillaRuns.size()) vanillaRuns.add(new VanillaRun());
        return vanillaRuns.get(vanillaCount++);
    }

    private <T extends GuiRenderOperation<T>> T getOperation(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }

    public record StyleKey(FontStyle style, double scale) { }

    private static final class VanillaRun {
        private String text;
        private double x;
        private double y;
        private Color color;
        private double scale;
        private FontStyle style;
        private boolean shadow;
        private boolean drawn;

        private void set(String text, double x, double y, Color color, double scale, FontStyle style, boolean shadow) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.scale = scale;
            this.style = style;
            this.shadow = shadow;
            this.drawn = false;
        }

        private void clear() {
            text = null;
            color = null;
            drawn = false;
        }
    }
}
