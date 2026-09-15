package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import cz.honzasik.hontun.gui.util.WidgetUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class HontunModuleScreen extends WindowScreen {
    private final HontunGuiTheme theme;
    private final Module module;

    private WContainer settingsContainer;
    private WHontunKeybind keybind;
    private WCheckbox active;

    public HontunModuleScreen(HontunGuiTheme theme, Module module) {
        super(theme, theme.favorite(module.favorite), module.title);
        ((WFavorite) window.icon).action = () -> module.favorite = ((WFavorite) window.icon).checked;

        this.theme = theme;
        this.module = module;
    }

    @Override
    public void initWidgets() {
        double pad = theme.pad();

        WHontunWindow window = (WHontunWindow) this.window;
        window.view.spacing = 0;

        WVerticalList moduleInfo = window.add(theme.verticalList()).padHorizontal(pad).padBottom(pad).expandX().widget();
        moduleInfo.spacing = pad;

        moduleInfo.add(theme.label(module.description, getWindowWidth() / 4.0));

        if (module.addon != null && module.addon != MeteorClient.ADDON) {
            WHorizontalList addon = moduleInfo.add(theme.horizontalList()).expandX().widget();
            addon.add(theme.label("From: ").color(theme.textSecondaryColor()));
            addon.add(theme.label(module.addon.name).color(theme.accentColor()));
        }

        WHorizontalList bind = moduleInfo.add(
                theme.horizontalList()).expandX().padVertical(pad).widget();
        bind.spacing = pad;

        keybind = bind.add(theme.hontunKeybind(module.keybind)).widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        BindAction bindAction = module.toggleOnBindRelease ? BindAction.HOLD : BindAction.TOGGLE;

        WDropdown<BindAction> bindActionWidget = bind.add(theme.dropdown(bindAction)).expandCellX().widget();
        bindActionWidget.action = () -> module.toggleOnBindRelease = bindActionWidget.get().isHold();
        bindActionWidget.tooltip = "Determines whether the module toggles or remains active only while holding the key.";

        WidgetUtils.reset(
                bind,
                null,
                () -> {
                    BindAction toggle = BindAction.TOGGLE;
                    keybind.resetBind();
                    module.toggleOnBindRelease = toggle.isHold();
                    bindActionWidget.set(toggle);
                },
                () -> bind.mouseOver
        );

        WHorizontalList cf = moduleInfo.add(theme.horizontalList()).expandX().widget();

        WCheckbox cfC = cf.add(theme.checkbox(module.chatFeedback)).widget();
        cfC.action = () -> module.chatFeedback = cfC.checked;

        WLabel cfLabel = theme.label("Chat Feedback");
        cfLabel.tooltip = "Displays a toggle message in chat when enabled.";
        cf.add(cfLabel);

        moduleInfo.add(theme.horizontalSeparator()).expandX();

        if (!module.settings.groups.isEmpty()) {
            settingsContainer = window.add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(module.settings)).expandX();
        }

        WWidget widget = module.getWidget(theme);

        if (widget != null) {
            window.add(theme.horizontalSeparator()).pad(pad).expandX();

            WContainer container = window.add(theme.horizontalList()).expandX().padHorizontal(pad).widget();
            Cell<WWidget> cell = container.add(widget);

            if (widget instanceof WContainer) cell.expandX();
        }

        double windowPadding = window.padding;

        if (!module.settings.groups.isEmpty() || widget != null)
            window.addDirect(theme.horizontalSeparator()).padHorizontal(windowPadding * 2).expandX();

        WHorizontalList bottom = window.addDirect(theme.horizontalList())
                .expandX()
                .padHorizontal(windowPadding * 2)
                .padVertical(windowPadding)
                .widget();

        active = bottom.add(theme.checkbox(module.isActive())).widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };

        bottom.add(theme.label(RichText.of("Active"))).expandCellX().padLeft(4);

        WHorizontalList sharing = bottom.add(theme.horizontalList()).right().widget();
        WButton copy = sharing.add(theme.button(HontunBuiltinIcons.COPY.texture())).widget();
        copy.action = () -> {
            if (toClipboard()) {
                OkPrompt.create()
                        .title("Module copied!")
                        .message("The settings for this module are now in your clipboard.")
                        .message("You can also copy settings using Ctrl+C.")
                        .message("Settings can be imported using Ctrl+V or the paste button.")
                        .id("config-sharing-guide")
                        .show();
            }
        };
        copy.tooltip = "Copy config";

        WButton paste = sharing.add(theme.button(HontunBuiltinIcons.IMPORT.texture())).widget();
        paste.action = this::fromClipboard;
        paste.tooltip = "Paste config";
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public void tick() {
        super.tick();

        module.settings.tick(settingsContainer, theme);
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        keybind.reset();
    }

    @EventHandler
    private void onActiveModulesChanged(ActiveModulesChangedEvent event) {
        this.active.checked = module.isActive();
    }

    @Override
    public boolean toClipboard() {
        CompoundTag tag = new CompoundTag();

        tag.putString("name", module.name);

        CompoundTag settingsTag = module.settings.toTag();
        if (!settingsTag.isEmpty()) tag.put("settings", settingsTag);

        return writeClipboardTag(tag);
    }

    @Override
    public boolean fromClipboard() {
        CompoundTag tag = readClipboardTag();
        if (tag == null) return false;

        if (!applySettingsFromTag(tag)) return false;

        if (parent instanceof WidgetScreen p) p.reload();
        reload();

        return true;
    }

    private CompoundTag readClipboardTag() {
        return NbtUtils.fromClipboard();
    }

    private boolean applySettingsFromTag(CompoundTag tag) {
        if (!tag.getStringOr("name", "").equals(module.name)) return false;

        Optional<CompoundTag> settings = tag.getCompound("settings");

        if (settings.isPresent()) module.settings.fromTag(settings.get());
        else module.settings.reset();

        return true;
    }

    private boolean writeClipboardTag(CompoundTag tag) {
        return NbtUtils.toClipboard(tag);
    }

    private enum BindAction {
        TOGGLE(false),
        HOLD(true);

        private final boolean hold;

        BindAction(boolean hold) {
            this.hold = hold;
        }

        public boolean isHold() {
            return hold;
        }
    }
}
