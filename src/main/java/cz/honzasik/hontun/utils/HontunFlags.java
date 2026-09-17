package cz.honzasik.hontun.utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class HontunFlags {
    public static final int W = 12, H = 8;

    private static final int COLS = 16;
    private static final int CELL_W = 60, CELL_H = 40, PAD = 2;
    private static final int STRIDE_W = CELL_W + PAD * 2, STRIDE_H = CELL_H + PAD * 2;
    private static final int TEX_W = 1024, TEX_H = 704;

    private static final Identifier TEX = Identifier.fromNamespaceAndPath("hontun", "textures/flags.png");

    private static final String CODES =
        "adaeafagaialamaoaqarasatauawaxazbabbbdbebfbgbhbibjblbmbnbobqbrbsbtbwbybz" +
        "cacccdcfcgchcickclcmcncocrcucvcwcxcyczdedjdkdmdodzeceeegehereseteufifjfk" +
        "fmfofrgagbgdgegfggghgiglgmgngpgqgrgsgtgugwgyhkhnhrhthuidieiliminioiqiris" +
        "itjejmjojpkekgkhkikmknkpkrkwkykzlalblclilklrlsltlulvlymamcmdmemfmgmhmkml" +
        "mmmnmompmqmrmsmtmumvmwmxmymznancnenfngninlnonpnrnunzompapepfpgphpkplpmpn" +
        "prpsptpwpyqarerorsrurwsasbscsdsesgshsiskslsmsnsosrssstsvsxsysztctdtftgth" +
        "tjtktltmtntotrtttvtwtzuaugunusuyuzvavcvevgvivnvuwfwsxkyeytzazmzw";

    private HontunFlags() {}

    private static int indexOf(String code) {
        if (code == null || code.length() != 2) return -1;
        String c = code.toLowerCase(Locale.ROOT);
        char a = c.charAt(0), b = c.charAt(1);
        for (int i = 0; i < CODES.length(); i += 2) {
            if (CODES.charAt(i) == a && CODES.charAt(i + 1) == b) return i >> 1;
        }
        return -1;
    }

    public static boolean has(String code) {
        return indexOf(code) >= 0;
    }

    public static boolean draw(GuiGraphicsExtractor g, String code, int x, int y) {
        int idx = indexOf(code);
        if (idx < 0) return false;
        float u = (idx % COLS) * STRIDE_W + PAD;
        float v = (idx / COLS) * STRIDE_H + PAD;
        g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, u, v, W, H, CELL_W, CELL_H, TEX_W, TEX_H);
        return true;
    }

    public static boolean drawRight(GuiGraphicsExtractor g, String code, int right, int y) {
        return draw(g, code, right - W, y);
    }

    public static void placeholder(GuiGraphicsExtractor g, int right, int y) {
        int x = right - W;
        g.fill(x, y, x + W, y + H, 0x28FFFFFF);
    }
}
