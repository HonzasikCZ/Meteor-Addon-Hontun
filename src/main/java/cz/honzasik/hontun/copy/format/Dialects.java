package cz.honzasik.hontun.copy.format;

public final class Dialects {
    public static final LegacyWriter.Dialect TAB = new Tab(false, false);
    public static final LegacyWriter.Dialect TAB_SCOREBOARD = new Tab(true, false);
    public static final LegacyWriter.Dialect TAB_LAYOUT = new Tab(false, true);
    public static final LegacyWriter.Dialect DECENT = new Decent();
    public static final LegacyWriter.Dialect CMI = new Cmi();

    private static final String[] DECENT_TOKENS = {"x]", "X]", "/]", ",]", ",,]", ",,,]", "p]", "P]", "|]"};

    private Dialects() {}

    private record Tab(boolean pipes, boolean layout) implements LegacyWriter.Dialect {
        @Override
        public String name() {
            return "TAB";
        }

        @Override
        public String hex(int rgb) {
            return "&#" + LegacyWriter.hex6(rgb);
        }

        @Override
        public boolean fonts() {
            return true;
        }

        @Override
        public boolean softHex() {
            return true;
        }

        @Override
        public GradientFit.Curve curve() {
            return Curves.MINI_MESSAGE;
        }

        @Override
        public String gradient(int from, int to, String decorations, String text) {
            return "<#" + LegacyWriter.hex6(from) + ">" + decorations + text + "</#" + LegacyWriter.hex6(to) + ">";
        }

        @Override
        public String escape(String text, String codes, boolean inFont) {
            StringBuilder sb = new StringBuilder(text.length() + 8);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                char next = i + 1 < text.length() ? text.charAt(i + 1) : 0;
                if (layout && c == '|') {
                    sb.append('│');
                    continue;
                }
                sb.append(c);
                if (c == '&' && next != 0 && LegacyWriter.codeChar(next, "#")) sb.append(codes);
                else if (c == '#' && LegacyWriter.hexAt(text, i + 1)) sb.append(codes);
                else if (c == '<' && next != 0 && tagStart(next)) sb.append(inFont ? codes : "<font:minecraft:default></font>" + codes);
                else if (c == '\\' && next == 'n') sb.append(codes);
                else if (pipes && c == '|' && (next == '|' || next == 0)) sb.append(codes.isEmpty() ? "&f" : codes);
            }
            return sb.toString();
        }
    }

    private static final class Decent implements LegacyWriter.Dialect {
        @Override
        public String name() {
            return "DecentHolograms";
        }

        @Override
        public String hex(int rgb) {
            return "&#" + LegacyWriter.hex6(rgb);
        }

        @Override
        public GradientFit.Curve curve() {
            return Curves.DECENT;
        }

        @Override
        public String gradient(int from, int to, String decorations, String text) {
            return "<#" + LegacyWriter.hex6(from) + ">" + decorations + text + "</#" + LegacyWriter.hex6(to) + ">";
        }

        @Override
        public String escape(String text, String codes, boolean inFont) {
            StringBuilder sb = new StringBuilder(text.length() + 8);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                char next = i + 1 < text.length() ? text.charAt(i + 1) : 0;
                sb.append(c);
                if (c == '&' && next != 0 && LegacyWriter.codeChar(next, "u#")) sb.append(codes);
                else if (c == '#' && LegacyWriter.hexAt(text, i + 1)) sb.append(codes);
                else if ((c == '<' || c == '{') && animation(text, i + 1)) sb.append(codes);
                else if (c == '{' && (text.startsWith("player}", i + 1) || text.startsWith("page}", i + 1) || text.startsWith("pages}", i + 1))) sb.append(codes);
                else if (c == '[' && token(text, i + 1)) sb.append(codes);
            }
            return sb.toString();
        }

        private static boolean animation(String s, int at) {
            String rest = s.substring(at).toUpperCase(java.util.Locale.ROOT);
            return rest.startsWith("#ANIM:") || rest.startsWith("ANIM:") || rest.startsWith("/ANIM") || rest.startsWith("/#ANIM")
                || rest.startsWith("RAINBOW") || rest.startsWith("/RAINBOW") || rest.startsWith("#") || rest.startsWith("/#");
        }

        private static boolean token(String s, int at) {
            for (String t : DECENT_TOKENS) if (s.startsWith(t, at)) return true;
            return false;
        }
    }

    private static final class Cmi implements LegacyWriter.Dialect {
        @Override
        public String name() {
            return "CMI";
        }

        @Override
        public String hex(int rgb) {
            return "{#" + LegacyWriter.hex6(rgb) + "}";
        }

        @Override
        public GradientFit.Curve curve() {
            return Curves.CMI;
        }

        @Override
        public String gradient(int from, int to, String decorations, String text) {
            return "{#" + LegacyWriter.hex6(from) + ">}" + decorations + text + "{#" + LegacyWriter.hex6(to) + "<}";
        }

        @Override
        public String escape(String text, String codes, boolean inFont) {
            StringBuilder sb = new StringBuilder(text.length() + 8);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                char next = i + 1 < text.length() ? text.charAt(i + 1) : 0;
                sb.append(c);
                if (c == '&' && next != 0 && LegacyWriter.codeChar(next, "u#")) sb.append(codes);
                else if (c == '#' && LegacyWriter.hexAt(text, i + 1)) sb.append(codes);
                else if (c == '{' && next == '#') sb.append(codes);
                else if (c == '<' && next != 0 && tagStart(next)) sb.append(codes);
            }
            return sb.toString();
        }
    }

    private static boolean tagStart(char c) {
        return Character.isLetter(c) || c == '#' || c == '/' || c == '$' || c == '!' || c == '?' || c == ':';
    }
}
