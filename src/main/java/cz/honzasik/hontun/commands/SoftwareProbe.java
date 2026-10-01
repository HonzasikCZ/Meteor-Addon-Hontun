package cz.honzasik.hontun.commands;

import cz.honzasik.hontun.utils.FreshFreeze;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
import cz.honzasik.hontun.utils.MobCadence;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.ServerSoftware.Evidence;
import cz.honzasik.hontun.utils.ServerSoftware.Family;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.MutableComponent;

public class SoftwareProbe {
    public static final SoftwareProbe INSTANCE = new SoftwareProbe();

    private SoftwareProbe() {}

    public void run() {
        if (MCUtil.getPlayNetHandler() == null) {
            ChatUtils.error("Not connected to a server.");
            return;
        }
        report(ServerSoftware.detect());
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        ServerSoftware.observe(event.packet);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MobCadence.tick();
        FreshFreeze.tick();
        ServerSoftware.tick();
    }

    public static String summary(ServerSoftware.Result r) {
        StringBuilder sb = new StringBuilder(r.software());
        sb.append(" (").append(r.confidence()).append(" confidence)");
        if (r.proxy() != null) sb.append(" behind ").append(r.proxy());
        return sb.toString();
    }

    public static void report(ServerSoftware.Result r) {
        MutableComponent msg = HontunChat.light("Software: ").append(HontunChat.value(r.software()));
        msg.append(HontunChat.punct(" (")).append(HontunChat.light(r.confidence() + " confidence")).append(HontunChat.punct(")"));
        msg.append(HontunChat.light("\nStructure: "))
                .append(r.family() == Family.UNKNOWN ? HontunChat.light("inconclusive") : HontunChat.value(r.family().label))
                .append(HontunChat.punct(" (what the packet methods think)"));
        if (r.brand() != null && !r.brand().isBlank()) {
            msg.append(HontunChat.light("\nBrand: ")).append(HontunChat.value(r.brand()));
        }
        msg.append(HontunChat.light("\nProxy: "))
                .append(r.proxy() != null ? HontunChat.value(r.proxy()) : HontunChat.light("none detected"));
        if (!r.evidence().isEmpty()) {
            msg.append(HontunChat.light("\nEvidence:"));
            for (Evidence e : r.evidence()) {
                boolean paperOnBukkit = r.family() == Family.PAPER && e.family() == Family.BUKKIT && !e.againstPaper();
                boolean supports = e.weight() > 0 && (e.family() == r.family() || paperOnBukkit);
                boolean against = (e.weight() > 0 && !supports)
                        || (e.againstPaper() && r.family() == Family.PAPER);
                String mark = supports ? " + " : against ? " - " : " · ";
                msg.append(HontunChat.dim("\n" + mark)).append(HontunChat.light(e.signal() + ": "))
                        .append(HontunChat.punct(e.detail()));
                if (e.weight() > 0) {
                    msg.append(HontunChat.dim(" [")).append(supports ? HontunChat.value(e.family().label + " +" + e.weight())
                            : HontunChat.light(e.family().label + " +" + e.weight())).append(HontunChat.dim("]"));
                }
            }
        }
        for (String n : r.notes()) {
            msg.append(HontunChat.dim("\n ! ")).append(HontunChat.punct(n));
        }
        ChatUtils.sendMsg(msg);
    }
}
