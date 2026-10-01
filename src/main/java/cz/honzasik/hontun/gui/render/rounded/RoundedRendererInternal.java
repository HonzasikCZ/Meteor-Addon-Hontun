package cz.honzasik.hontun.gui.render.rounded;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.honzasik.hontun.gui.api.render.RoundedRect;

public interface RoundedRendererInternal {
    void begin();

    void end();

    void render(PoseStack stack);

    void render(RoundedRect rect);

    void flipFrame();
}
