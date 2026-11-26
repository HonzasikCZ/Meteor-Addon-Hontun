package cz.honzasik.hontun.copy;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.Motion;
import cz.honzasik.hontun.copy.capture.SidebarCapture;
import cz.honzasik.hontun.copy.capture.TabCapture;
import cz.honzasik.hontun.copy.export.CmiExport;
import cz.honzasik.hontun.copy.export.DecentExport;
import cz.honzasik.hontun.copy.export.FancyExport;
import cz.honzasik.hontun.copy.export.TabExport;
import cz.honzasik.hontun.inspect.Out;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Copier {
    public record Report(List<Output> outputs, Path dir, String error, boolean wrote) {
        static Report failed(String error) {
            return new Report(List.of(), null, error, false);
        }
    }

    private Copier() {}

    public static Report tab(List<Target> targets) {
        return tab(targets, TabCapture.take(), null);
    }

    public static Report tab(List<Target> targets, TabCapture.Snapshot snap, Motion.Tab motion) {
        if (snap == null) return Report.failed("Not connected to a server.");
        if (snap.isEmpty()) return Report.failed("The tab list is empty, there is nothing to copy.");
        String key = CopyStore.saveKey();
        CopyStore.State state = CopyStore.load(key);
        state.tab = snap;
        state.tabMotion = motion;
        List<Output> outputs = new ArrayList<>();
        for (Target t : targets) {
            outputs.add(switch (t) {
                case TAB -> TabExport.export(state);
                case CMI -> CmiExport.tab(state);
                default -> new Output(t).fail("no tab list settings");
            });
        }
        return finish(key, state, outputs);
    }

    public static Report scoreboard(List<Target> targets) {
        return scoreboard(targets, SidebarCapture.take(), null);
    }

    public static Report scoreboard(List<Target> targets, SidebarCapture.Snapshot snap, Motion.Sidebar motion) {
        if (snap == null) return Report.failed("No sidebar scoreboard is shown right now.");
        String key = CopyStore.saveKey();
        CopyStore.State state = CopyStore.load(key);
        state.sidebar = snap;
        state.sidebarMotion = motion;
        List<Output> outputs = new ArrayList<>();
        for (Target t : targets) {
            outputs.add(t == Target.TAB ? TabExport.export(state) : new Output(t).fail("no sidebar scoreboard"));
        }
        return finish(key, state, outputs);
    }

    public static Report hologram(Hologram h, List<Target> targets) {
        String key = CopyStore.saveKey();
        CopyStore.State state = CopyStore.load(key);
        List<Output> outputs = new ArrayList<>();
        for (Target t : targets) {
            String reason = switch (t) {
                case DECENT_HOLOGRAMS -> DecentExport.unsupported(h);
                case CMI -> CmiExport.unsupported(h);
                default -> null;
            };
            if (reason != null) {
                outputs.add(new Output(t).fail(reason));
                continue;
            }
            List<Hologram> list = state.holograms(t);
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                if (h.sameSpot(list.get(i))) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) list.set(index, h);
            else list.add(h);
            Output all = exportHolograms(t, list);
            all.notes.replaceWith(exportHolograms(t, List.of(h)).notes);
            outputs.add(all);
        }
        return finish(key, state, outputs);
    }

    private static Output exportHolograms(Target t, List<Hologram> list) {
        return switch (t) {
            case DECENT_HOLOGRAMS -> DecentExport.export(list);
            case FANCY_HOLOGRAMS -> FancyExport.export(list);
            case CMI -> CmiExport.holograms(list);
            default -> new Output(t).fail("no holograms");
        };
    }

    private static Report finish(String key, CopyStore.State state, List<Output> outputs) {
        Path dir = CopyStore.serverDir(key);
        boolean wrote = false;
        for (Output o : outputs) {
            if (o.error != null || o.files.isEmpty()) continue;
            try {
                if (o.target == Target.DECENT_HOLOGRAMS) CopyStore.prune(key, "DecentHolograms/holograms", o.files.keySet());
                CopyStore.write(key, o.files);
                wrote = true;
            } catch (IOException e) {
                Hontun.LOG.warn("[Hontun] Could not write the copied {} files: {}", o.target.label, e.toString());
                o.error = "could not write the files";
            }
        }
        if (wrote) CopyStore.save(key, state);
        return new Report(outputs, dir, null, wrote);
    }

    public static MutableComponent describe(String what, Report report) {
        List<String> copied = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        MutableComponent details = Component.empty();
        StringBuilder log = new StringBuilder("[Copy] ").append(what);
        boolean anyNote = false;
        for (Output o : report.outputs()) {
            if (o.error != null) {
                skipped.add(o.target.label + " (" + o.error + ")");
                log.append("\n  ").append(o.target.label).append(": skipped, ").append(o.error);
                continue;
            }
            copied.add(o.target.label);
            log.append("\n  ").append(o.target.label).append(": ").append(String.join(", ", o.files.keySet()));
            for (String note : o.notes.lines()) {
                if (anyNote) details.append(Component.literal("\n"));
                details.append(Out.value(o.target.label)).append(Out.punct(": ")).append(Out.text(note));
                log.append("\n    - ").append(note);
                anyNote = true;
            }
        }
        if (report.dir() != null) log.append("\n  folder: ").append(report.dir().toAbsolutePath());
        Hontun.LOG.info(log.toString());

        MutableComponent msg = Out.header(what);
        if (!copied.isEmpty()) msg.append(Out.punct(" → ")).append(Out.value(String.join(", ", copied)));
        if (anyNote) {
            MutableComponent hover = details;
            msg.append(Out.sep()).append(Out.muted("details").withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(hover))));
        }
        if (!skipped.isEmpty()) msg.append(Component.literal("\n  ")).append(Out.kv("Skipped", Out.muted(String.join(", ", skipped))));
        if (report.dir() != null && report.wrote()) {
            Path dir = report.dir();
            String shown = CopyStore.root().relativize(dir).toString().replace('\\', '/');
            MutableComponent link = Out.value(shown).withStyle(s -> s
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenFile(dir.toFile()))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(dir.toAbsolutePath().toString()))));
            msg.append(Component.literal("\n  ")).append(Out.kv("Saved to", link));
        }
        return msg;
    }
}
