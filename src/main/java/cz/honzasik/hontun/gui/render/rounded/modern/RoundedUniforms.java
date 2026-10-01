package cz.honzasik.hontun.gui.render.rounded.modern;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import net.minecraft.client.renderer.DynamicUniformStorage;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.joml.Vector2f;
import org.joml.Vector4f;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class RoundedUniforms {
    private static final int ROUNDED_DATA_SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec2()
            .putVec4()
            .get();

    private static final RoundedRectData ROUNDED_DATA = new RoundedRectData();
    private static final DynamicUniformStorage<RoundedRectData> ROUNDED_STORAGE = new DynamicUniformStorage<>("Hontun - Rounded UBO", ROUNDED_DATA_SIZE, 16);

    public static GpuBufferSlice getUniformStorage() {
        return ROUNDED_STORAGE.writeUniform(ROUNDED_DATA);
    }

    public static void flipFrame() {
        ROUNDED_STORAGE.endFrame();
    }

    public static boolean update(RoundedRect rect, float globalAlpha) {
        if (!applyClipRect(ROUNDED_DATA.clipRect, rect)) return false;

        Color fillColor = rect.getFillColor();
        Color borderColor = rect.getOutlineColor();
        float feather = rect.getFeather();

        ROUNDED_DATA.fillColor.set(fillColor.r / 255f, fillColor.g / 255f, fillColor.b / 255f, fillColor.a / 255f);
        ROUNDED_DATA.borderColor.set(borderColor.r / 255f, borderColor.g / 255f, borderColor.b / 255f, borderColor.a / 255f);
        ROUNDED_DATA.borderData.set(rect.getOutlineWidth(), feather > 0f ? feather : 0f, globalAlpha, rect.getDither() ? 1f : 0f);
        ROUNDED_DATA.radii.set(rect.getTopLeft(), rect.getTopRight(), rect.getBottomRight(), rect.getBottomLeft());
        ROUNDED_DATA.halfSize.set((float) (rect.getWidth() * 0.5), (float) (rect.getHeight() * 0.5));
        return true;
    }

    private static boolean applyClipRect(Vector4f target, RoundedRect rect) {
        HontunRenderer renderer = HontunRenderer.get();
        boolean scissor = renderer.isClipEnabled();
        boolean band = rect.hasClip();

        if (!scissor && !band) {
            target.set(0f, 0f, -1f, -1f);
            return true;
        }

        float minX;
        float minY;
        float maxX;
        float maxY;

        if (band) {
            minX = Math.round(rect.getClipX());
            minY = Math.round(rect.getClipY());
            maxX = Math.round(rect.getClipX() + rect.getClipWidth());
            maxY = Math.round(rect.getClipY() + rect.getClipHeight());

            if (scissor) {
                minX = Math.max(minX, renderer.getClipMinX());
                minY = Math.max(minY, renderer.getClipMinY());
                maxX = Math.min(maxX, renderer.getClipMaxX());
                maxY = Math.min(maxY, renderer.getClipMaxY());
            }
        } else {
            minX = renderer.getClipMinX();
            minY = renderer.getClipMinY();
            maxX = renderer.getClipMaxX();
            maxY = renderer.getClipMaxY();
        }

        if (!(maxX > minX) || !(maxY > minY)) return false;

        target.set(minX, minY, maxX, maxY);
        return true;
    }

    private static final class RoundedRectData implements DynamicUniformStorage.DynamicUniform {
        private final Vector4f fillColor = new Vector4f();
        private final Vector4f borderColor = new Vector4f();
        private final Vector4f borderData = new Vector4f();
        private final Vector4f radii = new Vector4f();
        private final Vector2f halfSize = new Vector2f();
        private final Vector4f clipRect = new Vector4f();

        @Override
        public void write(

                @NonNull
                ByteBuffer buffer
        ) {
            Std140Builder.intoBuffer(buffer)
                    .putVec4(fillColor)
                    .putVec4(borderColor)
                    .putVec4(borderData)
                    .putVec4(radii)
                    .putVec2(halfSize)
                    .putVec4(clipRect);
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
