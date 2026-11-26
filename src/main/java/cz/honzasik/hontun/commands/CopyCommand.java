package cz.honzasik.hontun.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import cz.honzasik.hontun.copy.Copier;
import cz.honzasik.hontun.copy.HologramPicker;
import cz.honzasik.hontun.copy.Recorder;
import cz.honzasik.hontun.copy.Target;
import cz.honzasik.hontun.copy.capture.SidebarCapture;
import cz.honzasik.hontun.copy.capture.TabCapture;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;

public class CopyCommand extends Command {
    public CopyCommand() {
        super("copy", "Copies the tab list, sidebar scoreboard or holograms into configs for TAB, DecentHolograms, FancyHolograms and CMI.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("tab")
            .executes(ctx -> {
                tab(null);
                return SINGLE_SUCCESS;
            })
            .then(config(Target.Kind.TAB).executes(ctx -> {
                tab(StringArgumentType.getString(ctx, "config"));
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("scoreboard")
            .executes(ctx -> {
                scoreboard(null);
                return SINGLE_SUCCESS;
            })
            .then(config(Target.Kind.SCOREBOARD).executes(ctx -> {
                scoreboard(StringArgumentType.getString(ctx, "config"));
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("holograms")
            .then(literal("on")
                .executes(ctx -> {
                    holograms(null);
                    return SINGLE_SUCCESS;
                })
                .then(config(Target.Kind.HOLOGRAMS).executes(ctx -> {
                    holograms(StringArgumentType.getString(ctx, "config"));
                    return SINGLE_SUCCESS;
                })))
            .then(literal("off").executes(ctx -> {
                if (!HologramPicker.INSTANCE.active()) {
                    warning("Hologram copy mode is not on.");
                } else {
                    HologramPicker.INSTANCE.stop();
                    info("Hologram copy mode is off.");
                }
                return SINGLE_SUCCESS;
            })));
    }

    private static RequiredArgumentBuilder<ClientSuggestionProvider, String> config(Target.Kind kind) {
        return argument("config", StringArgumentType.word())
            .suggests((ctx, sb) -> SharedSuggestionProvider.suggest(Target.labels(kind), sb));
    }

    private List<Target> targets(Target.Kind kind, String raw) {
        if (raw == null) return Target.all(kind);
        Target t = Target.parse(raw);
        if (t == null) {
            error("Unknown config %s. Use one of: %s.", raw, String.join(", ", Target.labels(kind)));
            return null;
        }
        if (!t.supports(kind)) {
            String what = switch (kind) {
                case TAB -> "tab list";
                case SCOREBOARD -> "sidebar scoreboard";
                case HOLOGRAMS -> "holograms";
            };
            error("%s has no %s config. Use one of: %s.", t.label, what, String.join(", ", Target.labels(kind)));
            return null;
        }
        return List.of(t);
    }

    private boolean record(Target.Kind kind, String what) {
        if (mc.level == null || mc.getConnection() == null) {
            error("Not connected to a server.");
            return false;
        }
        if (Recorder.INSTANCE.busy()) {
            error("Already recording, wait a few seconds.");
            return false;
        }
        info("Recording the %s for a few seconds to catch animations...", what);
        return true;
    }

    private void tab(String raw) {
        List<Target> targets = targets(Target.Kind.TAB, raw);
        if (targets == null || !record(Target.Kind.TAB, "tab list")) return;
        Recorder.INSTANCE.start(Target.Kind.TAB, result -> {
            if (result.error() != null) {
                error(result.error());
                return;
            }
            Copier.Report report = Copier.tab(targets, TabCapture.take(), result.tab());
            if (report.error() != null) {
                error(report.error());
                return;
            }
            info(Copier.describe("Tab list copied", report));
        });
    }

    private void scoreboard(String raw) {
        List<Target> targets = raw == null ? List.of(Target.TAB, Target.CMI) : targets(Target.Kind.SCOREBOARD, raw);
        if (targets == null || !record(Target.Kind.SCOREBOARD, "sidebar scoreboard")) return;
        Recorder.INSTANCE.start(Target.Kind.SCOREBOARD, result -> {
            if (result.error() != null) {
                error(result.error());
                return;
            }
            Copier.Report report = Copier.scoreboard(targets, SidebarCapture.take(), result.sidebar());
            if (report.error() != null) {
                error(report.error());
                return;
            }
            info(Copier.describe("Scoreboard copied", report));
        });
    }

    private void holograms(String raw) {
        List<Target> targets = targets(Target.Kind.HOLOGRAMS, raw);
        if (targets == null) return;
        if (mc.level == null) {
            error("Not connected to a server.");
            return;
        }
        HologramPicker.INSTANCE.start(targets);
        StringBuilder names = new StringBuilder();
        for (Target t : targets) {
            if (!names.isEmpty()) names.append(", ");
            names.append(t.label);
        }
        info("Hologram copy mode is on for %s.", names);
        info(HologramPicker.hint());
    }
}
