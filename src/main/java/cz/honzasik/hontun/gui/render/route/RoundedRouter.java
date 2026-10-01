package cz.honzasik.hontun.gui.render.route;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class RoundedRouter implements PrimitiveRouter {
    @Override
    public boolean quad4(GuiRenderer r, double x, double y, double w, double h, Color tl, Color tr, Color br, Color bl) {
        if (tl.a == 0 && tr.a == 0 && br.a == 0 && bl.a == 0) return true;
        if (w == 0 || h == 0) return true;

        if (w < 0) {
            x += w;
            w = -w;
            Color t = tl;
            tl = tr;
            tr = t;
            t = bl;
            bl = br;
            br = t;
        }

        if (h < 0) {
            y += h;
            h = -h;
            Color t = tl;
            tl = bl;
            bl = t;
            t = tr;
            tr = br;
            br = t;
        }

        RoundedRect.get().pos(x, y).size(w, h).gradient(tl, tr, br, bl).render();
        return true;
    }
}
