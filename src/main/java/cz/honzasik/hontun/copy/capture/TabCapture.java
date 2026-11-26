package cz.honzasik.hontun.copy.capture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.mixin.client.copy.PlayerTabOverlayAccessor;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class TabCapture {
    public static final int MAX_ENTRIES = 80;

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private static final Comparator<PlayerInfo> ORDER = Comparator.<PlayerInfo>comparingInt(p -> -p.getTabListOrder())
        .thenComparingInt(p -> p.getGameMode() == GameType.SPECTATOR ? 1 : 0)
        .thenComparing(p -> Optionull.mapOrDefault(p.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(p -> p.getProfile().name(), String::compareToIgnoreCase);

    public record Entry(String name, String uuid, StyledText shown, boolean displayName, StyledText tag, String team,
                        int latency, String skinValue, String skinSignature, boolean fake) {}

    public record Snapshot(StyledText header, StyledText footer, List<Entry> entries, List<String> online) {
        public boolean layout() {
            if (entries.size() < 20) return false;
            int fake = 0;
            for (Entry e : entries) if (e.fake) fake++;
            return fake * 10 >= entries.size() * 6;
        }

        public boolean isEmpty() {
            return header.isBlank() && footer.isBlank() && entries.isEmpty();
        }

        public String player(Entry e) {
            if (!e.fake) return e.name;
            String plain = e.shown.plain();
            String best = null;
            for (String name : online) {
                int at = plain.indexOf(name);
                if (at < 0) continue;
                boolean before = at == 0 || !Character.isLetterOrDigit(plain.charAt(at - 1)) && plain.charAt(at - 1) != '_';
                int end = at + name.length();
                boolean after = end >= plain.length() || !Character.isLetterOrDigit(plain.charAt(end)) && plain.charAt(end) != '_';
                if (before && after && (best == null || name.length() > best.length())) best = name;
            }
            return best;
        }
    }

    private TabCapture() {}

    public static Snapshot take() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener conn = mc.getConnection();
        if (conn == null || mc.level == null) return null;
        PlayerTabOverlayAccessor tab = (PlayerTabOverlayAccessor) mc.gui.hud.getTabList();
        StyledText header = StyledText.of(tab.hontun$header());
        StyledText footer = StyledText.of(tab.hontun$footer());

        List<Entry> entries = new ArrayList<>();
        List<PlayerInfo> infos = conn.getListedOnlinePlayers().stream().sorted(ORDER).limit(MAX_ENTRIES).toList();
        for (PlayerInfo info : infos) {
            GameProfile profile = info.getProfile();
            String name = profile.name() == null ? "" : profile.name();
            PlayerTeam team = info.getTeam();
            Component display = info.getTabListDisplayName();
            Component shown = display != null ? display : PlayerTeam.formatNameForTeam(team, Component.literal(name));
            StyledText tag = team == null ? null : StyledText.of(PlayerTeam.formatNameForTeam(team, Component.literal(name)));
            String value = null;
            String signature = null;
            for (Property p : profile.properties().get("textures")) {
                value = p.value();
                signature = p.signature();
                break;
            }
            UUID id = profile.id();
            entries.add(new Entry(name, id == null ? "" : id.toString(), StyledText.of(shown), display != null, tag,
                team == null ? null : team.getName(), info.getLatency(), value, signature, fake(id, name)));
        }

        List<String> online = new ArrayList<>();
        for (PlayerInfo info : conn.getOnlinePlayers()) {
            GameProfile profile = info.getProfile();
            if (profile.name() != null && !fake(profile.id(), profile.name())) online.add(profile.name());
        }
        return new Snapshot(header, footer, List.copyOf(entries), List.copyOf(online));
    }

    public static boolean fake(UUID id, String name) {
        if (id != null && id.getMostSignificantBits() == 0L) {
            long low = id.getLeastSignificantBits();
            if (low >= 1 && low <= MAX_ENTRIES) return true;
            if (low > 0xFFFFFFFFL || (name != null && (name.startsWith(".") || name.startsWith("*")))) return false;
        }
        return name == null || !NAME.matcher(name).matches();
    }
}
