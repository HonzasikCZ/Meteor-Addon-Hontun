package cz.honzasik.hontun.gui.theme.widgets.settings;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunButton;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;

public class WHontunKeybind extends WKeybind implements HontunWidget {
    public Runnable action;
    public Runnable actionOnSet;

    private WHontunButton button;

    private final String title;
    private final Keybind keybind;
    private final Keybind defaultValue;

    private final String listeningText = "Press any key";
    private boolean listening;

    public WHontunKeybind(String title, Keybind keybind, Keybind defaultValue) {
        super(keybind, defaultValue);
        this.title = title != null && !title.isEmpty() ? title : "Bind";
        this.keybind = keybind;
        this.defaultValue = defaultValue;
    }

    @Override
    protected void onCalculateSize() {
        button.width = Math.max(theme.textWidth(listeningText), button.width);
        super.onCalculateSize();
    }

    @Override
    public void init() {
        button = add(theme().button(RichText.of(""))).widget();
        button.action = () -> {
            listening = true;
            button.set(listeningText);

            if (actionOnSet != null) actionOnSet.run();
        };

        refreshLabel();
    }

    @Override
    public boolean onClear() {
        if (listening) {
            keybind.reset();
            reset();

            return true;
        }

        return false;
    }

    @Override
    public boolean onAction(boolean isKey, int value, int modifiers) {
        if (listening && keybind.canBindTo(isKey, value, modifiers)) {
            keybind.set(isKey, value, modifiers);
            reset();

            return true;
        }

        return false;
    }

    @Override
    public void resetBind() {
        keybind.set(defaultValue);
        reset();
    }

    @Override
    public void reset() {
        listening = false;
        refreshLabel();
        if (Modules.get().isBinding()) {
            Modules.get().setModuleToBind(null);
        }
    }

    private void refreshLabel() {
        button.set(RichText.bold(title + ": ").append(keybind.toString()));
    }
}
