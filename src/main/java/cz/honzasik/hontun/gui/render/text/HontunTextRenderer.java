package cz.honzasik.hontun.gui.render.text;

import cz.honzasik.hontun.gui.api.text.FontStyle;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderOperation;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HontunTextRenderer {
    private final Pool<RichTextOperation> textPool = new Pool<>(RichTextOperation::new);
    private final Map<StyleKey, List<RichTextOperation>> groupedOperations = new HashMap<>();

    public void text(RichText text, double x, double y, Color color, HontunGuiTheme theme) {
        double segmentX = x;

        for (RichTextSegment segment : text.getSegments()) {
            if (segment.getText() == null || segment.getText().isEmpty()) continue;

            RichTextOperation operation = getOperation(textPool, segmentX, y, color)
                    .set(segment, theme.richTextRenderer());

            StyleKey key = new StyleKey(operation.getStyle(), operation.getScale());

            groupedOperations
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(operation);

            segmentX += theme.textWidth(segment);
        }
    }

    public void render(

            GuiGraphicsExtractor graphics,
            HontunGuiTheme theme
    ) {
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
    }

    private <T extends GuiRenderOperation<T>> T getOperation(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }

    public record StyleKey(FontStyle style, double scale) { }
}
