package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class FreeInteract extends Module {
    private final SettingGroup defaultGroup = settings.getDefaultGroup();

    public final Setting<Boolean> useAndAttack = defaultGroup.add(new BoolSetting.Builder()
        .name("use-and-attack")
        .description("Lets you attack while using an item.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> interactInBoat = defaultGroup.add(new BoolSetting.Builder()
        .name("interact-in-boat")
        .description("Lets you attack and use items in a boat while moving.")
        .defaultValue(true)
        .build()
    );

    public FreeInteract() {
        super(Hontun.CATEGORY, "free-interact", "Removes interaction limits.");
    }
}
