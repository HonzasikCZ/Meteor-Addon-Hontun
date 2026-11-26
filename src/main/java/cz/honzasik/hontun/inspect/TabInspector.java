package cz.honzasik.hontun.inspect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.capture.TabCapture;
import cz.honzasik.hontun.mixin.client.copy.PlayerTabOverlayAccessor;
import cz.honzasik.hontun.utils.Oversize;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class TabInspector {
    private static final int SHOWN = 10;
    private static final int MAX_HIDDEN_LOGGED = 200;
    private static final String OVERSIZE = "hidden tab player";
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Set<String> TAB_OBJECTIVES = Set.of("TAB-PlayerList", "TAB-BelowName", "TAB-Scoreboard", "TAB-YellowNumber");

    private static final Comparator<PlayerInfo> ORDER = Comparator.<PlayerInfo>comparingInt(p -> -p.getTabListOrder())
        .thenComparingInt(p -> p.getGameMode() == GameType.SPECTATOR ? 1 : 0)
        .thenComparing(p -> Optionull.mapOrDefault(p.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(p -> p.getProfile().name(), String::compareToIgnoreCase);

    private TabInspector() {}

    private enum Bucket { REAL, UNVERIFIED, FAKE }

    private enum Kind {
        PREMIUM("premium", Bucket.REAL),
        V4_UNVERIFIED("v4 UUID, unverified", Bucket.UNVERIFIED),
        OFFLINE("offline", Bucket.REAL),
        OFFLINE_OTHER("v3 UUID, not the offline one", Bucket.UNVERIFIED),
        BEDROCK("Bedrock (Floodgate)", Bucket.REAL),
        NPC("NPC (UUID v2)", Bucket.FAKE),
        TAB_LAYOUT("TAB layout slot", Bucket.FAKE),
        FAKE("fake entry", Bucket.FAKE),
        OTHER("unusual UUID", Bucket.UNVERIFIED);

        final String label;
        final Bucket bucket;
        final boolean fake;

        Kind(String label, Bucket bucket) {
            this.label = label;
            this.bucket = bucket;
            this.fake = bucket == Bucket.FAKE;
        }
    }

    public static MutableComponent inspect() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener conn = mc.getConnection();
        if (conn == null || mc.level == null) return null;
        Scoreboard scoreboard = mc.level.getScoreboard();
        PlayerTabOverlayAccessor overlay = (PlayerTabOverlayAccessor) mc.gui.hud.getTabList();
        Component header = overlay.hontun$header();
        Component footer = overlay.hontun$footer();

        List<PlayerInfo> listed = conn.getListedOnlinePlayers().stream().sorted(ORDER).toList();
        Set<UUID> listedIds = new HashSet<>();
        for (PlayerInfo p : listed) listedIds.add(p.getProfile().id());
        List<PlayerInfo> hidden = new ArrayList<>();
        for (PlayerInfo p : conn.getOnlinePlayers()) if (!listedIds.contains(p.getProfile().id())) hidden.add(p);
        List<PlayerInfo> shownInTab = listed.subList(0, Math.min(TabCapture.MAX_ENTRIES, listed.size()));

        int real = 0;
        int unverified = 0;
        int fake = 0;
        int spectators = 0;
        int layoutSlots = 0;
        long pingSum = 0;
        int pingCount = 0;
        for (PlayerInfo p : shownInTab) {
            Kind kind = kind(p, skin(p.getProfile()));
            switch (kind.bucket) {
                case REAL -> real++;
                case UNVERIFIED -> unverified++;
                case FAKE -> fake++;
            }
            if (kind == Kind.TAB_LAYOUT) layoutSlots++;
            if (p.getGameMode() == GameType.SPECTATOR) spectators++;
            if (kind.bucket == Bucket.REAL && p.getLatency() > 0) {
                pingSum += p.getLatency();
                pingCount++;
            }
        }
        int hiddenFake = 0;
        for (PlayerInfo p : hidden) if (kind(p, skin(p.getProfile())).fake) hiddenFake++;

        Objective list = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        Objective below = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
        StringBuilder log = new StringBuilder("[Inspect] Tab list\n");
        Out.Block block = new Out.Block("Tab list");

        block.add(Out.kv("Header", lines(header, "Header")));
        block.add(Out.kv("Footer", lines(footer, "Footer")));
        log.append("header mm=").append(Raw.mm(header)).append("\n  json=").append(Raw.json(header)).append('\n');
        log.append("footer mm=").append(Raw.mm(footer)).append("\n  json=").append(Raw.json(footer)).append('\n');

        MutableComponent players = Out.kv("Players", Out.value(shownInTab.size() + " in tab"))
            .append(Out.sep()).append(Out.muted(real + " real"));
        if (unverified > 0) players.append(Out.sep()).append(Out.muted(unverified + " unverified"));
        players.append(Out.sep()).append(Out.muted(fake + " fake"));
        if (!hidden.isEmpty()) players.append(Out.sep()).append(Out.muted(hidden.size() + " hidden" + (hiddenFake > 0 ? " (" + hiddenFake + " fake)" : "")));
        if (spectators > 0) players.append(Out.sep()).append(Out.muted(spectators + " spectators"));
        if (pingCount > 0) players.append(Out.sep()).append(Out.muted("avg ping " + (pingSum / pingCount) + " ms"));
        if (listed.size() > shownInTab.size()) players.append(Out.sep()).append(Out.muted((listed.size() - shownInTab.size()) + " over the 80 limit"));
        block.add(players);

        String plugin = plugin(layoutSlots, scoreboard);
        if (layoutSlots > 0) {
            int rows = shownInTab.size();
            int cols = 1;
            while (rows > 20) rows = (shownInTab.size() + ++cols - 1) / cols;
            block.add(Out.kv("Layout", Out.value("TAB layout")).append(Out.sep()).append(Out.muted(layoutSlots + " slots, " + cols + " columns")));
        }
        if (plugin != null) block.add(Out.kv("Plugins seen", Out.value(plugin)));
        if (list != null) {
            block.add(Out.kv("Yellow number", Out.value(list.getName())).append(Out.sep()).append(Out.muted(list.getRenderType().getSerializedName())));
        }
        if (below != null) {
            block.add(Out.kv("Below name", Out.value(below.getName())).append(Out.sep())
                .append(Raw.copyable(Raw.inert(below.getDisplayName()), below.getDisplayName(), "Below name title")));
        }

        Out.Entry entries = block.add(Out.kv("Entries", Out.value(shownInTab.isEmpty() ? "none" : "in tab order")));
        int i = 1;
        int listedInChat = 0;
        int more = 0;
        int empty = 0;
        for (PlayerInfo p : shownInTab) {
            Detail d = detail(p, list, scoreboard);
            log.append(i).append(". ").append(d.log).append('\n');
            boolean blank = Raw.looksEmpty(StyledText.of(d.shown));
            if (blank && d.kind.fake) {
                empty++;
            } else if (listedInChat < SHOWN) {
                MutableComponent line = Out.muted(i + ". ").append(Raw.inert(d.shown)).append(Out.sep()).append(Out.muted(d.kind.label));
                if (!d.kind.fake) line.append(Out.sep()).append(Out.muted(p.getLatency() + " ms"));
                String copy = Raw.mm(d.shown);
                entries.child(line.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(d.hover)).withClickEvent(new ClickEvent.CopyToClipboard(copy))));
                listedInChat++;
            } else {
                more++;
            }
            i++;
        }
        if (more > 0) entries.child(Out.muted("and " + more + " more (in the client log)"));
        if (empty > 0) entries.child(Out.muted(empty + " empty slots"));

        if (!hidden.isEmpty()) {
            Out.Entry h = block.add(Out.kv("Hidden from tab", Out.value(String.valueOf(hidden.size()))));
            int k = 0;
            for (PlayerInfo p : hidden) {
                if (k >= MAX_HIDDEN_LOGGED) {
                    for (PlayerInfo rest : hidden.subList(k, hidden.size())) Oversize.item(OVERSIZE, rest.getProfile().name() + " " + rest.getProfile().id());
                    log.append("hidden: ").append(hidden.size() - k).append(" more saved to ").append(Oversize.where(OVERSIZE)).append('\n');
                    break;
                }
                Detail d = detail(p, list, scoreboard);
                log.append("hidden: ").append(d.log).append('\n');
                if (k++ >= 5) continue;
                h.child(Out.value(Raw.escape(p.getProfile().name())).append(Out.sep()).append(Out.muted(d.kind.label))
                    .withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(d.hover))));
            }
            if (hidden.size() > 5) h.child(Out.muted("and " + (hidden.size() - 5) + " more ("
                + (hidden.size() > MAX_HIDDEN_LOGGED ? "first " + MAX_HIDDEN_LOGGED + " in the client log, the rest in " + Oversize.where(OVERSIZE) : "in the client log") + ")"));
        }

        block.note("Hover an entry for details, click to copy it. Full dump in the client log.");
        Hontun.LOG.info(log.toString());
        return block.render();
    }

    private static Kind kind(PlayerInfo p, Skin skin) {
        GameProfile profile = p.getProfile();
        UUID id = profile.id();
        String name = profile.name() == null ? "" : profile.name();
        if (id == null) return Kind.FAKE;
        if (id.getMostSignificantBits() == 0L) {
            long low = id.getLeastSignificantBits();
            if (low >= 1 && low <= TabCapture.MAX_ENTRIES) return Kind.TAB_LAYOUT;
            if (low > 0xFFFFFFFFL || name.startsWith(".") || name.startsWith("*")) return Kind.BEDROCK;
            return Kind.FAKE;
        }
        if (id.version() == 3 && plausibleLogin(name)
            && UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).equals(id)) return Kind.OFFLINE;
        if (!NAME.matcher(name).matches()) return Kind.FAKE;
        return switch (id.version()) {
            case 4 -> premiumProof(p, skin) ? Kind.PREMIUM : Kind.V4_UNVERIFIED;
            case 3 -> Kind.OFFLINE_OTHER;
            case 2 -> Kind.NPC;
            default -> Kind.OTHER;
        };
    }

    private static boolean plausibleLogin(String name) {
        if (name.isEmpty() || name.length() > 16) return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c <= 0x20 || c == 0x7F || c == '§') return false;
        }
        return true;
    }

    private static boolean premiumProof(PlayerInfo p, Skin skin) {
        if (p.getChatSession() != null) return true;
        UUID id = p.getProfile().id();
        String name = p.getProfile().name();
        return skin.signed && skin.profileId != null && id != null
            && skin.profileId.equalsIgnoreCase(id.toString().replace("-", ""))
            && skin.profileName != null && skin.profileName.equalsIgnoreCase(name);
    }

    private record Detail(Component shown, Kind kind, MutableComponent hover, String log) {}

    private static Detail detail(PlayerInfo p, Objective list, Scoreboard scoreboard) {
        GameProfile profile = p.getProfile();
        String name = profile.name() == null ? "" : profile.name();
        PlayerTeam team = p.getTeam();
        Component display = p.getTabListDisplayName();
        Component shown = display != null ? display : PlayerTeam.formatNameForTeam(team, Component.literal(name));
        Skin skin = skin(profile);
        Kind kind = kind(p, skin);
        RemoteChatSession chat = p.getChatSession();

        MutableComponent hover = Out.text(Raw.escape(name)).append(Component.literal("\n"));
        hover.append(Out.kv("uuid", Out.value(String.valueOf(profile.id())))).append(Out.sep())
            .append(Out.muted(kind.label + (profile.id() == null ? "" : " · v" + profile.id().version()))).append(Component.literal("\n"));
        hover.append(Out.kv("name made by", Out.value(display != null ? "display name" : team != null ? "team" : "plain name"))).append(Component.literal("\n"));
        if (team != null) {
            hover.append(Out.kv("team", Out.value(Raw.escape(team.getName())))).append(Component.literal("\n"));
            hover.append(Out.kv("prefix", Raw.inert(team.getPlayerPrefix()))).append(Component.literal("\n"));
            hover.append(Out.kv("suffix", Raw.inert(team.getPlayerSuffix()))).append(Component.literal("\n"));
            team.getColor().ifPresent(c -> hover.append(Out.kv("team color", Out.value(c.getSerializedName()))).append(Component.literal("\n")));
            hover.append(Out.kv("name tags", Out.value(team.getNameTagVisibility().name().toLowerCase(Locale.ROOT))))
                .append(Out.sep()).append(Out.muted("collision " + team.getCollisionRule().name().toLowerCase(Locale.ROOT))).append(Component.literal("\n"));
        }
        hover.append(Out.kv("order", Out.value(String.valueOf(p.getTabListOrder())))).append(Out.sep())
            .append(Out.muted(p.getGameMode().getName())).append(Out.sep()).append(Out.muted(p.getLatency() + " ms")).append(Component.literal("\n"));
        hover.append(Out.kv("chat key", Out.value(chat == null ? "none" : chatKey(chat)))).append(Component.literal("\n"));
        hover.append(Out.kv("skin", Out.value(skin.describe()))).append(Out.sep()).append(Out.muted(p.showHat() ? "hat layer on" : "hat layer off"));
        String score = null;
        if (list != null && !name.isEmpty()) {
            ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly(name), list);
            if (info != null) {
                score = String.valueOf(info.value());
                hover.append(Component.literal("\n")).append(Out.kv("yellow number", Out.value(score)));
            }
        }

        String log = "name=" + Raw.escape(name) + " uuid=" + profile.id() + " kind=" + kind.label
            + " order=" + p.getTabListOrder() + " mode=" + p.getGameMode().getName() + " ping=" + p.getLatency()
            + " team=" + (team == null ? "-" : Raw.escape(team.getName())) + " chatKey=" + (chat == null ? "none" : chatKey(chat))
            + " skin=" + skin.describe() + " hat=" + p.showHat() + (score == null ? "" : " score=" + score)
            + "\n   shown mm=" + Raw.mm(shown) + "\n   shown json=" + Raw.json(shown)
            + (display != null ? "\n   display json=" + Raw.json(display) : "")
            + (team != null ? "\n   prefix json=" + Raw.json(team.getPlayerPrefix()) + "\n   suffix json=" + Raw.json(team.getPlayerSuffix()) : "")
            + (skin.url != null ? "\n   skin url=" + skin.url : "")
            + (skin.profileId != null ? "\n   skin profile=" + skin.profileId + " " + skin.profileName : "");
        return new Detail(shown, kind, hover, log);
    }

    private static String chatKey(RemoteChatSession chat) {
        if (chat.profilePublicKey() == null) return "present";
        Instant expires = chat.profilePublicKey().data().expiresAt();
        if (expires.isBefore(Instant.now())) return "expired";
        long hours = Duration.between(Instant.now(), expires).toHours();
        return "valid, expires in " + hours + " h";
    }

    private record Skin(String url, boolean slim, boolean signed, boolean present, String profileId, String profileName) {
        String describe() {
            if (!present) return "none sent (default skin)";
            String hash = url == null ? "?" : url.substring(url.lastIndexOf('/') + 1);
            if (hash.length() > 12) hash = hash.substring(0, 12) + "…";
            return (slim ? "slim " : "") + hash + (signed ? "" : " (unsigned)");
        }
    }

    private static Skin skin(GameProfile profile) {
        Property textures = null;
        for (Property p : profile.properties().get("textures")) {
            textures = p;
            break;
        }
        if (textures == null) return new Skin(null, false, false, false, null, null);
        String url = null;
        boolean slim = false;
        String profileId = null;
        String profileName = null;
        try {
            JsonObject root = JsonParser.parseString(new String(Base64.getDecoder().decode(textures.value()), StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("profileId")) profileId = root.get("profileId").getAsString();
            if (root.has("profileName")) profileName = root.get("profileName").getAsString();
            JsonObject all = root.getAsJsonObject("textures");
            JsonObject skin = all == null ? null : all.getAsJsonObject("SKIN");
            if (skin != null) {
                JsonElement u = skin.get("url");
                if (u != null) url = u.getAsString();
                JsonObject meta = skin.getAsJsonObject("metadata");
                slim = meta != null && meta.has("model") && "slim".equals(meta.get("model").getAsString());
            }
        } catch (Throwable ignored) {
        }
        return new Skin(url, slim, textures.signature() != null, true, profileId, profileName);
    }

    private static String plugin(int layoutSlots, Scoreboard scoreboard) {
        List<String> found = new ArrayList<>();
        boolean tab = layoutSlots > 0;
        for (Objective o : scoreboard.getObjectives()) if (TAB_OBJECTIVES.contains(o.getName())) tab = true;
        if (tab) found.add("TAB");
        for (PlayerTeam t : scoreboard.getPlayerTeams()) {
            if (t.getName().startsWith("CMINP")) {
                found.add("CMI name plates");
                break;
            }
        }
        return found.isEmpty() ? null : String.join(", ", found);
    }

    private static MutableComponent lines(Component text, String what) {
        if (text == null || text.getString().isEmpty()) return Out.muted("none");
        List<StyledText> lines = StyledText.of(text).lines();
        MutableComponent shown = Out.text(Raw.looksEmpty(lines.getFirst()) ? "(empty first line)" : "").append(firstLine(text));
        MutableComponent out = Raw.copyable(shown, text, what);
        if (lines.size() > 1) out.append(Out.sep()).append(Out.muted(lines.size() + " lines"));
        MutableComponent hover = Out.text(what).append(Component.literal("\n")).append(Raw.inert(text));
        return out.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(hover)));
    }

    private static MutableComponent firstLine(Component text) {
        String s = text.getString();
        int nl = s.indexOf('\n');
        if (nl < 0) return Raw.inert(text);
        MutableComponent out = Component.empty();
        int[] left = {nl};
        text.visit((style, part) -> {
            if (left[0] <= 0) return Optional.of(Unit.INSTANCE);
            String piece = part.length() > left[0] ? part.substring(0, left[0]) : part;
            left[0] -= piece.length();
            out.append(Component.literal(piece).withStyle(style.withClickEvent(null).withHoverEvent(null).withInsertion(null)));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
