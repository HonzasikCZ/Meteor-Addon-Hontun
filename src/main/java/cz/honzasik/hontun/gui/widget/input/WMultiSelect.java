package cz.honzasik.hontun.gui.widget.input;

import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunCheckbox;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import net.minecraft.client.input.MouseButtonEvent;

public abstract class WMultiSelect<T> extends WVerticalList {
    protected final String title;
    protected boolean expanded;

    protected final List<T> items;
    protected List<T> filteredItems;

    protected FilterMode filterMode = FilterMode.ALL;
    private int selectedCount = 0;

    protected StyleAnimation animation;

    private Function<T, String> labelMapper = Object::toString;
    private Predicate<T> selectionPredicate = item -> false;
    private BiConsumer<T, Boolean> selectionChangeHandler;

    protected WHeader header;
    private WHontunCheckbox selectAllCheckbox;
    private WVerticalList itemContainer;

    public WMultiSelect(String title, List<T> items) {
        this.title = title;
        this.items = new ArrayList<>(items);
        filteredItems = new ArrayList<>(items);
    }

    @Override
    public void init() {
        HontunGuiTheme theme = (HontunGuiTheme) getTheme();

        Metrics m = theme.style().metrics();

        animation = StyleAnimation.of(theme.style().anim(AnimRole.MULTISELECT_EXPAND), theme, expanded);

        header = add(createHeader()).padBottom(m.gap).expandX().widget();

        if (items.size() > 1) {
            WHorizontalList list = add(theme.horizontalList()).expandX().pad(m.multiSelectItemPad).widget();

            selectAllCheckbox = (WHontunCheckbox) list.add(theme.checkbox(false)).padLeft(m.multiSelectCheckPad).widget();
            list.add(theme.label("Select all")).padLeft(m.gap).expandX();

            selectAllCheckbox.action = () -> {
                boolean shouldSelectAll = selectAllCheckbox.checked;
                filteredItems.forEach(item -> handleSelectionChange(item, shouldSelectAll));
                refreshItems();
            };

            add(theme.horizontalSeparator()).expandX();
        }

        itemContainer = add(theme.verticalList()).expandX().pad(m.multiSelectItemPad).widget();
        itemContainer.spacing = m.multiSelectItemSpacing;

        refreshItems();
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();

        height = (expanded || animation.isRunning()
                ? (height - header.height) * animation.getProgress() + header.height
                : header.height);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        boolean isAnimationRunning = animation.isRunning();

        if (isAnimationRunning) {
            invalidate();
            renderer.scissorStart(x, y, width, height);
        }

        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);

