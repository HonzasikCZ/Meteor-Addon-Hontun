package cz.honzasik.hontun.gui.render.rounded.modern;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.HontunGui;
import meteordevelopment.meteorclient.renderer.ExtendedRenderPipelineBuilder;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.MeteorVertexFormats;
import com.mojang.blaze3d.shaders.UniformType;

import java.lang.reflect.Method;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import java.util.Optional;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;

public class HontunRenderPipelines {
    private static final RenderPipeline.Snippet MESH_UNIFORMS = RenderPipeline.builder()

        .withBindGroupLayout(BindGroupLayout.builder()
                .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
                .build())

        .buildSnippet();

    public static final RenderPipeline ROUNDED_UI = register(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(HontunGui.identifier("pipeline/rounded_ui"))
        .withVertexShader(HontunGui.identifier("shaders/rounded_ui.vert"))
        .withFragmentShader(HontunGui.identifier("shaders/rounded_ui.frag"))

        .withVertexBinding(0, MeteorVertexFormats.POS2_TEXTURE_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withBindGroupLayout(BindGroupLayout.builder()
                .withUniform("RoundedRectData", UniformType.UNIFORM_BUFFER)
                .build())

        .withDepthStencilState(Optional.empty())
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))

        .withCull(false)
        .build()
    );

    private static RenderPipeline register(RenderPipeline pipeline) {
        try {
            Method method = MeteorRenderPipelines.class.getDeclaredMethod("add", RenderPipeline.class);
            method.setAccessible(true);
            return (RenderPipeline) method.invoke(null, pipeline);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register pipeline", e);
        }
    }
}
