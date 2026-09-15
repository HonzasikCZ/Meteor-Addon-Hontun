package cz.honzasik.hontun.utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class HontunFlags {
    private static final int CELL_W = 32, CELL_H = 18, COLS = 16;
    private static final int TEX_W = 512, TEX_H = 288;

    private static final int[] WIDTHS = {
        26, 32, 27, 27, 32, 25, 32, 27, 27, 29, 32, 27, 32, 27, 28, 32, 32, 27, 30, 21, 27, 30, 30, 30,
        27, 27, 32, 32, 26, 27, 26, 32, 27, 25, 27, 32, 30, 32, 32, 24, 27, 27, 18, 27, 32, 27, 27, 27,
        27, 30, 32, 31, 27, 32, 27, 27, 30, 27, 24, 32, 27, 27, 27, 28, 27, 32, 32, 27, 32, 27, 29, 32,
        32, 32, 25, 27, 24, 32, 30, 27, 27, 27, 27, 32, 27, 27, 27, 27, 27, 27, 32, 29, 32, 32, 30, 27,
        32, 32, 32, 30, 32, 27, 32, 25, 32, 27, 32, 27, 32, 25, 27, 30, 32, 32, 27, 27, 30, 28, 32, 30,
        27, 32, 27, 32, 32, 32, 27, 27, 32, 30, 32, 32, 27, 30, 30, 32, 32, 27, 23, 32, 32, 27, 27, 32,
        32, 27, 27, 32, 27, 32, 27, 27, 32, 27, 27, 27, 27, 32, 32, 27, 27, 32, 21, 32, 32, 30, 27, 25,
        15, 32, 32, 32, 32, 27, 27, 27, 24, 32, 27, 29, 27, 32, 27, 32, 27, 29, 32, 32, 27, 27, 27, 27,
        27, 27, 32, 32, 32, 29, 27, 32, 32, 25, 27, 27, 24, 27, 27, 27, 32, 32, 32, 27, 27, 27, 32, 27,
        27, 29, 27, 32, 32, 32, 27, 27, 32, 27, 30, 32, 27, 27, 27, 27, 32, 27, 32, 27, 32, 18, 27, 27,
        32, 27, 27, 30, 27, 32, 25, 27, 27, 27, 27, 32
    };

    private static final Identifier TEX = Identifier.fromNamespaceAndPath("hontun", "textures/flags.png");

    private static final String CODES =
        "adaeafagaialamaoaqarasatauawaxazbabbbdbebfbgbhbibjblbmbnbobqbrbsbtbvbwbybz" +
        "cacccdcfcgchcickclcmcncocrcucvcwcxcyczdedjdkdmdodzeceeegehereseteufifjfkfm" +
        "fofrgagbgdgegfggghgiglgmgngpgqgrgsgtgugwgyhkhmhnhrhthuidieiliminioiqirisit" +
        "jejmjojpkekgkhkikmknkpkrkwkykzlalblclilklrlsltlulvlymamcmdmemfmgmhmkmlmmmn" +
        "mompmqmrmsmtmumvmwmxmymznancnenfngninlnonpnrnunzompapepfpgphpkplpmpnprpspt" +
        "pwpyqarerorsrurwsasbscsdsesgshsisjskslsmsnsosrssstsvsxsysztctdtftgthtjtktl" +
        "tmtntotrtttvtwtzuaugumunusuyuzvavcvevgvivnvuwfwsxkyeytzazmzw";

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

    public static int drawRight(GuiGraphicsExtractor g, String code, int right, int y, int h) {
        int idx = indexOf(code);
        if (idx < 0) return 0;
        int srcW = WIDTHS[idx];
        if (srcW <= 0) return 0;
        int w = Math.max(1, srcW * h / CELL_H);
        float u = (idx % COLS) * CELL_W;
        float v = (idx / COLS) * CELL_H;
        g.blit(RenderPipelines.GUI_TEXTURED, TEX, right - w, y, u, v, w, h, srcW, CELL_H, TEX_W, TEX_H);
        return w;
    }

    public static int drawRightFramed(GuiGraphicsExtractor g, String code, int right, int y, int h) {
        int w = drawRight(g, code, right, y, h);
        if (w == 0) return 0;
        int x = right - w;
        int frame = 0x70000000;
        g.fill(x - 1, y - 1, x + w + 1, y, frame);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, frame);
        g.fill(x - 1, y, x, y + h, frame);
        g.fill(x + w, y, x + w + 1, y + h, frame);
        return w;
    }

    public static void placeholder(GuiGraphicsExtractor g, int right, int y, int w, int h) {
        int x = right - w;
        g.fill(x, y, x + w, y + h, 0x30FFFFFF);
        int frame = 0x50000000;
        g.fill(x - 1, y - 1, x + w + 1, y, frame);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, frame);
        g.fill(x - 1, y, x, y + h, frame);
        g.fill(x + w, y, x + w + 1, y + h, frame);
    }
}
