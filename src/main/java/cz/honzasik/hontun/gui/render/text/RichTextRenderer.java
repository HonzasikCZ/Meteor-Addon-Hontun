package cz.honzasik.hontun.gui.render.text;

import cz.honzasik.hontun.gui.api.text.FontStyle;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import meteordevelopment.meteorclient.renderer.*;
import meteordevelopment.meteorclient.renderer.text.*;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.nio.ByteBuffer;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.IOException;

import net.minecraft.client.Minecraft;

public class RichTextRenderer implements TextRenderer {
    public static final Color SHADOW_COLOR = new Color(60, 60, 60, 180);

    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);

    public final FontFace fontFace;

    private final Font[] regularFonts;
    private final Font[] boldFonts;
    private final Font[] italicFonts;

    private Font currentFont;
    private FontStyle currentStyle = FontStyle.REGULAR;
    private int currentFontIndex = 0;

    private boolean building;
    private boolean scaleOnly;
    private double fontScale = 1;
    private double scale = 1;

    public RichTextRenderer(FontFace fontFace)

            throws IOException
    {
        this.fontFace = fontFace;
        this.regularFonts = loadFonts(fontFace);
        this.boldFonts = resolveVariant(fontFace, FontInfo.Type.Bold);
        this.italicFonts = resolveVariant(fontFace, FontInfo.Type.Italic);
    }

    @Override
    public void setAlpha(double a) {
        mesh.alpha = a;
    }

    @Override
    public void begin(

            GuiGraphicsExtractor graphics,
            double scale, boolean scaleOnly, boolean big
    ) {
        if (building) throw new RuntimeException("RichTextRenderer.begin() called twice");

        if (!scaleOnly) mesh.begin();

        int scaleIndex;
        if (scale >= 3) scaleIndex = 4;
        else if (scale >= 2.5) scaleIndex = 3;
        else if (scale >= 2) scaleIndex = 2;
        else if (scale >= 1.5) scaleIndex = 1;
        else scaleIndex = 0;

        currentFontIndex = scaleIndex;
        Font[] fonts = getFonts(currentStyle);

        if (currentFontIndex >= fonts.length) currentFontIndex = fonts.length - 1;
        currentFont = fonts[currentFontIndex];

        building = true;
        this.scaleOnly = scaleOnly;

        fontScale = currentFont.getHeight() / 27.0;
        this.scale = 1 + (scale - fontScale) / fontScale;
    }

    public double getWidth(RichTextSegment segment, int length) {
        Font[] fonts = getFonts(segment.getStyle());
        Font font = building ? (currentFontIndex < fonts.length ? fonts[currentFontIndex] : fonts[0]) : fonts[0];

        double width = font.getWidth(segment.getText(), length);
        if (segment.hasShadow()) width += 1;

        return width * (segment.getScale() / 1.5);
    }

    public double getWidth(RichText text, int length) {
        if (length <= 0) return 0;

        double totalWidth = 0;
        int remainingLength = length;

        for (RichTextSegment segment : text.getSegments()) {
            String segText = segment.getText();
            if (segText.isEmpty()) continue;

            int segLength = Math.min(segText.length(), remainingLength);

            totalWidth += getWidth(segment, segLength);

            remainingLength -= segLength;
            if (remainingLength == 0) break;
        }

        return totalWidth;
    }

    public double getWidth(RichText text) {
        return getWidth(text, text.length());
    }

    @Override
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) return 0;
        return getWidth(RichText.of(text).shadowIf(shadow), length);
    }

    public double getHeight(RichTextSegment segment) {
        Font[] fonts = getFonts(segment.getStyle());
        Font font = building ? (currentFontIndex < fonts.length ? fonts[currentFontIndex] : fonts[0]) : fonts[0];

        return (font.getHeight() + 1 + (segment.hasShadow() ? 1 : 0)) * segment.getScale() / 1.5;
    }

    public double getHeight(RichText text) {
        double largestSegment = 0;
        if (text.getSegments().isEmpty()) return 0;

        for (RichTextSegment segment : text.getSegments()) {
            double h = getHeight(segment);
            if (h > largestSegment) largestSegment = h;
        }

        return largestSegment;
    }

    @Override
    public double getHeight(boolean shadow) {
        Font font = building ? currentFont : regularFonts[0];
        return (font.getHeight() + 1 + (shadow ? 1 : 0)) * scale / 1.5;
    }

    public void renderTextWithStyle(String text, double x, double y, Color color, boolean shadow, FontStyle fontStyle) {
        if (currentStyle != fontStyle) setStyleInternal(fontStyle);
        render(text, x, y, color, shadow);
    }

    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        if (!building) throw new RuntimeException("RichTextRenderer.render() called without calling begin()");

        double renderScale = scale / 1.5;
        double width;

        if (shadow) {
            int originalShadowAlpha = SHADOW_COLOR.a;
            SHADOW_COLOR.a = (int) (color.a / 255.0 * originalShadowAlpha);

            double shadowOffset = fontScale * renderScale;

            width = currentFont.render(mesh, text, x + shadowOffset, y + shadowOffset, SHADOW_COLOR, renderScale);
            currentFont.render(mesh, text, x, y, color, renderScale);

            SHADOW_COLOR.a = originalShadowAlpha;
        } else {
            width = currentFont.render(mesh, text, x, y, color, renderScale);
        }

        return width;
    }

    @Override
    public boolean isBuilding() {
        return building;
    }

    @Override
    public void end(

    ) {
        if (!building) throw new RuntimeException("end() called without calling begin()");

        if (!scaleOnly) {
            mesh.end();

            MeshRenderer.begin()
                    .attachments(Minecraft.getInstance().gameRenderer.mainRenderTarget())
                    .pipeline(MeteorRenderPipelines.UI_TEXT)
                    .mesh(mesh)

                    .sampler("u_Texture", currentFont.texture.getTextureView(), currentFont.texture.getSampler())

                    .end();
        }

        building = false;
        scale = 1;
    }

    private Font[] loadFonts(FontFace fontFace)

            throws IOException
    {
        ByteBuffer buffer = fontFace.readToDirectByteBuffer();

        Font[] fonts = new Font[5];

        for (int i = 0; i < fonts.length; i++)
            fonts[i] = new Font(buffer, (int) Math.round(27 * ((i * 0.5) + 1)));

        return fonts;
    }

    private Font[] resolveVariant(FontFace regularFace, FontInfo.Type type)

            throws IOException
    {
        FontFamily family = Fonts.getFamily(regularFace.info.family());
        FontFace fontVariant = family.get(type);

        if (fontVariant != null && fontVariant != regularFace)
            return loadFonts(fontVariant);

        return regularFonts;
    }

    public void setFontStyle(FontStyle style) {
        if (this.currentStyle == style) return;
        setStyleInternal(style);
    }

    private void setStyleInternal(FontStyle style) {
        this.currentStyle = style;
        Font[] fonts = getFonts(style);

        if (building) {
            if (currentFontIndex < fonts.length) currentFont = fonts[currentFontIndex];
            else currentFont = fonts[0];
        }
    }

    private Font[] getFonts(FontStyle style) {
        return switch (style) {
            case BOLD -> boldFonts;
            case ITALIC -> italicFonts;
            default -> regularFonts;
        };
    }

    public void destroy() {
        for (Font font : regularFonts)
            font.texture.close();

        if (boldFonts != regularFonts) {
            for (Font font : boldFonts)
                font.texture.close();
        }

        if (italicFonts != regularFonts) {
            for (Font font : italicFonts)
                font.texture.close();
        }
    }
}
