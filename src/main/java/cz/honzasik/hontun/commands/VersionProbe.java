package cz.honzasik.hontun.commands;

import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
import cz.honzasik.hontun.utils.RealVersion;
import cz.honzasik.hontun.utils.RealVersion.Evidence;
import cz.honzasik.hontun.utils.RealVersion.Fate;
import cz.honzasik.hontun.utils.RealVersion.Report;
import cz.honzasik.hontun.utils.RealVersion.Snapshot;
import cz.honzasik.hontun.utils.RealVersion.Stage;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.VersionKeeper;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VersionProbe {
    private static final Map<String, String> TITLES = Map.of(
            RealVersion.CORE, "Core pack",
            RealVersion.VIA, "ViaVersion",
            RealVersion.VFP, "ViaFabricPlus",
            RealVersion.PING, "Ping",
            RealVersion.TAGS, "Tags",
            RealVersion.REGISTRIES, "Registries",
            RealVersion.OVERWORLD, "Overworld",
            RealVersion.JOIN, "Join packets");

    private VersionProbe() {}

    public static void run() {
        if (MCUtil.getPlayNetHandler() == null) {
            ChatUtils.error("Not connected to a server. Join one first, the version is worked out from what the server sends while you join.");
            return;
        }
        Snapshot s = ServerSoftware.realVersionSnapshot();
        ChatUtils.sendMsg(format(RealVersion.explain(s), s, MCUtil.MC.hasSingleplayerServer(), VersionKeeper.version));
    }

    public static MutableComponent format(Report r, Snapshot s, boolean singleplayer, String corePack) {
        RealVersion.Result res = r.result();
        String missing = r.missing() != null ? r.missing() : "not enough signals yet";
        boolean known = corePack != null && !"unknown".equals(corePack);
        MutableComponent msg = HontunChat.light("Version: ");
        if (singleplayer) {
            note(msg, HontunChat.value(s.clientVersion), "singleplayer, the integrated server is this client");
        } else if (res != null && res.method().isEmpty()) {
            note(msg, HontunChat.value(res.range()), "hidden by " + (r.through() != null ? r.through() : "ViaFabricPlus"));
        } else if (res != null) {
            note(msg, HontunChat.value(res.range()), r.confidence() + " confidence");
        } else if (known) {
            note(msg, HontunChat.value(corePack), "core pack");
        } else {
            note(msg, HontunChat.value("unknown"), missing);
        }
        if (singleplayer || (res == null && known)) {
            msg.append(HontunChat.light("\nResolver: "));
            if (res != null) note(msg, HontunChat.value(res.range()), r.confidence() + " confidence");
            else note(msg, HontunChat.light("no answer"), missing);
        }
        msg.append(HontunChat.light("\nMethod: "))
                .append(res == null || res.method().isEmpty() ? HontunChat.light("none") : HontunChat.value(res.method()));
        String client = s.clientVersion + ", protocol " + s.clientProtocol;
        msg.append(HontunChat.light("\nTranslated: "));
        if (res == null) {
            msg.append(HontunChat.light("unknown")).append(HontunChat.punct(" (client " + client + ")"));
        } else if (res.translated()) {
            msg.append(HontunChat.value("yes")).append(HontunChat.punct(", through "))
                    .append(HontunChat.value(r.through() != null ? r.through() : "ViaVersion"))
                    .append(HontunChat.punct(" (client " + client + ")"));
        } else {
            msg.append(HontunChat.value("no")).append(HontunChat.punct(", the client speaks it natively (" + client + ")"));
        }
        if (r.disagree() != null) msg.append(HontunChat.light("\nDisagreement: ")).append(HontunChat.punct(r.disagree()));
        stages(msg, r.stages());
        for (String source : RealVersion.SOURCES) section(msg, source, r.evidence());
        return msg;
    }

    private static void note(MutableComponent msg, MutableComponent head, String detail) {
        msg.append(head).append(HontunChat.punct(" (")).append(HontunChat.light(detail)).append(HontunChat.punct(")"));
    }

    private static void stages(MutableComponent msg, List<Stage> stages) {
        if (stages.isEmpty()) return;
        msg.append(HontunChat.light("\nRange: "));
        String prev = null;
        boolean first = true;
        for (Stage st : stages) {
            if (st.step().equals("named") && st.range().equals(prev)) continue;
            if (!first) msg.append(HontunChat.punct(" → "));
            msg.append(HontunChat.dim(st.step() + " ")).append(HontunChat.value(st.range()));
            prev = st.range();
            first = false;
        }
    }

    private static void section(MutableComponent msg, String source, List<Evidence> all) {
        List<Evidence> list = new ArrayList<>();
        for (Evidence e : all) if (e.source().equals(source)) list.add(e);
        if (list.isEmpty()) return;
        String title = TITLES.getOrDefault(source, source);
        if (list.size() == 1) {
            msg.append(HontunChat.light("\n" + title + ": "));
            body(msg, list.get(0));
            return;
        }
        msg.append(HontunChat.light("\n" + title + ":"));
        boolean grouped = source.equals(RealVersion.TAGS) || source.equals(RealVersion.REGISTRIES);
        boolean agreeShown = false;
        List<Evidence> folded = new ArrayList<>();
        List<Evidence> quiet = new ArrayList<>();
        for (Evidence e : list) {
            if (grouped && !e.hint() && e.fate() == Fate.AGREES) {
                if (agreeShown) {
                    folded.add(e);
                    continue;
                }
                agreeShown = true;
            }
            if (e.hint() && (e.fate() == Fate.AGREES || e.fate() == Fate.UNUSED)) {
                quiet.add(e);
                continue;
            }
            msg.append(HontunChat.dim("\n" + mark(e)));
            body(msg, e);
        }
        fold(msg, folded, folded.size() == 1 ? " more group, already implied" : " more groups, already implied");
        boolean dropped = !quiet.isEmpty() && quiet.get(0).fate() == Fate.UNUSED;
        fold(msg, quiet, (quiet.size() == 1 ? " hint " : " hints ") + (dropped ? "not needed, the version is pinned" : quiet.size() == 1 ? "agrees" : "agree"));
    }

    private static String mark(Evidence e) {
        return switch (e.fate()) {
            case DECISIVE -> e.hint() ? " ~ " : " + ";
            case IGNORED, UNUSED -> " - ";
            default -> " · ";
        };
    }

    private static void body(MutableComponent msg, Evidence e) {
        boolean decisive = e.fate() == Fate.DECISIVE;
        List<String> details = e.details();
        boolean group = details.size() > 1 && details.get(0).equals(e.signal());
        boolean listed = !details.isEmpty() && !(details.size() == 1 && details.get(0).equals(e.signal()));
        MutableComponent signal = decisive ? HontunChat.value(e.signal()) : HontunChat.light(e.signal());
        msg.append(listed ? hover(signal, String.join("\n", details)) : signal);
        if (group) msg.append(HontunChat.dim(" +" + (details.size() - 1) + " more"));
        if (e.implies() != null) {
            msg.append(HontunChat.dim(" [")).append(decisive ? HontunChat.value(e.implies()) : HontunChat.light(e.implies())).append(HontunChat.dim("]"));
        }
        if (e.reason() != null) msg.append(HontunChat.punct((e.implies() == null ? ", " : " ") + e.reason()));
    }

    private static void fold(MutableComponent msg, List<Evidence> list, String text) {
        if (list.isEmpty()) return;
        List<String> lines = new ArrayList<>();
        for (Evidence e : list) {
            StringBuilder line = new StringBuilder(e.signal());
            if (e.details().size() > 1 && e.details().get(0).equals(e.signal())) line.append(" +").append(e.details().size() - 1).append(" more");
            if (e.implies() != null) line.append(" [").append(e.implies()).append(']');
            if (e.reason() != null) line.append(e.implies() == null ? ", " : " ").append(e.reason());
            lines.add(line.toString());
        }
        msg.append(HontunChat.dim("\n · ")).append(hover(HontunChat.light(list.size() + text), String.join("\n", lines)));
    }

    private static MutableComponent hover(MutableComponent text, String tip) {
        return text.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(tip))));
    }
}
