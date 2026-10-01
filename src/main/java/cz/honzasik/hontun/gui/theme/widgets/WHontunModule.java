package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.HoverTarget;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WHontunModule extends WPressable implements HontunWidget, HoverTarget {
    private final Module module;
    private final RichText title;

    private double titleWidth;
    private boolean wasHovered = false;
    private boolean wasActive = false;

    private boolean prevActive;
    private boolean nextActive;
    private int runIndex;
    private int runLength;

    private StyleAnimation highlightAnimation;
    private StyleAnimation hoverAnimation;

    private final double[] size = new double[2];

    public WHontunModule(Module module, String title) {
        this.module = module;
        this.title = RichText.of(title);
        this.tooltip = module.description;
    }

    @Override
    public void init() {
        boolean isActive = module.isActive();
        wasActive = isActive;

        ClickStyle style = style();
        highlightAnimation = StyleAnimation.of(style.anim(AnimRole.MODULE_ACTIVE), theme(), isActive);
        hoverAnimation = StyleAnimation.of(style.anim(AnimRole.MODULE_HOVER), theme());
    }

    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        style().moduleSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected void onPressed(int button) {
        style().onPressed(this);

        if (button == GLFW_MOUSE_BUTTON_LEFT)
            module.toggle();

        else if (button == GLFW_MOUSE_BUTTON_RIGHT)
            mc.gui.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean moduleActive = module.isActive();

        if (moduleActive != wasActive) {
            wasActive = moduleActive;

            highlightAnimation.start(moduleActive ? Direction.FORWARDS : Direction.BACKWARDS);
        }

        if (mouseOver != wasHovered) {
            wasHovered = mouseOver;

            if (mouseOver) hoverAnimation.forward();

            else hoverAnimation.backward();
        }

        updateNeighbours();

        style().paintModule(this, renderer, mouseX, mouseY);
    }

    private void updateNeighbours() {
        prevActive = false;
        nextActive = false;
        runIndex = 0;
        runLength = module.isActive() ? 1 : 0;

        if (!(parent instanceof WContainer container)) return;

        List<Cell<?>> cells = container.cells;
        int index = -1;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).widget() == this) {
                index = i;
                break;
            }
        }
        if (index < 0) return;

        prevActive = activeAt(cells, index - 1);
        nextActive = activeAt(cells, index + 1);

        if (!module.isActive()) return;

        int start = index;
        while (activeAt(cells, start - 1)) start--;
        int end = index;
        while (activeAt(cells, end + 1)) end++;

        runIndex = index - start;
        runLength = end - start + 1;
    }

    private static boolean activeAt(List<Cell<?>> cells, int index) {
        if (index < 0 || index >= cells.size()) return false;
        return cells.get(index).widget() instanceof WHontunModule m && m.module.isActive();
    }

    public Module module() {
        return module;
    }

    public RichText title() {
        return title;
    }

    public double titleWidth() {
        return titleWidth;
    }

    public double hoverProgress() {
        return hoverAnimation.getProgress();
    }

    public double highlightProgress() {
        return highlightAnimation.getProgress();
    }

    public boolean isPrevActive() {
        return prevActive;
    }

    public boolean isNextActive() {
        return nextActive;
    }

    public int runIndex() {
        return runIndex;
    }

    public int runLength() {
        return runLength;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    public boolean hoverLit() {
        return mouseOver;
    }
}
