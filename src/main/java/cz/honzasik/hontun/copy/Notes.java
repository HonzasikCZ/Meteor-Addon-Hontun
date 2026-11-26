package cz.honzasik.hontun.copy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Notes {
    private final Map<String, Integer> counts = new LinkedHashMap<>();

    public void add(String note) {
        counts.merge(note, 1, Integer::sum);
    }

    public void addAll(Notes other) {
        other.counts.forEach((k, v) -> counts.merge(k, v, Integer::sum));
    }

    public void replaceWith(Notes other) {
        counts.clear();
        addAll(other);
    }

    public boolean isEmpty() {
        return counts.isEmpty();
    }

    public List<String> lines() {
        List<String> out = new ArrayList<>();
        counts.forEach((k, v) -> out.add(v > 1 ? k + " (" + v + "x)" : k));
        return out;
    }
}
