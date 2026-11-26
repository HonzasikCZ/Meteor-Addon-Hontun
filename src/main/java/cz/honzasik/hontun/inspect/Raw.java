package cz.honzasik.hontun.inspect;

import com.mojang.serialization.JsonOps;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.format.MiniMessageWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

final class Raw {
    private Raw() {}

    static String mm(Component c) {
        if (c == null) return "";
        return MiniMessageWriter.write(StyledText.of(c), null);
    }

    static String json(Component c) {
        if (c == null) return "null";
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return "?";
        try {
            return ComponentSerialization.CODEC.encodeStart(mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE), c)
                .result().map(Object::toString).orElse("?");
        } catch (Throwable t) {
            return "?";
        }
    }

    static String escape(String s) {
        if (s == null) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§') sb.append('&');
            else if (c < 0x20 || c == 0x7F) sb.append(String.format("\\u%04X", (int) c));
            else sb.append(c);
        }
        return sb.isEmpty() ? "(empty)" : sb.toString();
    }

    static MutableComponent inert(Component c) {
        if (c == null) return Component.empty();
        MutableComponent out = MutableComponent.create(c.getContents())
            .setStyle(c.getStyle().withClickEvent(null).withHoverEvent(null).withInsertion(null));
        for (Component sibling : c.getSiblings()) out.append(inert(sibling));
        return out;
    }

    static boolean looksEmpty(StyledText text) {
        for (StyledText.Run r : text.runs()) {
            if (!r.text().isBlank() || r.look().visibleOnSpace()) return false;
        }
        return true;
    }

    static MutableComponent copyable(MutableComponent shown, Component source, String what) {
        String copy = mm(source);
        MutableComponent hover = Out.text(what).append(Component.literal("\n"))
            .append(Out.kv("MiniMessage", Out.value(copy)));
        return shown.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(hover)).withClickEvent(new ClickEvent.CopyToClipboard(copy)));
    }
}
