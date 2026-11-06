package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.FooterKind;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class TerminalModuleScreen extends WidgetScreen {
    private final HontunGuiTheme theme;
    private final Module module;
    private final Setting<?> focus;

    private WTermConfig config;

    public TerminalModuleScreen(HontunGuiTheme theme, Module module, Setting<?> focus) {
        super(theme, module.title);
        this.theme = theme;
        this.module = module;
        this.focus = focus;

        if (focus != null) {
            for (SettingGroup group : module.settings.groups) {
                for (Setting<?> s : group) {
                    if (s == focus) group.sectionExpanded = true;
                }
            }
        }
    }

    public Module module() {
        return module;
    }

    public String file() {
        return module.name + ".conf";
    }

    @Override
    public void initWidgets() {
        TerminalGrid g = TerminalGrid.of(theme);
        if (theme.style() instanceof TerminalClickStyle style) style.syncMetrics(theme);

        add(theme.topBar()).top().centerX();

        WHontunWindow window = (WHontunWindow) theme.window(null, file());
        window.subtitle = "  ~/.hontun/" + WTermConfig.key(module.category.name) + "/";
        window.padding = 0;

        double top = g.tb + g.px(22);
        add(window).top().centerX().marginTop(g.unscaled(top));
        window.view.scrollOnlyWhenMouseOver = false;

        double chrome = g.tb + 2 * g.px(4) + g.st + g.ml;
        window.view.maxHeight = Math.max(3 * g.lh, Utils.getWindowHeight() - g.tb - g.px(14) - top - chrome);

        config = new WTermConfig(module.settings, "", module);
        config.focus(focus);
        window.add(config).expandX();

        window.addDirect(new WTermStatusLine(config)).expandX();
        window.addDirect(new WTermMessageLine(config)).expandX();

        WWidget footer = theme.style().footer(theme, FooterKind.MODULE);
        if (footer != null) add(footer).bottom().expandX();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (Modules.get().isBinding()) return false;
        return config == null || !config.consumesEscape();
    }

    @Override
    public boolean toClipboard() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", module.name);

        CompoundTag settingsTag = module.settings.toTag();
        if (!settingsTag.isEmpty()) tag.put("settings", settingsTag);

        boolean ok = NbtUtils.toClipboard(tag);
        if (ok) {
            TerminalState.dirty(module, false);
            TerminalLog.post("\"" + file() + "\" " + (config != null ? config.lineCount() : 0) + "L copied to clipboard");
        }
        return ok;
    }

    @Override
    public boolean fromClipboard() {
        CompoundTag tag = NbtUtils.fromClipboard();
        if (tag == null || !tag.getStringOr("name", "").equals(module.name)) {
            TerminalLog.post("\"" + file() + "\": clipboard holds no config for " + module.name);
            return false;
        }

        Optional<CompoundTag> settings = tag.getCompound("settings");
        if (settings.isPresent()) module.settings.fromTag(settings.get());
        else module.settings.reset();

        if (parent instanceof WidgetScreen p) p.reload();
        if (config != null) config.reload();

        TerminalState.dirty(module, false);
        TerminalLog.post("\"" + file() + "\" pasted from clipboard");
        return true;
    }
}
