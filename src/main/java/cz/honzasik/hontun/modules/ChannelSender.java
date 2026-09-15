package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.utils.MCUtil;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

public class ChannelSender extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgData = settings.createGroup("Data");
    private final SettingGroup sgRepeat = settings.createGroup("Repeat");

    private final Setting<String> namespace = sgGeneral.add(new StringSetting.Builder()
        .name("Namespace")
        .description("Channel identifier namespace.")
        .defaultValue("plugin")
        .build()
    );

    private final Setting<String> path = sgGeneral.add(new StringSetting.Builder()
        .name("Path")
        .description("Channel identifier path.")
        .defaultValue("cloudsync")
        .build()
    );

    private final Setting<String> data = sgData.add(new StringSetting.Builder()
        .name("Data")
        .description("Raw UTF-8 payload sent on the channel.")
        .defaultValue("{\"message\":\"text\"}")
        .build()
    );

    private final Setting<Boolean> repeat = sgRepeat.add(new BoolSetting.Builder()
        .name("Repeat")
        .description("Send the payload multiple times.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> amount = sgRepeat.add(new IntSetting.Builder()
        .name("Amount")
        .description("How many packets to send.")
        .visible(repeat::get)
        .defaultValue(1)
        .min(1)
        .max(128)
        .sliderMin(1)
        .sliderMax(128)
        .build()
    );

    private final Setting<Boolean> disableOnKick = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable On Kick")
        .description("Disable the module when leaving the server.")
        .defaultValue(false)
        .build()
    );

    public ChannelSender() {
        super(Hontun.CATEGORY, "Channel Sender", "Sends custom payloads to custom channels.");
    }

    @Override
    public void onActivate() {
        if (MCUtil.getPlayNetHandler() == null) {
            error("Not connected to server");
            toggle();
            return;
        }

        Identifier channel;
        try {
            channel = Identifier.fromNamespaceAndPath(namespace.get(), path.get());
        } catch (Exception e) {
            error("Invalid channel identifier: %s:%s", namespace.get(), path.get());
            toggle();
            return;
        }

        byte[] bytes = data.get().getBytes(StandardCharsets.UTF_8);
        int count = repeat.get() ? amount.get() : 1;
        for (int i = 0; i < count; i++) {
            MCUtil.sendCustomPayload(channel, bytes);
        }

        info("Sent %d packet(s) on (highlight)%s(default) (%d bytes each).", count, channel, bytes.length);
        toggle();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (disableOnKick.get()) toggle();
    }
}
