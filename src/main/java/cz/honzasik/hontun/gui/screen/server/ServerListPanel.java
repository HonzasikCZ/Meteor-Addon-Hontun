package cz.honzasik.hontun.gui.screen.server;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.mixin.client.multiplayer.JoinMultiplayerScreenAccessor;
import cz.honzasik.hontun.mixin.client.multiplayer.OnlineServerEntryAccessor;
import cz.honzasik.hontun.serverlog.AddressInfo;
import cz.honzasik.hontun.serverlog.LogModel;
import cz.honzasik.hontun.serverlog.ServerLog;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.OpenPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ServerListPanel extends AbstractWidget {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/ServerListPanel");
    private static final int GAP = 12, PAD = 12, MIN_W = 240, MIN_VIEW = 48, LOGO = 32, TAB_H = 20;
    private static final int APPEAR_MS = 160;
    private static final Identifier UNKNOWN_ICON = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");

    private record TabBox(int tab, int x, int w, Component label, Component count, boolean enabled) {}

    private final JoinMultiplayerScreen screen;
    private final List<TabBox> tabs = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final List<Doc.Btn> specs = new ArrayList<>();

    ServerData data;
    LogModel.View log = LogModel.View.empty("");
    AddressInfo.View addr;
    int selServer;
    boolean changesOnly;
    boolean wide;
    int viewH;

    private Identifier icon = UNKNOWN_ICON;
    private Doc doc = new Doc();
    private Object[] key;
    private int tab = PanelPages.OVERVIEW;
    private int ix, iw, headY, tabsY, bodyY, bodyB;
    private int addrX, addrR, addrY;
    private double scroll;
    private long builtAt, shownAt;
    private int flashX, flashY;
    private boolean flashing;
    private boolean shown, pointer, failed;
    private int scissors, poses;

    public ServerListPanel(JoinMultiplayerScreen screen) {
        super(0, 0, 0, 0, Component.literal("Server info"));
        this.screen = screen;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        pointer = false;
        scissors = poses = 0;
        try {
            if (!prepare()) {
                hide();
                return;
            }
            if (!shown) shownAt = Util.getMillis();
            shown = true;
            draw(g, mx, my, delta);
            handleCursor(g);
        } catch (Throwable t) {
            while (scissors-- > 0) g.disableScissor();
            while (poses-- > 0) g.pose().popMatrix();
            hide();
            if (!failed) LOG.warn("Could not draw the server info panel", t);
            failed = true;
        }
    }

    private void hide() {
        shown = false;
        key = null;
        setWidth(0);
        setHeight(0);
    }

    private boolean prepare() {
        ServerSelectionList list = ((JoinMultiplayerScreenAccessor) screen).hontun$serverList();
        if (list == null || !(list.getSelected() instanceof ServerSelectionList.OnlineServerEntry entry)) return false;
        ServerData s = entry.getServerData();
        if (s == null || s.ip == null || s.ip.isBlank()) return false;
        AbstractScrollArea area = list;
        int x = list.getRowRight() + GAP;
        if (Paint.term()) x = list.getRowRight() + 6 + Math.max(TerminalWidgets.markerSpace() + 3, area.scrollBarX() + area.scrollbarWidth() + 2 - list.getRowRight());
        else if (area.scrollable()) x = Math.max(x, area.scrollBarX() + area.scrollbarWidth() + 6);
        int y = list.getY() + 8, w = screen.width - GAP - x, h = list.getBottom() - 8 - y;
        if (w < MIN_W) return false;
        headY = y + PanelPaint.headTop();
        tabsY = headY + 50;
        bodyY = headY + 80;
        bodyB = y + h - PAD;
        viewH = bodyB - bodyY;
        if (viewH < MIN_VIEW) return false;
        setX(x);
        setY(y);
        setWidth(w);
        setHeight(h);
        ix = x + PAD;
        iw = w - 2 * PAD;
        wide = iw >= 400;

        if (data == null || !Objects.equals(data.ip, s.ip) || !Objects.equals(data.name, s.name)) {
            scroll = 0;
            selServer = 0;
            changesOnly = false;
        }
        data = s;
        log = ServerLog.forAddress(s.ip).view();
        addr = AddressInfo.get(s.ip);
        FaviconTexture fav = ((OnlineServerEntryAccessor) (Object) entry).hontun$icon();
        icon = fav != null ? fav.textureLocation() : UNKNOWN_ICON;

        Object[] now = {tab, selServer, changesOnly, log, addr, s.state(), s.motd, s.ping, s.protocol, s.players, s.version,
                HontunTheme.mode(), HontunTheme.version(), iw, viewH, s.ip};
        if (key == null || !Arrays.equals(key, now) || Util.getMillis() - builtAt > 1000) {
            key = now;
            rebuild();
        }
        return true;
    }

    private int shownTab() {
        return enabled(tab) ? tab : PanelPages.OVERVIEW;
    }

    private boolean enabled(int t) {
        if (t == PanelPages.SERVERS) return !log.servers().isEmpty();
        return t != PanelPages.HISTORY || log.visited();
    }

    private void rebuild() {
        doc = PanelPages.build(this, shownTab(), iw);
        clampScroll();
        buttons.clear();
        specs.clear();
        for (Doc.Btn spec : doc.buttons) {
            Button b = Button.builder(Component.literal(spec.label()), q -> {
                if (spec.act() != null) spec.act().run();
            }).bounds(0, 0, spec.width(), btnH()).build();
            buttons.add(b);
            specs.add(spec);
        }
        layoutTabs();
        builtAt = Util.getMillis();
    }

    private void layoutTabs() {
        tabs.clear();
        int gap = 3, cg = Paint.term() ? 6 : 4;
        String[] counts = {null, null, log.servers().isEmpty() ? null : String.valueOf(log.servers().size()),
                log.visits().isEmpty() ? null : String.valueOf(log.visits().size())};
        boolean withCounts = true;
        int pad = wide ? 10 : 7, sum = 0;
        for (int pass = 0; pass < 2; pass++) {
            sum = 0;
            for (int i = 0; i < PanelPages.TABS.length; i++) sum += tabText(i, withCounts ? counts[i] : null, cg);
            int room = (iw - sum - gap * (PanelPages.TABS.length - 1)) / (2 * PanelPages.TABS.length);
            pad = Math.min(wide ? 10 : 7, room);
            if (pad >= 2) break;
            withCounts = false;
            cg = 0;
        }
        pad = Math.max(1, pad);
        int x = ix;
        for (int i = 0; i < PanelPages.TABS.length; i++) {
            String n = withCounts ? counts[i] : null;
            int w = tabText(i, n, cg) + 2 * pad;
            tabs.add(new TabBox(i, x, w, Paint.c(PanelPages.TABS[i]), n == null ? null : Paint.c(n), enabled(i)));
            x += w + gap;
        }
    }

    private static int tabText(int i, String count, int gap) {
        return Paint.w(Paint.c(PanelPages.TABS[i])) + (count == null ? 0 : gap + Paint.w(Paint.c(count)));
    }

    private static int btnH() {
        return HontunTheme.hvanilla() || Paint.vanilla() ? 20 : 16;
    }

    private int maxScroll() { return Math.max(0, doc.height - viewH); }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
    }

    private static float easeOut(float t) {
        float u = 1 - Math.max(0f, Math.min(1f, t));
        return 1 - u * u * u;
    }

    private void draw(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        float p = easeOut((Util.getMillis() - shownAt) / (float) APPEAR_MS);
        boolean anim = p < 1;
        int hx = anim ? -1 : mx, hy = anim ? -1 : my;
        if (anim) {
            float cx = x + w / 2f, cy = y + h / 2f, s = 0.97f + 0.03f * p;
            g.pose().pushMatrix();
            poses++;
            g.pose().translate(cx, cy);
            g.pose().scale(s, s);
            g.pose().translate(-cx, -cy);
        }
        PanelPaint.frame(g, x, y, w, h);
        drawHeader(g, hx, hy);
        drawTabs(g, hx, hy);
        drawBody(g, hx, hy, delta);
        if (anim) {
            PanelPaint.veil(g, x, y, w, h, 1 - p);
            g.pose().popMatrix();
            poses--;
            return;
        }
        drawOverlay(g, mx, my);
    }

    private void drawHeader(GuiGraphicsExtractor g, int mx, int my) {
        g.blit(RenderPipelines.GUI_TEXTURED, icon, ix, headY, 0f, 0f, LOGO, LOGO, 64, 64, 64, 64);
        boolean on = PanelPages.online(data), down = PanelPages.down(data);
        Component label = Paint.c(on ? "Online" : down ? "Offline" : "Pinging");
        int color = on ? Paint.green() : down ? Paint.red() : Paint.yellow();
        int si = on ? Paint.CHECK : down ? Paint.CLOSE : Paint.REFRESH;
        int sy = wide ? headY + 2 : headY + 35, right = ix + iw, sx = right;
        if (on && data.ping >= 0) {
            Component ms = Paint.c(data.ping + " ms");
            sx -= Paint.w(ms);
            Paint.text(g, ms, sx, sy, Paint.sub());
            sx -= 6 + 9;
            Paint.pingBars(g, sx, sy + 1, Paint.pingLit((int) Math.min(data.ping, 100000)));
            sx -= 6;
        }
        sx -= Paint.pillWidth(label, Paint.NORMAL, si);
        PanelPaint.status(g, label, sx, sy, color, si);

        int tx = ix + LOGO + 8, limit = (wide ? sx - 8 : right) - tx;
        Component name = Paint.c(data.name == null || data.name.isBlank() ? data.ip : data.name);
        if (Paint.bigWidth(name) <= limit) Paint.big(g, name, tx, headY - 1, Paint.text());
        else Paint.text(g, Paint.clip(name, limit), tx, headY + 3, Paint.text());

        int iconW = Paint.iconW(Paint.COPY), gap = iconW > 0 ? 5 : 0, ty = headY + 21;
        Component ac = Paint.clip(Paint.c(data.ip), Math.max(20, right - tx - iconW - gap));
        int aw = Paint.w(ac);
        addrX = tx;
        addrR = tx + aw + gap + iconW;
        addrY = ty;
        boolean hover = overAddress(mx, my);
        float f = Doc.flash(addressKey());
        int base = hover ? Paint.acc() : Paint.sub(), col = f >= 0 ? Doc.flashColor(f, base) : base;
        Paint.text(g, ac, tx, ty, col);
        if (hover) g.fill(tx, ty + 9, tx + aw, ty + 10, col);
        Paint.icon(g, f >= 0 && f < 0.75f ? Paint.CHECK : Paint.COPY, tx + aw + gap, ty, f >= 0 || hover ? col : Paint.dim());
        pointer |= hover;
    }

    private String addressKey() { return "panel-ip:" + data.ip; }

    private boolean overAddress(double mx, double my) {
        return mx >= addrX && mx < addrR && my >= addrY - 2 && my < addrY + 11;
    }

    private boolean overTab(TabBox t, double mx, double my) {
        return t.enabled && mx >= t.x && mx < t.x + t.w && my >= tabsY && my < tabsY + TAB_H;
    }

    private void drawTabs(GuiGraphicsExtractor g, int mx, int my) {
        int on = shownTab(), cg = Paint.term() ? 6 : 4;
        for (TabBox t : tabs) {
            boolean hover = overTab(t, mx, my);
            PanelPaint.tab(g, t.x, tabsY, t.w, TAB_H, t.label, t.count, cg, t.tab == on, hover, t.enabled);
            pointer |= hover && t.tab != on;
        }
        Paint.divider(g, ix - 2, ix + iw + 2, tabsY + TAB_H);
    }

    private boolean inBody(double mx, double my) {
        return mx >= ix - 4 && mx < ix + iw + 4 && my >= bodyY && my < bodyB;
    }

    private void drawBody(GuiGraphicsExtractor g, int mx, int my, float delta) {
        boolean in = inBody(mx, my);
        int hx = in ? mx : -1, hy = in ? my : -1, oy = bodyY - (int) scroll;
        g.enableScissor(ix - 4, bodyY, ix + iw + 4, bodyB);
        scissors++;
        for (Doc.Item it : doc.all()) {
            if (oy + it.y + it.h < bodyY || oy + it.y > bodyB) continue;
            it.draw(g, ix, oy, hx, hy);
        }
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            Doc.Btn spec = specs.get(i);
            Doc.Item a = spec.anchor();
            b.setX(ix + a.x + a.w - spec.rightInset() - spec.width());
            b.setY(oy + a.y + (a.h - b.getHeight()) / 2);
            b.visible = b.getY() + b.getHeight() > bodyY && b.getY() < bodyB;
            if (!b.visible) continue;
            b.extractRenderState(g, hx, hy, delta);
            if (HontunTheme.modern2() && spec.icon() >= 0 && spec.width() > 30) {
                Paint.icon(g, spec.icon(), b.getX() + 7, b.getY() + 4, b.isHovered() ? Paint.acc() : Paint.sub());
            }
        }
        g.disableScissor();
        scissors--;
        if (doc.height > viewH) {
            int th = Math.min(viewH, Math.max(20, viewH * viewH / doc.height));
            int tt = bodyY + (int) ((viewH - th) * (scroll / maxScroll()));
            Paint.scrollbar(g, ix + iw + (PAD - Paint.scrollbarW()) / 2, bodyY, bodyB, tt, th);
        }
        if (!in) return;
        Doc.Span sp = doc.spanAt(ix, oy, hx, hy);
        Doc.Line l = doc.lineAt(ix, oy, hx, hy);
        pointer |= sp != null && sp.act != null || l != null && l.act != null;
    }

    private void drawOverlay(GuiGraphicsExtractor g, int mx, int my) {
        if (inBody(mx, my)) {
            int oy = bodyY - (int) scroll;
            Doc.Span sp = doc.spanAt(ix, oy, mx, my);
            Doc.Tip tip = sp != null ? sp.tooltip() : null;
            if (tip == null) {
                Doc.Line l = doc.lineAt(ix, oy, mx, my);
                if (l != null) tip = l.tip;
            }
            boolean busy = sp != null && Doc.flash(sp.key) >= 0 && Util.getMillis() - Doc.flashAt < 900;
            if (tip != null && !busy) {
                g.nextStratum();
                Paint.tooltip(g, tip, mx, my, 4, 4, screen.width - 4, screen.height - 4);
            }
        }
        if (!flashing) return;
        long t = Util.getMillis() - Doc.flashAt;
        if (Doc.flashKey == null || t < 0 || t >= 900) {
            flashing = false;
            return;
        }
        float f = t / 900f, rise = easeOut(Math.min(1f, t / 260f));
        float a = Math.min(1f, t / 90f) * (f < 0.6f ? 1f : 1f - (f - 0.6f) / 0.4f);
        g.nextStratum();
        Paint.toast(g, Paint.sm("Copied"), flashX, flashY - 20 - Math.round(6 * rise), a);
    }

    void copy(String s) {
        Minecraft.getInstance().keyboardHandler.setClipboard(s);
    }

    void selectServer(int index) {
        if (selServer != index) click();
        selServer = index;
    }

    void showChanges(boolean only) {
        if (changesOnly == only) return;
        changesOnly = only;
        scroll = 0;
        click();
    }

    void openLogFolder() {
        Path dir = ServerLog.dir();
        if (dir == null) return;
        ServerLog.openDir(() -> Minecraft.getInstance().execute(() -> {
            try {
                OpenPath.open(dir);
            } catch (Throwable t) {
                LOG.warn("Could not open the log folder", t);
            }
        }));
    }

    private static void click() {
        playButtonClickSound(Minecraft.getInstance().getSoundManager());
    }

    private void flash(String k, double mx, double my) {
        Doc.flashKey = k;
        Doc.flashAt = Util.getMillis();
        flashX = (int) mx;
        flashY = (int) my;
        flashing = true;
        click();
    }

    @Override
    public boolean isMouseOver(double mx, double my) {
        return shown && mx >= getX() && mx < getRight() && my >= getY() && my < getBottom();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (!isMouseOver(e.x(), e.y())) return false;
        if (e.button() != InputConstants.MOUSE_BUTTON_LEFT) return true;
        try {
            press(e, doubleClick);
        } catch (Throwable t) {
            LOG.warn("Could not handle a click on the server info panel", t);
        }
        return true;
    }

    private void press(MouseButtonEvent e, boolean doubleClick) {
        double mx = e.x(), my = e.y();
        if (overAddress(mx, my)) {
            copy(data.ip);
            flash(addressKey(), mx, addrY - 3);
            return;
        }
        for (TabBox t : tabs) {
            if (!overTab(t, mx, my)) continue;
            if (t.tab != shownTab()) {
                scroll = 0;
                click();
            }
            tab = t.tab;
            return;
        }
        if (!inBody(mx, my)) return;
        for (Button b : buttons) {
            if (b.visible && b.isMouseOver(mx, my)) {
                b.mouseClicked(e, doubleClick);
                return;
            }
        }
        int oy = bodyY - (int) scroll, px = (int) mx, py = (int) my;
        Doc.Span sp = doc.spanAt(ix, oy, px, py);
        if (sp != null && sp.act != null) {
            sp.act.run();
            if (sp.key != null) flash(sp.key, mx, my);
            return;
        }
        Doc.Line l = doc.lineAt(ix, oy, px, py);
        if (l != null && l.act != null) l.act.run();
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!isMouseOver(mx, my)) return false;
        scroll -= sy * 20;
        clampScroll();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        return false;
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false;
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void handleCursor(GuiGraphicsExtractor g) {
        if (shown && pointer) super.handleCursor(g);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
    }
}
