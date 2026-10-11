package cz.honzasik.hontun.gui.render.text;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class BundledFonts {
    public static final String LEXEND_DECA = "Lexend Deca";

    private static final Logger LOG = LoggerFactory.getLogger("Hontun/Fonts");

    private static Path lexendRegular;
    private static Path lexendBold;
    private static boolean failed;

    private BundledFonts() {}

    public static void register() {
        if (failed || Fonts.getFamily(LEXEND_DECA) != null) return;

        try {
            if (lexendRegular == null) lexendRegular = extract("/assets/hontun/font/lexend-deca-regular.ttf", "hontun-lexend-deca-regular");
            if (lexendBold == null) lexendBold = extract("/assets/hontun/font/lexend-deca-bold.ttf", "hontun-lexend-deca-bold");
        } catch (IOException e) {
            failed = true;
            LOG.warn("[Hontun] Lexend Deca could not be added to the font list", e);
            return;
        }

        FontFamily family = new FontFamily(LEXEND_DECA);
        family.addFont(new SystemFontFace(new FontInfo(LEXEND_DECA, FontInfo.Type.Regular), lexendRegular));
        family.addFont(new SystemFontFace(new FontInfo(LEXEND_DECA, FontInfo.Type.Bold), lexendBold));
        Fonts.FONT_FAMILIES.add(family);
    }

    private static Path extract(String resource, String prefix) throws IOException {
        Path path = Files.createTempFile(prefix, ".ttf");
        path.toFile().deleteOnExit();

        try (InputStream in = BundledFonts.class.getResourceAsStream(resource)) {
            if (in == null) throw new IOException("Bundled font " + resource + " not found on classpath");
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return path;
    }
}
