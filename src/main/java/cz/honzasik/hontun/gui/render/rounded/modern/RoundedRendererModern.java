package cz.honzasik.hontun.gui.render.rounded.modern;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.rounded.RoundedRendererInternal;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.Minecraft;

public class RoundedRendererModern implements RoundedRendererInternal {
    private final MeshBuilder roundedMesh;

    public RoundedRendererModern() {
        this.roundedMesh = new MeshBuilder(HontunRenderPipelines.ROUNDED_UI);
    }

    @Override
    public void begin() {
        roundedMesh.begin();
    }

    @Override
    public void end() {
        if (roundedMesh.isBuilding()) roundedMesh.end();
    }

    @Override
    public void render(PoseStack stack) { }

    @Override
    public void render(RoundedRect rect) {
        float alpha = (float) HontunRenderer.get().globalAlpha();
        if (!(alpha > 0f)) return;
        if (alpha > 1f) alpha = 1f;

        if (!RoundedUniforms.update(rect, alpha)) return;

        double x = rect.getX();
        double y = rect.getY();
        double width = rect.getWidth();
        double height = rect.getHeight();
        double halfWidth = width * 0.5;
        double halfHeight = height * 0.5;

        boolean gradient = rect.hasGradient();
        Color topLeft = gradient ? rect.getGradientTopLeft() : Color.WHITE;
        Color topRight = gradient ? rect.getGradientTopRight() : Color.WHITE;
        Color bottomRight = gradient ? rect.getGradientBottomRight() : Color.WHITE;
        Color bottomLeft = gradient ? rect.getGradientBottomLeft() : Color.WHITE;

        boolean building = roundedMesh.isBuilding();
        if (!building) roundedMesh.begin();

        roundedMesh.ensureQuadCapacity();
        roundedMesh.quad(
                roundedMesh.vec2(x, y).vec2(-halfWidth, -halfHeight).color(topLeft).next(),
                roundedMesh.vec2(x, y + height).vec2(-halfWidth, halfHeight).color(bottomLeft).next(),
                roundedMesh.vec2(x + width, y + height).vec2(halfWidth, halfHeight).color(bottomRight).next(),
                roundedMesh.vec2(x + width, y).vec2(halfWidth, -halfHeight).color(topRight).next()
        );

        roundedMesh.end();

        MeshRenderer.begin()
                .attachments(Minecraft.getInstance().gameRenderer.mainRenderTarget())
                .pipeline(HontunRenderPipelines.ROUNDED_UI)
                .mesh(roundedMesh)
                .uniform("RoundedRectData", RoundedUniforms.getUniformStorage())
                .end();

        if (building) roundedMesh.begin();
    }

    public void flipFrame() {
        RoundedUniforms.flipFrame();
    }
}
