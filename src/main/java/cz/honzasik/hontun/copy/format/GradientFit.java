package cz.honzasik.hontun.copy.format;

import cz.honzasik.hontun.copy.StyledText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GradientFit {
    public static final int MIN_LENGTH = 4;
    public static final int TOLERANCE = 2;

    public interface Curve {
        int[] colors(int from, int to, int length);
    }

    public record Piece(List<StyledText.Run> runs, int from, int to, String text, StyledText.Look look) {
        public boolean gradient() {
            return text != null;
        }

        static Piece plain(List<StyledText.Run> runs) {
            return new Piece(runs, 0, 0, null, null);
        }
    }

    private record Cell(int codepoint, StyledText.Look look) {
        boolean space() {
            return Character.isWhitespace(codepoint);
        }
    }

    private GradientFit() {}

    public static List<Piece> split(StyledText text, Curve curve) {
        if (curve == null) return List.of(Piece.plain(text.runs()));
        List<Cell> cells = new ArrayList<>();
        for (StyledText.Run run : text.runs()) {
            run.text().codePoints().forEach(cp -> cells.add(new Cell(cp, run.look())));
        }
        List<Piece> out = new ArrayList<>();
        List<StyledText.Run> pending = new ArrayList<>();
        int i = 0;
        while (i < cells.size()) {
            int end = longest(cells, i, curve);
            if (end < 0) {
                appendCell(pending, cells.get(i));
                i++;
                continue;
            }
            if (!pending.isEmpty()) {
                out.add(Piece.plain(List.copyOf(pending)));
                pending.clear();
            }
            StringBuilder sb = new StringBuilder();
            List<StyledText.Run> runs = new ArrayList<>();
            for (int k = i; k <= end; k++) {
                sb.appendCodePoint(cells.get(k).codepoint);
                appendCell(runs, cells.get(k));
            }
            StyledText.Look base = cells.get(i).look;
            out.add(new Piece(List.copyOf(runs), cells.get(i).look.color(), cells.get(end).look.color(), sb.toString(), base));
            i = end + 1;
        }
        if (!pending.isEmpty()) out.add(Piece.plain(List.copyOf(pending)));
        return out;
    }

    private static void appendCell(List<StyledText.Run> runs, Cell cell) {
        String s = Character.toString(cell.codepoint);
        if (!runs.isEmpty() && runs.getLast().look().equals(cell.look)) {
            StyledText.Run last = runs.removeLast();
            runs.add(new StyledText.Run(last.text() + s, cell.look));
        } else {
            runs.add(new StyledText.Run(s, cell.look));
        }
    }

    private static boolean compatible(StyledText.Look a, StyledText.Look b) {
        return a.sameDecorations(b) && Objects.equals(a.font(), b.font()) && a.hasShadow() == b.hasShadow() && a.shadow() == b.shadow();
    }

    private static int longest(List<Cell> cells, int start, Curve curve) {
        Cell first = cells.get(start);
        if (first.space() || !first.look.hasColor() || first.codepoint > 0xFFFF) return -1;
        int limit = start;
        while (limit + 1 < cells.size()) {
            Cell next = cells.get(limit + 1);
            if (next.codepoint > 0xFFFF || !compatible(first.look, next.look)) break;
            if (!next.look.hasColor() && !(next.space() && !next.look.visibleOnSpace())) break;
            limit++;
        }
        while (limit > start && cells.get(limit).space()) limit--;
        for (int end = limit; end >= start + MIN_LENGTH - 1; end--) {
            if (cells.get(end).space()) continue;
            if (!fits(cells, start, end, curve)) continue;
            return varied(cells, start, end) ? end : -1;
        }
        return -1;
    }

    private static boolean varied(List<Cell> cells, int start, int end) {
        int c0 = cells.get(start).look.color();
        int visible = 0;
        boolean varied = false;
        for (int k = start; k <= end; k++) {
            Cell c = cells.get(k);
            if (c.space()) continue;
            visible++;
            if (c.look.color() != c0) varied = true;
        }
        return varied && visible >= MIN_LENGTH;
    }

    private static boolean fits(List<Cell> cells, int start, int end, Curve curve) {
        int n = end - start + 1;
        int[] want = curve.colors(cells.get(start).look.color(), cells.get(end).look.color(), n);
        if (want == null) return false;
        for (int k = 0; k < n; k++) {
            Cell c = cells.get(start + k);
            if (c.space() && !c.look.visibleOnSpace()) continue;
            if (!close(want[k], c.look.color())) return false;
        }
        return true;
    }

    private static boolean close(int a, int b) {
        return Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF)) <= TOLERANCE
            && Math.abs(((a >> 8) & 0xFF) - ((b >> 8) & 0xFF)) <= TOLERANCE
            && Math.abs((a & 0xFF) - (b & 0xFF)) <= TOLERANCE;
    }
}
