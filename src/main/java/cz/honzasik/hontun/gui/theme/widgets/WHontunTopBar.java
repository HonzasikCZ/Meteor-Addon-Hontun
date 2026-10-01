package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.icons.HontunIcons;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screens.Screen;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public class WHontunTopBar extends WTopBar implements HontunWidget {
    private final double[] size = new double[2];

    @Override
    public void init() {
        for (Tab tab : Tabs.get())
            add(new WTopBarButton(tab)).pad(metrics().topBarItemPad);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintTopBar(this, renderer, mouseX, mouseY);
    }

    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor();
    }

    public class WTopBarButton extends WPressable {
        private final Tab tab;
        private final RichText text;
        private final GuiTexture icon;

        private StyleAnimation selectedAnimation;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
            text = RichText.of(tab.name);
            icon = HontunIcons.getTabIcon(tab.getClass());
        }

        @Override
        public void init() {
            selectedAnimation = StyleAnimation.of(style().anim(AnimRole.TAB_SELECT), theme());
        }

        public WHontunTopBar bar() {
            return WHontunTopBar.this;
        }

        public Tab tab() {
            return tab;
        }

        public RichText text() {
            return text;
        }

        public GuiTexture icon() {
            return icon;
        }

        public boolean hasIcon() {
            return icon != null && theme().tabIcons.get();
        }

        public double iconSize() {
            return theme.textHeight();
        }

        public boolean isSelected() {
            return mc.gui.screen() instanceof TabScreen && ((TabScreen) mc.gui.screen()).tab == tab;
        }

        public double selectedProgress() {
            return selectedAnimation.getProgress();
        }

        public boolean isPressed() {
            return pressed;
        }

        @Override
        protected void onCalculateSize() {
            style().tabSize(this, size);
            width = size[0];
            height = size[1];
        }

        @Override
        protected void onPressed(int button) {
            Screen screen = mc.gui.screen();

            if (!(screen instanceof TabScreen) || ((TabScreen) screen).tab != tab) {
                double mouseX = mc.mouseHandler.xpos();
                double mouseY = mc.mouseHandler.ypos();

                tab.openScreen(theme);

                glfwSetCursorPos(

                        mc.getWindow().handle(),
                        mouseX,
                        mouseY
                );
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            boolean isSelected = isSelected();

            if (isSelected && !selectedAnimation.isRunning() && !selectedAnimation.isFinished())
                selectedAnimation.start();

            style().paintTab(this, renderer, mouseX, mouseY);
        }
    }
}
