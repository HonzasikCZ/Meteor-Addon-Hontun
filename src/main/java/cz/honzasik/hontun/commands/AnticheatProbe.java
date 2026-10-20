package cz.honzasik.hontun.commands;

import cz.honzasik.hontun.modules.AnticheatDetect;
import cz.honzasik.hontun.modules.AnticheatDetect.Verdict;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class AnticheatProbe {
    private AnticheatProbe() {}

    public static void run() {
        if (MCUtil.getPlayNetHandler() == null) {
            ChatUtils.error("Not connected to a server.");
            return;
        }
        if (MCUtil.MC.hasSingleplayerServer()) {
            ChatUtils.error("Only works on multiplayer servers.");
            return;
        }
        ChatUtils.sendMsg(format(AnticheatDetect.COLLECTOR.evaluate(AnticheatDetect.address()), true));
    }

    public static String summary(Verdict v) {
        if (v.name() == null) return "none (" + v.reason() + ")";
        if (v.name().equals("Unknown")) return "unknown (" + unknownReason(v) + ")";
        String out = v.name() + " (" + v.confidence() + ", " + v.source() + ")";
        return v.also() == null ? out : out + ", " + summary(v.also());
    }

    public static MutableComponent format(Verdict v, boolean details) {
        MutableComponent msg = HontunChat.light("Anticheat: ");
        headline(msg, v);
        if (v.also() != null) headline(msg.append(HontunChat.punct(", ")), v.also());
        if (!details) return msg;

        msg.append(HontunChat.light("\nPlugins: ")).append(v.plugins() == null ? HontunChat.light("not scanned") : list(v.plugins()));
        msg.append(HontunChat.light("\nCommands: ")).append(list(v.commands()));
        msg.append(HontunChat.light("\nChannels: ")).append(list(v.channels()));
        List<Integer> ids = v.pings();
        msg.append(HontunChat.light("\nPings (" + ids.size() + "): ")).append(ids.isEmpty() ? HontunChat.light("none") : HontunChat.value(join(ids, false)));
        msg.append(HontunChat.light("\nSteps: ")).append(ids.size() < 2 ? HontunChat.light("none") : HontunChat.value(join(steps(ids), true)));
        msg.append(HontunChat.light("\nFingerprint: ")).append(v.guess() == null ? HontunChat.light("none") : HontunChat.value(v.guess()))
            .append(HontunChat.punct(" (")).append(HontunChat.light(v.check())).append(HontunChat.punct(")"));
        msg.append(HontunChat.light("\nSetbacks: ")).append(v.setbacks() == null ? HontunChat.light("none") : HontunChat.value(v.setbacks()));
        if (v.teleports() > 0) {
            msg.append(HontunChat.punct(" (" + v.teleports() + (v.teleports() == 1 ? " normal teleport" : " normal teleports") + " ignored)"));
        }
        if (v.conflict() != null) {
            msg.append(HontunChat.light("\nConflict: ")).append(HontunChat.punct(v.conflict()));
        }
        return msg;
    }

    private static void headline(MutableComponent msg, Verdict v) {
        if (v.name() == null) {
            msg.append(HontunChat.value("none")).append(HontunChat.punct(" (")).append(HontunChat.light(v.reason())).append(HontunChat.punct(")"));
        } else if (v.name().equals("Unknown")) {
            msg.append(HontunChat.value("unknown")).append(HontunChat.punct(" (")).append(HontunChat.light(unknownReason(v))).append(HontunChat.punct(")"));
        } else {
            msg.append(HontunChat.value(v.name())).append(HontunChat.punct(" ("))
                .append(HontunChat.light(v.confidence() + ", " + v.source())).append(HontunChat.punct(")"));
        }
    }

    private static String unknownReason(Verdict v) {
        return v.reason() != null ? v.reason() : "the ping pattern matches no known anticheat";
    }

    private static MutableComponent list(List<String> values) {
        return values.isEmpty() ? HontunChat.light("none") : HontunChat.value(String.join(", ", values));
    }

    private static List<Long> steps(List<Integer> ids) {
        List<Long> out = new ArrayList<>();
        for (int i = 1; i < ids.size(); i++) out.add((long) ids.get(i) - ids.get(i - 1));
        return out;
    }

    private static String join(List<? extends Number> values, boolean signed) {
        StringBuilder sb = new StringBuilder();
        for (Number n : values) {
            long x = n.longValue();
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(signed && x > 0 ? "+" + x : String.valueOf(x));
        }
        return sb.toString();
    }
}
