package cz.honzasik.hontun.utils;

import net.minecraft.util.Util;

import java.nio.file.Path;

public final class OpenPath {
    private OpenPath() {}

    public static void open(Path p) {
        Util.getPlatform().openPath(p);
    }
}
