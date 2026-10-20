package cz.honzasik.hontun.gui.theme.style;

import cz.honzasik.hontun.gui.theme.HontunLightPalette;

public class StylePalette {
    public int crust, mantle, base;
    public int surface0, surface1, surface2;
    public int overlay0, overlay1, overlay2;
    public int text, subtext1, subtext0;
    public int accent, accentHi, accentLo;
    public int green, yellow, red;
    public int userAccent, userAccentHi, userAccentLo;
    public int accentText, accentHiText;
    public int accentLine, accentHiLine, accentLoLine;
    public int accentBar, accentHiBar, accentLoBar;
    public int onAccent;
    public boolean accentFaint;

    public void copyRoles() {
        accentText = accent;
        accentHiText = accentHi;
        accentLine = accent;
        accentHiLine = accentHi;
        accentLoLine = accentLo;
        accentBar = accent;
        accentHiBar = accentHi;
        accentLoBar = accentLo;
        onAccent = HontunLightPalette.ink(accent, crust, base);
        accentFaint = false;
    }

    public void lightRoles(int bg, int body) {
        accentText = HontunLightPalette.text(accent, bg);
        accentHiText = accentText;
        accentLine = HontunLightPalette.line(accent, bg);
        accentHiLine = HontunLightPalette.line(accentHi, bg);
        accentLoLine = HontunLightPalette.line(accentLo, bg);
        accentFaint = HontunLightPalette.faint(accent, body, crust, mantle, base, surface0, surface1, surface2);
        accentBar = accentFaint ? accentLine : accent;
        accentHiBar = accentFaint ? accentHiLine : accentHi;
        accentLoBar = accentFaint ? accentLoLine : accentLo;
        onAccent = HontunLightPalette.readableInk(accent, accent, text, base);
    }
}