        if (isAnimationRunning) renderer.scissorEnd();

        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animation.getProgress() > 0 || widget instanceof WMultiSelect<?>.WHeader) {
            widget.render(renderer, mouseX, mouseY, delta);
        }
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return super.propagateEvents(widget) && (expanded || widget instanceof WMultiSelect<?>.WHeader);
    }

    protected abstract WHeader createHeader();

    protected WItem createItem(T item) {
        return new WItem(item);
    }

    public WMultiSelect<T> label(Function<T, String> labelMapper) {
        this.labelMapper = labelMapper;
        return this;
    }

    public WMultiSelect<T> isSelected(Predicate<T> selectionPredicate) {
        this.selectionPredicate = selectionPredicate;
        return this;
    }

    public WMultiSelect<T> onSelectionChange(BiConsumer<T, Boolean> handler) {
        this.selectionChangeHandler = handler;
        return this;
    }

    protected String getItemLabel(T item) {
        return labelMapper.apply(item);
    }

    protected boolean isItemSelected(T item) {
        return selectionPredicate.test(item);
    }

    protected void handleSelectionChange(T item, boolean selected) {
        if (selectionChangeHandler != null) selectionChangeHandler.accept(item, selected);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;

        animation.start(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public void updateFilter(String query) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        filteredItems.clear();

        if (normalizedQuery.isEmpty() && filterMode == FilterMode.ALL)
            filteredItems = new ArrayList<>(items);
        else filterItems(normalizedQuery);

        refreshItems();

        if (filteredItems.isEmpty()) setExpanded(false);
    }

    private void filterItems(String query) {
        for (T item : items) {
            if (matchesFilter(item, query))
                filteredItems.add(item);
        }
    }

    private boolean matchesFilter(T item, String query) {
        boolean matchesQuery = query.isEmpty()
                || getItemLabel(item).toLowerCase(Locale.ROOT).contains(query);

        boolean matchesFilterMode = switch (filterMode) {
            case ALL -> true;
            case SELECTED -> isItemSelected(item);
            case UNSELECTED -> !isItemSelected(item);
        };

        return matchesQuery && matchesFilterMode;
    }

    private void refreshItems() {
        itemContainer.clear();
        selectedCount = 0;

        for (T item : filteredItems) {
            if (isItemSelected(item)) selectedCount++;

            WItem itemWidget = createItem(item);
            itemContainer.add(itemWidget).expandX();
        }

        updateSelectAllState();
        updateHeaderLabel();
    }

    private void updateSelectAllState() {
        if (selectAllCheckbox != null) selectAllCheckbox.setChecked(selectedCount > 0);
    }

    private void updateHeaderLabel() {
        header.sizeLabel.set(header.getSizeLabel());
    }

    public abstract class WHeader extends WHorizontalList {
        protected String title;
        protected WHontunLabel sizeLabel;
        protected WTriangle triangle;

        public WHeader(String title) {
            this.title = title;
        }

        @Override
        public void init() {
            HontunGuiTheme theme = (HontunGuiTheme) getTheme();
            Metrics m = theme.style().metrics();

            add(theme.label(RichText.bold(title))).expandX().padVertical(m.multiSelectHeaderPadV).padLeft(m.multiSelectHeaderPadL);
            sizeLabel = (WHontunLabel) add(theme.label(getSizeLabel())).padHorizontal(m.gap).widget();

            triangle = add(theme.triangle()).widget();
            triangle.action = () -> setExpanded(!expanded);
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (triangle != null) triangle.rotation = 90 + 90 * animation.getProgress();
            return super.render(renderer, mouseX, mouseY, delta);
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean used) {
            if (mouseOver

                && click.button() == GLFW_MOUSE_BUTTON_LEFT

                && !used
            ) {
                onClick();
                return true;
            }

            return false;
        }

        protected void onClick() {
            setExpanded(!expanded);
        }

        protected RichText getSizeLabel() {
            return RichText.of("(" + selectedCount + "/" + filteredItems.size() + ")")
                    .scale(TextScale.SMALL.get());
        }
    }

    public class WItem extends WHorizontalList {
        protected final T item;
        protected WHontunCheckbox checkbox;

        public WItem(T item) {
            this.item = item;
        }

        @Override
        public void init() {
            Metrics m = ((HontunGuiTheme) getTheme()).style().metrics();
            boolean selected = isItemSelected(item);
            checkbox = (WHontunCheckbox) add(theme.checkbox(selected)).padLeft(m.multiSelectCheckPad).widget();
            checkbox.action = this::onSelection;

            add(theme.label(getItemLabel(item))).padLeft(m.gap).expandX();
        }

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();
            height *= ((HontunGuiTheme) getTheme()).style().metrics().multiSelectRowScale;
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean used) {
            if (mouseOver

                && click.button() == GLFW_MOUSE_BUTTON_LEFT

                && !used
                && !checkbox.mouseOver
            ) {
                checkbox.setChecked(!checkbox.checked);
                onSelection();
                return true;
            }

            return false;
        }

        private void onSelection() {
            boolean selected = checkbox.checked;

            selectedCount += selected ? 1 : -1;
            selectedCount = Math.clamp(selectedCount, 0, items.size());

            updateSelectAllState();
            updateHeaderLabel();

            handleSelectionChange(item, selected);
        }
    }

    public enum FilterMode {
        ALL,
        SELECTED,
        UNSELECTED
    }
}
