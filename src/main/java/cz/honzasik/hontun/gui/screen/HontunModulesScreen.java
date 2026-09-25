package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.api.icons.HontunIcons;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.gui.util.search.results.ModuleSearchResult;
import cz.honzasik.hontun.gui.util.search.SearchUtils;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;

import net.minecraft.util.Util;

import net.minecraft.client.input.KeyEvent;

import org.jspecify.annotations.NonNull;

public class HontunModulesScreen extends TabScreen {
    private final HontunGuiTheme theme;
    private WCategoryController controller;

    private boolean showGrid = false;
    private boolean shouldSnap;
    private int gridSize;

    public HontunModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
        this.theme = (HontunGuiTheme) theme;
    }

    @Override
    public void initWidgets() {
        shouldSnap = theme.snapModuleCategories.get();
        gridSize = theme.snappingGridSize.get();

        controller = add(new WCategoryController()).widget();

        if (!theme.modulesHelpText.get()) return;

        WVerticalList help = add(theme.verticalList()).pad(4).bottom().widget();

        if (theme.hontunSearchScreen.get())
            help.add(helpLabel("Ctrl + F - Open search"));

        help.add(helpLabel("Left click - Toggle module"));
        help.add(helpLabel("Right click - Open module settings"));
    }

    private WLabel helpLabel(String text) {
        WLabel label = theme.label(text);
        if (theme.light()) label.color(HontunTheme.color(HontunTheme.textLight()));
        return label;
    }

    @Override
    protected void init() {
        super.init();
        controller.refresh();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractBackground(context, mouseX, mouseY, deltaTicks);

        if (!showGrid) return;

        int color = theme.overlay0Color().copy().a(60).getPacked();
        int windowWidth = Utils.getWindowWidth();
        int windowHeight = Utils.getWindowHeight();

        for (int x = 0; x <= windowWidth; x += gridSize) {
            context.verticalLine(x, 0, windowHeight, color);
        }

        for (int y = 0; y <= windowHeight; y += gridSize) {
            context.horizontalLine(0, windowWidth, y, color);
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        super.keyPressed(input);

        if (!theme.hontunSearchScreen.get()) return false;

        int keyCode = input.key();
        int modifiers = input.modifiers();

        boolean control = Util.getPlatform() == Util.OS.OSX ? modifiers == GLFW_MOD_SUPER : modifiers == GLFW_MOD_CONTROL;

        if (control && keyCode == GLFW_KEY_F) {
            mc.gui.setScreen(new HontunSearchScreen(theme));
            return true;
        }

        return false;
    }

    protected WWindow createCategory(WContainer c, Category category, List<Module> moduleList) {
        WGuiTexture icon = theme.categoryIcons()
                ? theme.texture(getIconForCategory(category), theme.textHeight())
                : null;

        WHontunWindow w = (WHontunWindow) theme.window(icon, category.name);
        w.id = category.name;
        w.padding = theme.pad();
        w.spacing = 0;

        if (shouldSnap) w.initSnapping(this, gridSize);

        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0;
        w.view.maxHeight -= 120;

        for (Module module : moduleList) {
            w.add(theme.module(module)).expandX();
        }

        return w;
    }

    protected void createSearchW(WContainer w, String text) {
        if (!text.isEmpty()) {
            int limit = Config.get().moduleSearchCount.get();

            List<ModuleSearchResult> modules = SearchUtils.searchModules(text, 50);

            if (!modules.isEmpty()) {
                WSection section = w.add(theme.section("Modules")).expandX().widget();
                section.spacing = 0;

                for (int i = 0; i < Math.min(modules.size(), limit); i++) {
                    ModuleSearchResult result = modules.get(i);
                    section.add(theme.module(result.module(), result.title())).expandX();
                }
            }

            List<Module> settingModules = Modules.get().searchSettingTitles(text).stream().toList();

            if (!settingModules.isEmpty()) {
                WSection section = w.add(theme.section("Settings")).expandX().widget();
                section.spacing = 0;

                for (int i = 0; i < Math.min(settingModules.size(), limit); i++) {
                    section.add(theme.module(settingModules.get(i))).expandX();
                }
            }
        }
    }

    protected WWindow createSearch(WContainer c) {
        WHontunWindow w = (WHontunWindow) theme.window(
                theme.texture(HontunBuiltinIcons.SEARCH.texture(), theme.textHeight()),
                "Search"
        );

        w.id = "search";

        if (shouldSnap) w.initSnapping(this, gridSize);

        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.maxHeight -= 20;

        WVerticalList l = theme.verticalList();

        WTextBox text = w.add(theme.textBox("", "Search modules...")).expandX().padBottom(4).widget();
        text.setFocused(true);
        text.action = () -> {
            l.clear();
            createSearchW(l, text.get());
        };

        w.add(l).expandX();
        createSearchW(l, text.get());

        return w;
    }

    protected Cell<WWindow> createFavorites(WContainer c) {
        boolean hasFavorites = Modules.get().getAll().stream().anyMatch(module -> module.favorite);
        if (!hasFavorites) return null;

        WHontunWindow w = (WHontunWindow) theme.window(
                theme.texture(HontunBuiltinIcons.BOOKMARK_YES.texture(), theme.textHeight()),
                "Favorites"
        );

        w.id = "favorites";
        w.padding = 0;
        w.spacing = 0;

        if (shouldSnap) w.initSnapping(this, gridSize);

        Cell<WWindow> cell = c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0;

        createFavoritesW(w);
        return cell;
    }

    protected boolean createFavoritesW(WWindow w) {
        List<Module> modules = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            if (module.favorite) {
                modules.add(module);
            }
        }

        modules.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name));

        for (Module module : modules) {
            w.add(theme.module(module)).expandX();
        }

        return !modules.isEmpty();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Modules.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Modules.get());
    }

    @Override
    public void reload() {}

    protected class WCategoryController extends WContainer {
        public final List<WWindow> windows = new ArrayList<>();
        private Cell<WWindow> favorites;

        @Override
        public void init() {
            for (Category category : Modules.loopCategories()) {
                List<Module> modules = Modules.get().getGroup(category);

                modules = modules.stream()
                        .filter(m -> !Config.get().hiddenModules.get().contains(m))
                        .toList();

                if (!modules.isEmpty()) {
                    windows.add(createCategory(this, category, modules));
                }
            }

            refresh();

            HontunGuiTheme hontunTheme = (HontunGuiTheme) theme;

            if (!hontunTheme.hontunSearchScreen.get())
                windows.add(createSearch(this));
        }

        protected void refresh() {
            if (favorites == null) {
                favorites = createFavorites(this);
                if (favorites != null) windows.add(favorites.widget());
            }
            else {
                favorites.widget().clear();

                if (!createFavoritesW(favorites.widget())) {
                    remove(favorites);
                    windows.remove(favorites.widget());
                    favorites = null;
                }
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            double pad = theme.scale(4);
            double h = theme.scale(40);

            double x = this.x + pad;
            double y = this.y;

            for (Cell<?> cell : cells) {
                double windowWidth = getWindowWidth();
                double windowHeight = getWindowHeight();

                if (x + cell.width > windowWidth) {
                    x = x + pad;
                    y += h;
                }

                if (x > windowWidth) {
                    x = windowWidth / 2.0 - cell.width / 2.0;
                    if (x < 0) x = 0;
                }
                if (y > windowHeight) {
                    y = windowHeight / 2.0 - cell.height / 2.0;
                    if (y < 0) y = 0;
                }

                cell.x = x;
                cell.y = y;

                cell.width = cell.widget().width;
                cell.height = cell.widget().height;

                cell.alignWidget();

                x += cell.width + pad;
            }
        }
    }

    private GuiTexture getIconForCategory(Category category) {
        GuiTexture icon = HontunIcons.getCategoryIcon(category.name);
        return icon != null ? icon : HontunBuiltinIcons.QUESTION_MARK.texture();
    }

    public void showGrid(boolean show) {
        showGrid = show;
    }

    public boolean showGrid() {
        return showGrid;
    }
}
