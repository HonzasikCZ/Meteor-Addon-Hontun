package cz.honzasik.hontun.copy.capture;

import cz.honzasik.hontun.copy.StyledText;

import java.util.List;
import java.util.Map;

public final class Motion {
    public record Anim(List<StyledText> frames, int interval) {
        public int cycle() {
            return frames.size() * interval;
        }
    }

    public record Block(Map<Integer, Anim> lines, Anim whole) {
        public boolean animated() {
            return whole != null || (lines != null && !lines.isEmpty());
        }
    }

    public record Tab(Block header, Block footer, List<String> notes) {}

    public record Sidebar(Anim title, Map<Integer, Anim> lines, Map<Integer, Anim> rights, List<String> notes) {}

    private Motion() {}
}
