package cz.honzasik.hontun.utils;

import com.google.common.io.ByteArrayDataOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface MCUtilWrapper {
    Minecraft MC = MCUtil.MC;

    default ClientPacketListener getPlayNetHandler() {
        return MCUtil.getPlayNetHandler();
    }

    default void sendPacket(Packet<?> packet) {
        MCUtil.sendPacket(packet);
    }

    default void sendCustomPayload(Identifier channel, Consumer<ByteArrayDataOutput> writer) {
        MCUtil.sendCustomPayload(channel, writer);
    }

    default void sendCustomPayload(Identifier channel, byte[] data) {
        MCUtil.sendCustomPayload(channel, data);
    }

    default boolean notCreative() {
        return MCUtil.notCreative();
    }

    default boolean isCommandRegistered(String commandName) {
        return MCUtil.isCommandRegistered(commandName);
    }

    default void sendCommand(String command) {
        MCUtil.sendCommand(command);
    }

    default ItemStack getStackInSlot(int slot) {
        return MCUtil.getStackInSlot(slot);
    }

    default int getSelectedSlot() {
        return MCUtil.getSelectedSlot();
    }

    default ItemStack getStackInSelectedSlot() {
        return MCUtil.getStackInSelectedSlot();
    }

    default void forceMainThread(Runnable runnable) {
        MCUtil.forceMainThread(runnable);
    }
}
