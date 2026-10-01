package cz.honzasik.hontun.gui.render.route;

import cz.honzasik.hontun.gui.theme.style.Pipeline;

public final class Routers {
    public static final PrimitiveRouter LEGACY = new LegacyRouter();
    public static final PrimitiveRouter ROUNDED = new RoundedRouter();
    public static final PrimitiveRouter QUADS = new QuadRouter();
    public static final PrimitiveRouter PIXEL = new PixelRouter();

    private Routers() {}

    public static PrimitiveRouter of(Pipeline pipeline) {
        if (pipeline == null) return LEGACY;
        return switch (pipeline) {
            case ROUNDED -> ROUNDED;
            case QUADS -> QUADS;
            case PIXEL -> PIXEL;
            default -> LEGACY;
        };
    }
}
