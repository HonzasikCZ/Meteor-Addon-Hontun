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
    public int body, roleBg;
    public double shift, tone;

    public void setSurfaces(int[] s) {
        crust = s[0];
        mantle = s[1];
        base = s[2];
        surface0 = s[3];
        surface1 = s[4];
        surface2 = s[5];
        overlay0 = s[6];
        overlay1 = s[7];
        overlay2 = s[8];
    }

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
        body = 0;
        roleBg = 0;
        shift = 0;
        tone = 0;
    }

    public void roles(int bg, int body, boolean light) {
        this.body = body;
        this.roleBg = bg;
        accentText = HontunLightPalette.text(accent, bg, light);
        accentHiText = light ? accentText : HontunLightPalette.text(accentHi, bg, false);
        accentLine = HontunLightPalette.line(accent, bg, light);
        accentHiLine = HontunLightPalette.line(accentHi, bg, light);
        accentLoLine = HontunLightPalette.line(accentLo, bg, light);
        accentFaint = HontunLightPalette.faint(accent, body, crust, mantle, base, surface0, surface1, surface2);
        accentBar = accentFaint ? accentLine : accent;
        accentHiBar = accentFaint ? accentHiLine : accentHi;
        accentLoBar = accentFaint ? accentLoLine : accentLo;
        onAccent = light
                ? HontunLightPalette.readableInk(accent, accent, text, base)
                : HontunLightPalette.readableInk(accent, accent, crust, text);
    }
}
