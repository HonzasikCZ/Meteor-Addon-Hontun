package cz.honzasik.hontun.gui.theme.style.vanilla;

import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class WItemIcon extends WWidget implements HontunWidget {
    private final ItemStack stack;
    private final Identifier sprite;

    public WItemIcon(ItemStack stack) {
        this.stack = stack;
        this.sprite = null;
    }

    public WItemIcon(Identifier sprite) {
        this.stack = null;
        this.sprite = sprite;
    }

    @Override
    protected void onCalculateSize() {
        int p = PixelCanvas.unit(theme());
        width = 16 * p;
        height = 16 * p;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        int p = PixelCanvas.unit(theme());

        if (sprite != null) PixelCanvas.sprite(sprite, x + 2 * p, y + 2 * p, 12, 12);
        else PixelCanvas.item(stack, x, y);
    }
}
