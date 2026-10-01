package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.HoverTarget;
import cz.honzasik.hontun.gui.widget.input.WMultiSelect;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

import java.util.List;

public class WHontunMultiSelect<T> extends WMultiSelect<T> implements HontunWidget {
    public WHontunMultiSelect(String title, List<T> items) {
        super(title, items);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintMultiSelectBody(this, renderer, mouseX, mouseY);
    }

    @Override
    protected WHeader createHeader() {
        return new WHontunHeader(title);
    }

    @Override
    protected WItem createItem(T item) {
        return new WHontunItem(item);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean animating() {
        return animation.isRunning();
    }

    public double expandProgress() {
        return animation.getProgress();
    }

    public WHeader headerWidget() {
        return header;
    }

    public String titleText() {
        return title;
    }

    public class WHontunHeader extends WHeader {
        public WHontunHeader(String title) {
            super(title);
        }

        public WHontunMultiSelect<T> owner() {
            return WHontunMultiSelect.this;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintMultiSelectHeader(this, renderer, mouseX, mouseY);
        }
    }

    public class WHontunItem extends WItem implements HoverTarget {
        public WHontunItem(T item) {
            super(item);
        }

        public WHontunMultiSelect<T> owner() {
            return WHontunMultiSelect.this;
        }

        public boolean checkboxHovered() {
            return checkbox.mouseOver;
        }

        public T item() {
            return item;
        }

        @Override
        public boolean hoverLit() {
            return mouseOver && !checkbox.mouseOver;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintMultiSelectItem(this, renderer, mouseX, mouseY);
        }
    }
}
