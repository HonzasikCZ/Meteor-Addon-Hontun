package cz.honzasik.hontun.gui;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public final class HontunGui {
    public static final Logger LOG = LogUtils.getLogger();

    public static final String MOD_ID = "hontun";

    private HontunGui() {}

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
