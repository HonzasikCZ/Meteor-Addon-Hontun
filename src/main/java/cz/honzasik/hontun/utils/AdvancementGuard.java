package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.modules.AntiExploit;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class AdvancementGuard {
    public static final int MAX_DEPTH = 512;

    private static final int OPEN = -2;
    private static final int REJECTED = -1;

    private AdvancementGuard() {}

    public static List<AdvancementHolder> parentFirst(Iterable<AdvancementHolder> added, Function<Identifier, AdvancementNode> present) {
        List<AdvancementHolder> list = new ArrayList<>();
        for (AdvancementHolder holder : added) list.add(holder);
        int n = list.size();

        Map<Identifier, Integer> byId = HashMap.newHashMap(n);
        for (int i = 0; i < n; i++) byId.putIfAbsent(list.get(i).id(), i);

        int[] pass = new int[n];
        int[] depth = new int[n];
        int[] chain = new int[n];
        Map<Identifier, Integer> treeDepths = new HashMap<>();

        for (int start = 0; start < n; start++) {
            if (pass[start] != 0) continue;
            int size = 0;
            int current = start;
            int parentPass;
            int parentIndex = -1;
            int parentDepth = 0;
            while (true) {
                pass[current] = OPEN;
                chain[size++] = current;
                Identifier parentId = list.get(current).value().parent().orElse(null);
                if (parentId == null) {
                    parentPass = 0;
                    break;
                }
                AdvancementNode node = present.apply(parentId);
                if (node != null) {
                    parentPass = 0;
                    parentDepth = treeDepth(node, parentId, treeDepths);
                    break;
                }
                Integer parent = byId.get(parentId);
                if (parent == null || pass[parent] == OPEN || pass[parent] == REJECTED) {
                    parentPass = REJECTED;
                    break;
                }
                if (pass[parent] > 0) {
                    parentPass = pass[parent];
                    parentIndex = parent;
                    parentDepth = depth[parent];
                    break;
                }
                current = parent;
            }
            for (int k = size - 1; k >= 0; k--) {
                int entry = chain[k];
                if (parentPass == REJECTED || parentDepth >= MAX_DEPTH) {
                    pass[entry] = REJECTED;
                } else {
                    pass[entry] = parentIndex < 0 ? 1 : parentIndex < entry ? parentPass : parentPass + 1;
                    depth[entry] = parentDepth + 1;
                }
                parentPass = pass[entry];
                parentIndex = entry;
                parentDepth = depth[entry];
            }
        }

        int maxPass = 0;
        for (int p : pass) maxPass = Math.max(maxPass, p);
        int[] offset = new int[maxPass + 2];
        for (int p : pass) if (p > 0) offset[p + 1]++;
        for (int p = 1; p <= maxPass + 1; p++) offset[p] += offset[p - 1];
        int kept = offset[maxPass + 1];

        AdvancementHolder[] ordered = new AdvancementHolder[kept];
        for (int i = 0; i < n; i++) {
            if (pass[i] > 0) ordered[offset[pass[i]]++] = list.get(i);
        }

        int dropped = n - kept;
        if (dropped > 0 && AntiExploit.shouldWarn("adv|dropped")) {
            Hontun.LOG.warn("[AntiExploit] left out {} advancements without a reachable parent or deeper than {}", dropped, MAX_DEPTH);
            AntiExploit.blocked("Advancements", "orphan, cyclic or too deep advancements left out");
        }
        return Arrays.asList(ordered);
    }

    public static AdvancementWidget flattenTo(AdvancementNode node, AdvancementWidget root, AdvancementWidget child) {
        if (!AntiExploit.packetGuard() || root == null || root == child) return null;
        int depth = 0;
        for (AdvancementNode current = node; current != null; current = current.parent()) {
            if (++depth > MAX_DEPTH) {
                if (AntiExploit.shouldWarn("adv|nesting")) {
                    Hontun.LOG.warn("[AntiExploit] attached an advancement nested deeper than {} to its tab root", MAX_DEPTH);
                    AntiExploit.blocked("Advancements", "too deep advancement attached to tab root");
                }
                return root;
            }
        }
        return null;
    }

    private static int treeDepth(AdvancementNode node, Identifier id, Map<Identifier, Integer> cache) {
        Integer known = cache.get(id);
        if (known != null) return known;
        int depth = 0;
        for (AdvancementNode current = node; current != null && depth < MAX_DEPTH; current = current.parent()) depth++;
        cache.put(id, depth);
        return depth;
    }
}
