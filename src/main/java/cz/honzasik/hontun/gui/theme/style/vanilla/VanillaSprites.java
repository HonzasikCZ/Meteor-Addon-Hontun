package cz.honzasik.hontun.gui.theme.style.vanilla;

import net.minecraft.resources.Identifier;

final class VanillaSprites {
    static final Identifier BUTTON = sprite("widget/button");
    static final Identifier BUTTON_HIGHLIGHTED = sprite("widget/button_highlighted");
    static final Identifier BUTTON_DISABLED = sprite("widget/button_disabled");
    static final Identifier TEXT_FIELD = sprite("widget/text_field");
    static final Identifier TEXT_FIELD_HIGHLIGHTED = sprite("widget/text_field_highlighted");
    static final Identifier SLIDER = sprite("widget/slider");
    static final Identifier SLIDER_HIGHLIGHTED = sprite("widget/slider_highlighted");
    static final Identifier SLIDER_HANDLE = sprite("widget/slider_handle");
    static final Identifier SLIDER_HANDLE_HIGHLIGHTED = sprite("widget/slider_handle_highlighted");
    static final Identifier CHECKBOX = sprite("widget/checkbox");
    static final Identifier CHECKBOX_HIGHLIGHTED = sprite("widget/checkbox_highlighted");
    static final Identifier CHECKBOX_SELECTED = sprite("widget/checkbox_selected");
    static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = sprite("widget/checkbox_selected_highlighted");
    static final Identifier SCROLLER = sprite("widget/scroller");
    static final Identifier SCROLLER_BACKGROUND = sprite("widget/scroller_background");
    static final Identifier TAB = sprite("widget/tab");
    static final Identifier TAB_HIGHLIGHTED = sprite("widget/tab_highlighted");
    static final Identifier TAB_SELECTED = sprite("widget/tab_selected");
    static final Identifier TAB_SELECTED_HIGHLIGHTED = sprite("widget/tab_selected_highlighted");
    static final Identifier PANEL = sprite("recipe_book/overlay_recipe");
    static final Identifier SORT_DOWN = sprite("statistics/sort_down");
    static final Identifier SORT_UP = sprite("statistics/sort_up");
    static final Identifier SEARCH = sprite("icon/search");

    private VanillaSprites() {}

    private static Identifier sprite(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
