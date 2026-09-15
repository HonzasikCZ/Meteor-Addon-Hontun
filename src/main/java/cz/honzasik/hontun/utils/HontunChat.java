package cz.honzasik.hontun.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public final class HontunChat {
    private static int cName()    { return HontunTheme.accent(); }
    private static int cBracket() { return HontunTheme.overlay0(); }
    private static int cTag()     { return HontunTheme.subtext1(); }
    private static int cLight()   { return HontunTheme.textLight(); }
    private static int cValue()   { return HontunTheme.accentHi(); }
    private static int cPunct()   { return HontunTheme.textDim(); }

    private HontunChat() {}

    public static Component prefix() {
        MutableComponent out = Component.empty();
        out.append(styled("[", cBracket(), true));
        out.append(styled("Hontun", cName(), true));
        out.append(styled("]", cBracket(), true));
        out.append(Component.literal(" "));
        return out;
    }

    public static MutableComponent tag(String title) {
        MutableComponent out = Component.empty();
        out.append(styled("[", cBracket(), false));
        out.append(styled(title, cTag(), false));
        out.append(styled("]", cBracket(), false));
        out.append(Component.literal(" "));
        return out;
    }

    public static MutableComponent light(String s) { return styled(s, cLight(), false); }
    public static MutableComponent value(String s) { return styled(s, cValue(), false); }
    public static MutableComponent punct(String s) { return styled(s, cPunct(), false); }
    public static MutableComponent dim(String s)   { return styled(s, cBracket(), false); }

    public static MutableComponent formatInfo(String raw) {
        boolean hasMarkers = raw.contains("(highlight)");

        StringBuilder plain = new StringBuilder();
        List<Boolean> hl = new ArrayList<>();
        List<Integer> sec = new ArrayList<>();
        boolean h = false;
        int section = -1;

        for (int i = 0; i < raw.length(); ) {
            if (raw.startsWith("(highlight)", i)) { h = true;  i += 11; continue; }
            if (raw.startsWith("(default)", i))   { h = false; section = -1; i += 9; continue; }
            if (raw.startsWith("(underline)", i)) { i += 11; continue; }
            if (raw.startsWith("(bold)", i))      { i += 6;  continue; }

            char c = raw.charAt(i);
            if (c == '§' && i + 1 < raw.length()) {
                int mapped = mapSection(Character.toLowerCase(raw.charAt(i + 1)));
                if (mapped != Integer.MIN_VALUE) section = mapped;
                i += 2;
                continue;
            }
            plain.append(c);
            hl.add(h);
            sec.add(section);
            i++;
        }

        String s = plain.toString();
        int colon = s.indexOf(':');
        boolean listItem = s.stripLeading().startsWith("-");

        MutableComponent out = Component.empty();
        StringBuilder run = new StringBuilder();
        int runColor = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int color;
            if (sec.get(i) >= 0) color = sec.get(i);
            else if (isPunct(c)) color = cPunct();
            else if (Character.isDigit(c)) color = cValue();
            else if (hl.get(i)) color = cValue();
            else if (!hasMarkers && (listItem || (colon >= 0 && i > colon))) color = cValue();
            else color = cLight();

            if (color != runColor && run.length() > 0) {
                out.append(styled(run.toString(), runColor, false));
                run.setLength(0);
            }
            runColor = color;
            run.append(c);
        }
        if (run.length() > 0) out.append(styled(run.toString(), runColor, false));
        return out;
    }

    private static int mapSection(char code) {
        switch (code) {
            case '0': return 0x000000;
            case '1': return 0x0000AA;
            case '2': return 0x00AA00;
            case '3': return 0x00AAAA;
            case '4': return 0xAA0000;
            case '5': return 0xAA00AA;
            case '6': return 0xFFAA00;
            case '7': return 0xAAAAAA;
            case '8': return 0x555555;
            case '9': return 0x5555FF;
            case 'a': return 0x00FF00;
            case 'b': return 0x55FFFF;
            case 'c': return 0xFF0000;
            case 'd': return 0xFF55FF;
            case 'e': return 0xFFFF55;
            case 'f': return 0xFFFFFF;
            case 'r': return -1;
            default:  return Integer.MIN_VALUE;
        }
    }

    private static boolean isPunct(char c) {
        return c == ':' || c == '(' || c == ')' || c == '[' || c == ']' || c == '-' || c == ',';
    }

    private static MutableComponent styled(String s, int rgb, boolean bold) {
        return Component.literal(s).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(bold));
    }
}
