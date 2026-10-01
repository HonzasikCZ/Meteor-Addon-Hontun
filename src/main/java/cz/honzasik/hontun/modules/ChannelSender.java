package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.screen.ActiveLabel;
import cz.honzasik.hontun.utils.MCUtil;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

public class ChannelSender extends Module implements ActiveLabel {
    public enum Format {
        Utf8,
        Hex,
        Int32
    }

    private static final int MAX_PAYLOAD = 32767;

    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgData = settings.createGroup("Data");
    private final SettingGroup sgRepeat = settings.createGroup("Repeat");
    private final SettingGroup sgAuto = settings.createGroup("Auto send");

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

    private final Setting<Format> format = sgData.add(new EnumSetting.Builder<Format>()
        .name("Format")
        .description("How the Data field is turned into payload bytes.")
        .defaultValue(Format.Utf8)
        .build()
    );

    private final Setting<String> data = sgData.add(new StringSetting.Builder()
        .name("Data")
        .description("Payload. Utf8: raw text. Hex: byte pairs like 00 03 E8 (spaces and 0x ignored). Int32: a number (decimal or 0x hex) sent as 4 big-endian bytes.")
        .defaultValue("{\"message\":\"text\"}")
        .build()
    );

    private final Setting<Boolean> repeat = sgRepeat.add(new BoolSetting.Builder()
        .name("Repeat")
        .description("Send the payload multiple times in one burst.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> amount = sgRepeat.add(new IntSetting.Builder()
        .name("Amount")
        .description("How many packets to send in the burst.")
        .visible(repeat::get)
        .defaultValue(1)
        .min(1)
        .max(20000)
        .sliderMin(1)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> interval = sgAuto.add(new IntSetting.Builder()
        .name("Interval")
        .description("While Auto send is on, send the payload (or the whole burst) every this many ticks. 20 ticks = 1 second.")
        .defaultValue(20)
        .min(1)
        .sliderRange(1, 200)
        .build()
    );

    private int timer;

    public ChannelSender() {
        super(Hontun.CATEGORY, "Channel Sender", "Sends custom payloads to custom channels. Send once for a single send, Auto send to keep sending.");
    }

    @Override
    public String activeLabel() {
        return "Auto send";
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton send = theme.button("Send once");
        send.action = () -> send(true);
        return send;
    }

    @Override
    public void onActivate() {
        timer = 0;
        if (!send(false)) {
            toggle();
            return;
        }
        info("Auto send on, every (highlight)%d(default) tick(s).", interval.get());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (++timer < interval.get()) return;
        timer = 0;
        if (!send(false)) toggle();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (isActive()) toggle();
    }

    private boolean send(boolean report) {
        if (MCUtil.getPlayNetHandler() == null) {
            error("Not connected to server");
            return false;
        }

        Identifier channel;
        try {
            channel = Identifier.fromNamespaceAndPath(namespace.get(), path.get());
        } catch (Exception e) {
            error("Invalid channel identifier: %s:%s", namespace.get(), path.get());
            return false;
        }

        byte[] bytes;
        try {
            bytes = buildPayload();
        } catch (Exception e) {
            error("Invalid %s data: %s", format.get(), e.getMessage());
            return false;
        }

        if (bytes.length > MAX_PAYLOAD) {
            error("Payload is %d bytes, the server rejects anything over %d.", bytes.length, MAX_PAYLOAD);
            return false;
        }

        int count = repeat.get() ? amount.get() : 1;
        int sent = 0;
        try {
            for (; sent < count; sent++) {
                MCUtil.sendCustomPayload(channel, bytes);
            }
        } catch (Exception e) {
            error("Sending failed after %d packet(s): %s", sent, e.getMessage());
            return false;
        }

        if (report) info("Sent %d packet(s) on (highlight)%s(default) (%d bytes each).", sent, channel, bytes.length);
        return true;
    }

    private byte[] buildPayload() {
        String raw = data.get();
        switch (format.get()) {
            case Hex: {
                StringBuilder sb = new StringBuilder();
                for (String token : raw.trim().split("\\s+")) {
                    if (token.isEmpty()) continue;
                    if (token.length() >= 2 && token.charAt(0) == '0' && (token.charAt(1) == 'x' || token.charAt(1) == 'X')) {
                        token = token.substring(2);
                    }
                    for (int i = 0; i < token.length(); i++) {
                        char c = token.charAt(i);
                        boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
                        if (!hex) throw new IllegalArgumentException("'" + c + "' is not a hex digit");
                        sb.append(c);
                    }
                }
                String clean = sb.toString();
                if (clean.length() % 2 != 0) {
                    throw new IllegalArgumentException("odd number of hex digits");
                }
                byte[] out = new byte[clean.length() / 2];
                for (int i = 0; i < out.length; i++) {
                    out[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
                }
                return out;
            }
            case Int32: {
                String t = raw.trim();
                boolean neg = t.startsWith("-");
                if (neg || t.startsWith("+")) t = t.substring(1);
                long v;
                if (t.regionMatches(true, 0, "0x", 0, 2)) {
                    v = Long.parseLong(t.substring(2), 16);
                } else {
                    v = Long.parseLong(t, 10);
                }
                if (neg) v = -v;
                if (v < Integer.MIN_VALUE || v > 0xFFFFFFFFL) {
                    throw new IllegalArgumentException("out of 32-bit range");
                }
                int i = (int) v;
                return new byte[] {
                    (byte) (i >>> 24),
                    (byte) (i >>> 16),
                    (byte) (i >>> 8),
                    (byte) i
                };
            }
            default:
                return raw.getBytes(StandardCharsets.UTF_8);
        }
    }
}
