package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NoWorldBorder extends Module {
    private final SettingGroup defaultGroup = settings.getDefaultGroup();

    public final Setting<Boolean> collision = defaultGroup.add(new BoolSetting.Builder()
        .name("collision")
        .description("Also remove the invisible wall (client-side border collision), not just the visual.")
        .defaultValue(true)
        .build()
    );

    public NoWorldBorder() {
        super(Hontun.CATEGORY, "no-world-border", "Removes the world border on the client (visual + local collision).");
    }
}
