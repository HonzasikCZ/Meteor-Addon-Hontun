package cz.honzasik.hontun.gui.widget;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;

@SuppressWarnings("unused")
public interface IWidgetBackport {
    boolean hontun$isFocused();
    boolean hontun$isSelfFocused();

    void hontun$setFocused(boolean focused);

    WView hontun$getView();
    boolean hontun$isWidgetInView(WWidget widget);
}
