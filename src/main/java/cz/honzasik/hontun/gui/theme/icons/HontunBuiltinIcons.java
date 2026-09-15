package cz.honzasik.hontun.gui.theme.icons;

import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.api.icons.HontunIcons;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.FriendsTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.MacrosTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ModulesTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ProfilesTab;
import meteordevelopment.meteorclient.systems.modules.Categories;

import java.util.Locale;

public enum HontunBuiltinIcons {
    ARROW,
    BOOKMARK_NO,
    BOOKMARK_YES,
    BRUSH,
    COPY,
    CUBE,
    EDIT,
    EYE,
    GRID,
    IMPORT,
    MINUS,
    MOUSE,
    MOVEMENT,
    PEOPLE,
    PERSON,
    PLUS,
    QUESTION_MARK,
    RESET,
    SEARCH,
    SETTING,
    SWORD,
    TICK;

    private final String path;
    private GuiTexture texture;

    HontunBuiltinIcons() {
        this.path = "textures/icons/gui/" + name().toLowerCase(Locale.ROOT) + ".png";
    }

    public static void init() {
        for (HontunBuiltinIcons icon : values())
            icon.initIcon();

        HontunIcons.registerCategoryIcon(Categories.Combat.name, SWORD.texture());
        HontunIcons.registerCategoryIcon(Categories.Player.name, PERSON.texture());
        HontunIcons.registerCategoryIcon(Categories.Movement.name, MOVEMENT.texture());
        HontunIcons.registerCategoryIcon(Categories.Render.name, EYE.texture());
        HontunIcons.registerCategoryIcon(Categories.World.name, CUBE.texture());

        HontunIcons.registerTabIcon(ModulesTab.class, CUBE.texture());
        HontunIcons.registerTabIcon(ConfigTab.class, SETTING.texture());
        HontunIcons.registerTabIcon(GuiTab.class, BRUSH.texture());
        HontunIcons.registerTabIcon(HudTab.class, GRID.texture());
        HontunIcons.registerTabIcon(FriendsTab.class, PEOPLE.texture());
        HontunIcons.registerTabIcon(MacrosTab.class, MOUSE.texture());
        HontunIcons.registerTabIcon(ProfilesTab.class, PERSON.texture());

        try {
            Class<? extends Tab> pathManagerClass = Class
                    .forName("meteordevelopment.meteorclient.gui.tabs.builtin.PathManagerTab")
                    .asSubclass(Tab.class);

            HontunIcons.registerTabIcon(pathManagerClass, MOVEMENT.texture());
        } catch (ClassNotFoundException ignored) { }
    }

    public void initIcon() {
        try {
            this.texture = GuiRenderer.addTexture(HontunGui.identifier(path));
        } catch (Exception e) {
            throw new RuntimeException("Icon '" + name() + "' could not be loaded.");
        }
    }

    public GuiTexture texture() {
        if (texture == null) throw new IllegalStateException("Icon " + name() + " not initialized.");
        return texture;
    }
}
