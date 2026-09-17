package cz.honzasik.hontun.utils;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public final class ResourcePackInfo {
    public static volatile boolean present = false;
    public static volatile String url = "";
    public static volatile String hash = "";
    public static volatile boolean required = false;
    public static volatile String prompt = "";

    private ResourcePackInfo() {}

    public static void got(ClientboundResourcePackPushPacket packet) {
        url = packet.url() == null ? "" : packet.url();
        hash = packet.hash() == null ? "" : packet.hash();
        required = packet.required();
        prompt = packet.prompt().map(c -> c.getString()).orElse("");
        present = true;
    }

    public static void clear() {
        present = false;
        url = "";
        hash = "";
        required = false;
        prompt = "";
    }
}
