package cz.honzasik.hontun.utils;

import com.mojang.blaze3d.Blaze3D;

import java.nio.file.Path;

public final class OpenPath {
    private OpenPath() {}

    public static void open(Path p) {
        Blaze3D.openPath(p);
    }
}
