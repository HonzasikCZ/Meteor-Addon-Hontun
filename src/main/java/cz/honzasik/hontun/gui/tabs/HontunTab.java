package cz.honzasik.hontun.gui.tabs;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.mixin.meteorclient.GuiThemesAccessor;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalClickStyle;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HontunTab extends Tab {
    private static boolean synced;

    public HontunTab() {
        super("Hontun");
    }

    public static void register() {
        HontunTab tab = new HontunTab();
        Tabs.add(tab);

        List<Tab> tabs = Tabs.get();
        int gui = tabs.indexOf(Tabs.get(GuiTab.class));
        if (gui < 0) return;

        tabs.remove(tab);
        tabs.add(gui + 1, tab);
    }

    public static boolean terminal(GuiTheme theme) {
        return theme instanceof HontunGuiTheme hontun && hontun.style() instanceof TerminalClickStyle;
    }

    public static void reopen() {
        mc.gui.setScreen(null);
        Tabs.get(HontunTab.class).openScreen(GuiThemes.get());
    }

    public static void saveDetached() {
        HontunGuiTheme theme = Hontun.THEME;
        GuiTheme current = GuiThemes.get();
        if (theme == null || current == null) return;
        if (current == theme) {
            synced = true;
            return;
        }
        if (!synced) return;

        try {
            File folder = GuiThemesAccessor.hontun$themesFolder();
            folder.mkdirs();
            NbtIo.write(theme.toTag(), new File(folder, theme.name + ".nbt").toPath());
        } catch (IOException e) {
            Hontun.LOG.warn("[Hontun] Could not save the Hontun settings: {}", e.toString());
        }
    }

    public static void loadDetached() {
        HontunGuiTheme theme = Hontun.THEME;
        GuiTheme current = GuiThemes.get();
        if (theme == null || current == null || synced) return;
        if (current == theme) {
            synced = true;
            return;
        }

        File file = new File(GuiThemesAccessor.hontun$themesFolder(), theme.name + ".nbt");
        try {
            if (file.exists()) {
                CompoundTag tag = NbtIo.read(file.toPath());
                if (tag != null) theme.fromTag(tag);
            }
            synced = true;
        } catch (IOException e) {
            Hontun.LOG.warn("[Hontun] Could not load the Hontun settings: {}", e.toString());
        }
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HontunScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HontunScreen;
    }

    private static class HontunScreen extends WindowTabScreen {
        HontunScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            loadDetached();
            Hontun.THEME.hontunSettings.onActivated();
            onClosed(HontunTab::saveDetached);
        }

        @Override
        public void initWidgets() {
            if (!terminal(theme)) {
                WHorizontalList opts = add(theme.horizontalList()).expandX().widget();

                WButton reset = opts.add(theme.button("Reset")).expandX().widget();
                reset.action = () -> {
                    Hontun.THEME.hontunSettings.reset();
                    reopen();
                };

                WButton copy = opts.add(theme.button(GuiRenderer.COPY)).widget();
                copy.action = this::toClipboard;
                copy.tooltip = "Copy config";

                WButton paste = opts.add(theme.button(GuiRenderer.PASTE)).right().widget();
                paste.action = this::fromClipboard;
                paste.tooltip = "Paste config";
            }

            add(theme.settings(Hontun.THEME.hontunSettings)).expandX();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Hontun.THEME);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Hontun.THEME);
        }
    }
}
