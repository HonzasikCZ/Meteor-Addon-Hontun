package cz.honzasik.hontun.gui.theme.style.flat;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;

public class FlatSectionTitle extends WHorizontalList {
    private final String title;

    public FlatSectionTitle(String title) {
        this.title = title;
    }

    @Override
    public void init() {
        spacing = 8;

        add(new FlatTick()).centerY();
        add(theme.label(title, true)).centerY();
    }

    @Override
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();

        if (parent == null) return;

        double shift = parent.x - x;
        if (shift >= 0) return;

        for (Cell<?> cell : cells) cell.move(shift, 0);
    }
}
