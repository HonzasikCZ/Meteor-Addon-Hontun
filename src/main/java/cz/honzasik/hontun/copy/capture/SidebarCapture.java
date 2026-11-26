package cz.honzasik.hontun.copy.capture;

import cz.honzasik.hontun.copy.StyledText;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class SidebarCapture {
    public static final int MAX_LINES = 15;

    private static final Comparator<PlayerScoreEntry> ORDER = Comparator.<PlayerScoreEntry>comparingInt(PlayerScoreEntry::value).reversed()
        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);

    public record Line(StyledText text, StyledText right) {}

    public record Snapshot(StyledText title, List<Line> lines) {}

    private SidebarCapture() {}

    public static Snapshot take() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = null;
        PlayerTeam own = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
        if (own != null) {
            Optional<TeamColor> color = own.getColor();
            if (color.isPresent()) objective = scoreboard.getDisplayObjective(color.get().displaySlot());
        }
        if (objective == null) objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return null;

        NumberFormat def = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
        List<PlayerScoreEntry> rows = scoreboard.listPlayerScores(objective).stream()
            .filter(e -> !e.isHidden())
            .sorted(ORDER)
            .limit(MAX_LINES)
            .toList();
        List<Line> lines = new ArrayList<>();
        for (PlayerScoreEntry e : rows) {
            PlayerTeam team = scoreboard.getPlayersTeam(e.owner());
            StyledText text = StyledText.of(PlayerTeam.formatNameForTeam(team, e.ownerName()));
            NumberFormat format = e.numberFormatOverride() != null ? e.numberFormatOverride() : def;
            StyledText right = format instanceof BlankFormat ? null : StyledText.of(e.formatValue(def));
            if (right != null && right.isEmpty()) right = null;
            lines.add(new Line(text, right));
        }
        return new Snapshot(StyledText.of(objective.getDisplayName()), List.copyOf(lines));
    }
}
