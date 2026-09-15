package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.mixin.network.custompayload.ClientListenerConnectionAccessor;
import cz.honzasik.hontun.mixin.network.custompayload.ConnectionChannelAccessor;
import cz.honzasik.hontun.network.HontunPayload;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class MCUtil {
    public static final Minecraft MC = Minecraft.getInstance();

    public static ClientPacketListener getPlayNetHandler() {
        return MC.getConnection();
    }

    public static void sendPacket(Packet<?> packet) {
        ClientPacketListener handler = getPlayNetHandler();
        if (handler == null) return;
        handler.send(packet);
    }

    public static void sendCustomPayload(Identifier channel, Consumer<ByteArrayDataOutput> writer) {
        ByteArrayDataOutput container = ByteStreams.newDataOutput();
        writer.accept(container);
        sendCustomPayload(channel, container.toByteArray());
    }

    public static void sendCustomPayload(Identifier channel, byte[] data) {
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(new HontunPayload(channel, data));

        ClientPacketListener handler = getPlayNetHandler();
        if (handler == null) {
            Hontun.LOG.warn("[ChannelSender] not connected – payload on {} not sent", channel);
            return;
        }

        try {
            Connection conn = ((ClientListenerConnectionAccessor) handler).hontun$getConnection();
            Channel ch = conn == null ? null : ((ConnectionChannelAccessor) (Object) conn).hontun$getChannel();
            if (ch != null && ch.isActive()) {
                ch.writeAndFlush(packet);
                return;
            }
        } catch (Throwable t) {
            Hontun.LOG.warn("[ChannelSender] direct channel write failed, falling back to send()", t);
        }

        handler.send(packet);
    }

    public static boolean notCreative() {
        return MC.player != null && !MC.player.getAbilities().instabuild;
    }

    public static boolean isCommandRegistered(String commandName) {
        ClientPacketListener handler = getPlayNetHandler();
        return handler != null && handler.getCommands().getRoot().getChild(commandName) != null;
    }

    public static void sendCommand(String command) {
        ClientPacketListener handler = getPlayNetHandler();
        if (handler != null) handler.sendCommand(command);
    }

    public static ItemStack getStackInSlot(int slot) {
        return MC.player.getInventory().getItem(slot);
    }

    public static int getSelectedSlot() {
        return MC.player.getInventory().getSelectedSlot();
    }

    public static ItemStack getStackInSelectedSlot() {
        return getStackInSlot(getSelectedSlot());
    }

    public static void forceMainThread(Runnable runnable) {
        MC.execute(runnable);
    }
}
