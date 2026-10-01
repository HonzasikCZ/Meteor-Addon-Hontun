package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;

public class WHontunCheckbox extends WCheckbox implements HontunWidget {
    private StyleAnimation animation;

    private final double[] size = new double[2];

    public WHontunCheckbox(boolean checked) {
        super(checked);
    }

    @Override
    public void init() {
        super.init();

        animation = StyleAnimation.of(style().anim(AnimRole.CHECKBOX), theme(), checked);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintCheckbox(this, renderer, mouseX, mouseY);
    }

    @Override
    protected void onCalculateSize() {
        style().checkboxSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected void onPressed(int button) {
        super.onPressed(button);
        animation.start(checked ? Direction.FORWARDS : Direction.BACKWARDS);
        style().onPressed(this);
    }

    public double progress() {
        return animation.getProgress();
    }

    public boolean animating() {
        return animation.isRunning();
    }

    public boolean animFinished() {
        return animation.isFinished();
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setChecked(boolean checked) {
        if (this.checked == checked) return;
        this.checked = checked;
        animation.start(checked ? Direction.FORWARDS : Direction.BACKWARDS);
    }
}
