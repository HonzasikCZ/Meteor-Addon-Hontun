package cz.honzasik.hontun.gui.api.icons;

import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;

import java.util.HashMap;
import java.util.Map;

public class HontunIcons {
    private static final Map<String, GuiTexture> CATEGORY_ICONS = new HashMap<>();
    private static final Map<Class<? extends Tab>, GuiTexture> TAB_ICONS = new HashMap<>();

    public static void registerCategoryIcon(String categoryName, GuiTexture texture) {
        CATEGORY_ICONS.put(categoryName, texture);
    }

    public static GuiTexture getCategoryIcon(String categoryName) {
        return CATEGORY_ICONS.get(categoryName);
    }

    public static void registerTabIcon(Class<? extends Tab> tabClass, GuiTexture texture) {
        TAB_ICONS.put(tabClass, texture);
    }

    public static GuiTexture getTabIcon(Class<? extends Tab> tabClass) {
        return TAB_ICONS.get(tabClass);
    }
}
