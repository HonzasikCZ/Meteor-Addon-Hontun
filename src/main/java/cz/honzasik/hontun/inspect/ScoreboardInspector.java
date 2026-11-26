package cz.honzasik.hontun.inspect;

import com.mojang.serialization.JsonOps;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.format.MiniMessageWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ScoreboardInspector {
    private static final int MAX_LINES = 15;
    private static final Pattern FASTBOARD = Pattern.compile("fb-[0-9a-f]{1,8}");
    private static final Pattern RANDOM16 = Pattern.compile("[A-Za-z0-9]{16}");
    private static final Comparator<PlayerScoreEntry> ORDER = Comparator.<PlayerScoreEntry>comparingInt(PlayerScoreEntry::value).reversed()
        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);

    private ScoreboardInspector() {}

    public static MutableComponent inspect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;
        Scoreboard scoreboard = mc.level.getScoreboard();
        StringBuilder log = new StringBuilder("[Inspect] Scoreboard\n");

        DisplaySlot slot = DisplaySlot.SIDEBAR;
        Objective objective = null;
        PlayerTeam own = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
        if (own != null) {
            Optional<TeamColor> color = own.getColor();
            if (color.isPresent()) {
                objective = scoreboard.getDisplayObjective(color.get().displaySlot());
                if (objective != null) slot = color.get().displaySlot();
            }
        }
        if (objective == null) objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);

        Out.Block block = new Out.Block("Scoreboard");
        if (objective == null) {
            block.add(Out.muted("No sidebar is shown right now."));
            log.append("no sidebar objective\n");
        } else {
            sidebar(block, log, scoreboard, objective, slot);
        }
        others(block, log, scoreboard, objective);
        block.note("Hover a line for details, click to copy it. Full dump in the client log.");
        Hontun.LOG.info(log.toString());
        return block.render();
    }

    private static void sidebar(Out.Block block, StringBuilder log, Scoreboard scoreboard, Objective objective, DisplaySlot slot) {
        Collection<PlayerScoreEntry> all = scoreboard.listPlayerScores(objective);
        List<PlayerScoreEntry> visible = all.stream().filter(e -> !e.isHidden()).sorted(ORDER).toList();
        List<PlayerScoreEntry> shown = visible.subList(0, Math.min(MAX_LINES, visible.size()));
        NumberFormat objectiveFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        String plugin = plugin(objective, scoreboard);
        block.add(Out.kv("Objective", Out.value(objective.getName()))
            .append(Out.sep()).append(Out.muted(slot.getSerializedName()))
            .append(Out.sep()).append(Out.muted(objective.getRenderType().getSerializedName()))
            .append(Out.sep()).append(Out.muted("numbers " + format(objective.numberFormat()) + (objective.numberFormat() == null ? " (default)" : ""))));
        if (plugin != null) block.add(Out.kv("Made by", Out.value(plugin)));
        log.append("objective=").append(objective.getName()).append(" slot=").append(slot.getSerializedName())
            .append(" render=").append(objective.getRenderType().getSerializedName())
            .append(" autoUpdate=").append(objective.displayAutoUpdate())
            .append(" numbers=").append(format(objective.numberFormat()))
            .append(" plugin=").append(plugin).append('\n');

        Component title = objective.getDisplayName();
        block.add(Out.kv("Title", clickable(Raw.inert(title), title, "Title")));
        log.append("title mm=").append(mm(title)).append("\n  json=").append(json(title)).append('\n');

        int hiddenHolders = all.size() - visible.size();
        MutableComponent linesHead = Out.kv("Lines", Out.value(shown.size() + " shown"));
        if (visible.size() > shown.size()) linesHead.append(Out.sep()).append(Out.muted((visible.size() - shown.size()) + " more scores not shown (limit 15)"));
        if (hiddenHolders > 0) linesHead.append(Out.sep()).append(Out.muted(hiddenHolders + " hidden (#) scores"));
        Out.Entry linesEntry = block.add(linesHead);

        Map<String, Integer> methods = new LinkedHashMap<>();
        int i = 1;
        for (PlayerScoreEntry e : shown) {
            PlayerTeam team = scoreboard.getPlayersTeam(e.owner());
            MutableComponent text = PlayerTeam.formatNameForTeam(team, e.ownerName());
            NumberFormat lineFormat = e.numberFormatOverride() != null ? e.numberFormatOverride() : objectiveFormat;
            MutableComponent right = e.formatValue(objectiveFormat);
            String method = method(e, team);
            methods.merge(method, 1, Integer::sum);

            MutableComponent hover = Out.text("Line " + i).append(Component.literal("\n"));
            hover.append(Out.kv("holder", Out.value(escape(e.owner())))).append(Component.literal("\n"));
            hover.append(Out.kv("score", Out.value(String.valueOf(e.value())))).append(Component.literal("\n"));
            hover.append(Out.kv("made with", Out.value(method))).append(Component.literal("\n"));
            if (e.display() != null) hover.append(Out.kv("display name", Raw.inert(e.display()))).append(Component.literal("\n"));
            if (team != null) {
                hover.append(Out.kv("team", Out.value(team.getName()))).append(Component.literal("\n"));
                hover.append(Out.kv("prefix", Raw.inert(team.getPlayerPrefix()))).append(Component.literal("\n"));
                hover.append(Out.kv("suffix", Raw.inert(team.getPlayerSuffix()))).append(Component.literal("\n"));
                team.getColor().ifPresent(c -> hover.append(Out.kv("team color", Out.value(c.getSerializedName()))).append(Component.literal("\n")));
            }
            hover.append(Out.kv("number", Out.value(format(lineFormat) + (e.numberFormatOverride() != null ? " (this line)" : ""))));

            MutableComponent line = Out.muted(i + ". ").append(Raw.inert(text));
            if (!(lineFormat instanceof BlankFormat) && !right.getString().isEmpty()) line.append(Out.punct("  ")).append(Raw.inert(right));
            String copy = mm(text);
            linesEntry.child(line.withStyle(s -> s
                .withHoverEvent(new HoverEvent.ShowText(hover))
                .withClickEvent(new ClickEvent.CopyToClipboard(copy))));

            log.append(String.format("line %d: holder=%s score=%d method=%s team=%s numbers=%s%n", i, escape(e.owner()), e.value(), method,
                team == null ? "-" : team.getName(), format(lineFormat)));
            log.append("  text mm=").append(mm(text)).append("\n  text json=").append(json(text)).append('\n');
            if (!(lineFormat instanceof BlankFormat)) log.append("  right mm=").append(mm(right)).append('\n');
            if (team != null) log.append("  prefix json=").append(json(team.getPlayerPrefix())).append("\n  suffix json=").append(json(team.getPlayerSuffix())).append('\n');
            if (e.display() != null) log.append("  display json=").append(json(e.display())).append('\n');
            i++;
        }
        if (!methods.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            methods.forEach((k, v) -> {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(k).append(" (").append(v).append(')');
            });
            block.add(Out.kv("Lines are made with", Out.value(sb.toString())));
        }
    }

    private static void others(Out.Block block, StringBuilder log, Scoreboard scoreboard, Objective sidebar) {
        Map<Objective, String> slots = new LinkedHashMap<>();
        for (DisplaySlot slot : DisplaySlot.values()) {
            Objective o = scoreboard.getDisplayObjective(slot);
            if (o != null) slots.merge(o, slot.getSerializedName(), (a, b) -> a + ", " + b);
        }
        List<Objective> rest = new ArrayList<>();
        for (Objective o : scoreboard.getObjectives()) if (o != sidebar) rest.add(o);
        if (rest.isEmpty()) return;
        Out.Entry entry = block.add(Out.kv("Other objectives", Out.value(String.valueOf(rest.size()))));
        int shown = 0;
        for (Objective o : rest) {
            String where = slots.getOrDefault(o, "hidden");
            int scores = scoreboard.listPlayerScores(o).size();
            log.append("objective ").append(o.getName()).append(" slot=").append(where).append(" scores=").append(scores)
                .append(" render=").append(o.getRenderType().getSerializedName()).append(" title mm=").append(mm(o.getDisplayName())).append('\n');
            if (shown++ >= 10) continue;
            MutableComponent hover = Out.kv("title", Raw.inert(o.getDisplayName())).append(Component.literal("\n"))
                .append(Out.kv("render", Out.value(o.getRenderType().getSerializedName()))).append(Component.literal("\n"))
                .append(Out.kv("numbers", Out.value(format(o.numberFormat()))));
            entry.child(Out.value(o.getName()).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(hover)))
                .append(Out.sep()).append(Out.muted(where)).append(Out.sep()).append(Out.muted(scores + " scores")));
        }
        if (rest.size() > 10) entry.child(Out.muted("and " + (rest.size() - 10) + " more (see the log)"));
    }

    private static String plugin(Objective objective, Scoreboard scoreboard) {
        String name = objective.getName();
        if (name.equals("TAB-Scoreboard")) return "TAB";
        if (FASTBOARD.matcher(name).matches()) return "the FastBoard library (used by many plugins)";
        for (PlayerTeam t : scoreboard.getPlayerTeams()) {
            if (t.getName().startsWith("TAB-Sidebar-")) return "TAB";
        }
        if (RANDOM16.matcher(name).matches() && !name.chars().allMatch(Character::isLetter)) return "probably the scoreboard-library library (random 16 character name)";
        return null;
    }

    private static String method(PlayerScoreEntry e, PlayerTeam team) {
        if (e.display() != null) return "score display name";
        if (team != null && (!team.getPlayerPrefix().getString().isEmpty() || !team.getPlayerSuffix().getString().isEmpty())) return "team prefix/suffix";
        if (Raw.looksEmpty(StyledText.of(PlayerTeam.formatNameForTeam(team, e.ownerName())))) return "empty line";
        return "holder name";
    }

    private static String format(NumberFormat f) {
        if (f == null) return "red numbers";
        if (f instanceof BlankFormat) return "hidden";
        if (f instanceof FixedFormat fixed) {
            String text = fixed.value().getString();
            return text.isEmpty() ? "hidden (empty fixed text)" : "fixed text \"" + escape(text) + "\"";
        }
        if (f instanceof StyledFormat styled) return "styled numbers" + (styled.style().getColor() == null ? "" : " " + styled.style().getColor().serialize());
        return f.getClass().getSimpleName();
    }

    private static String escape(String s) {
        return Raw.escape(s);
    }

    private static String mm(Component c) {
        return Raw.mm(c);
    }

    private static String json(Component c) {
        return Raw.json(c);
    }

    private static MutableComponent clickable(MutableComponent shown, Component source, String what) {
        return Raw.copyable(shown, source, what);
    }
}
