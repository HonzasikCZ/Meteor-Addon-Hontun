package cz.honzasik.hontun.gui.screen.server;

import com.mojang.blaze3d.platform.NativeImage;
import cz.honzasik.hontun.gui.screen.HontunScreen;
import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunTextBox;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.utils.HontunServerCard;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.OpenPath;
import meteordevelopment.meteorclient.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.InputConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ServerInfoScreen extends HontunScreen {
    public enum Page {
        OVERVIEW("Overview"), SOFTWARE("Software"), ANTICHEAT("Anticheat"), VERSION("Version"),
        PLUGINS("Plugins"), PLAYERS("Players"), WORLD("World"),
        CONNECTION("Connection"), CHANNELS("Channels"), COOKIES("Cookies");

        final String label;

        Page(String label) { this.label = label; }
    }

    private static final Page[][] GROUPS = {
        {Page.OVERVIEW},
        {Page.SOFTWARE, Page.ANTICHEAT, Page.VERSION},
        {Page.PLUGINS, Page.PLAYERS, Page.WORLD},
        {Page.CONNECTION, Page.CHANNELS, Page.COOKIES}
    };
    private static final String[] GROUP_NAMES = {null, "Detection", "Server", "Network"};
    private static final Identifier UNKNOWN_ICON = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");
    private static final int CARD_W = 305, CARD_H = 36, LOGO = 32;

    static String perms = "unknown";

    Page page;
    String query = "";
    String channel;
    int evTab = 0;
    boolean evBuiltin = true, evConfig = true;
    final Map<Page, String> counts = new EnumMap<>(Page.class);

    private boolean viewer;
    private int pathW;
    private Doc doc = new Doc();
    private double scroll;
    private int ax, ay, aw, ah, sbW, hx, vx, vy, vw, vh, cardY, logoX, logoY;
    private final List<int[]> nav = new ArrayList<>();
    private final List<Button> pageButtons = new ArrayList<>();
    private final List<Doc.Btn> pageSpecs = new ArrayList<>();
    private HontunTextBox search;
    private Button copyButton, saveButton;
    private int addrX = -1, addrR, addrY;
    private long copiedAt, builtAt, doneAt, savedAt, viewerAt, closeAt;
    private int flashX, flashY;
    private String doneLabel;
    private FaviconTexture favicon;
    private boolean faviconTried;
    private String savedPath;

    public ServerInfoScreen(Page page) {
        super(Component.literal("Server"), null);
        this.page = page;
    }

    public static void open(Page page, String permsText) {
        if (permsText != null) perms = permsText;
        Utils.screenToOpen = new ServerInfoScreen(page);
    }

    ServerData server() {
        return minecraft.getCurrentServer();
    }

    void setPage(Page p) {
        page = p;
        scroll = 0;
        viewer = false;
        rebuildWidgets();
    }

    void copy(String s) {
        minecraft.keyboardHandler.setClipboard(s);
    }

    private void flash(String key, double mx, double my) {
        Doc.flashKey = key;
        Doc.flashAt = Util.getMillis();
        flashX = (int) mx;
        flashY = (int) my;
        click();
    }

    private void click() {
        AbstractWidget.playButtonClickSound(minecraft.getSoundManager());
    }

    private static String doneText(String label) {
        return switch (label) {
            case "Copy" -> "Copied";
            case "Save to file" -> "Saved";
            default -> null;
        };
    }

    private boolean done(Doc.Btn spec) {
        return spec.label().equals(doneLabel) && Util.getMillis() - doneAt < 1500;
    }

    private static float easeOut(float t) {
        float u = 1 - Math.max(0f, Math.min(1f, t));
        return 1 - u * u * u;
    }

    private float viewerProgress() {
        long now = Util.getMillis();
        if (viewer) return easeOut((now - viewerAt) / 220f);
        float t = (now - closeAt) / 150f;
        return t < 1 ? 1 - t * t : 0;
    }

    private void openViewer() {
        viewer = true;
        viewerAt = Util.getMillis();
        click();
        rebuildWidgets();
    }

    private void closeViewer() {
        viewer = false;
        closeAt = Util.getMillis();
        rebuildWidgets();
    }

    @Override
    protected void init() {
        super.init();
        if (!Paint.vanilla()) return;
        for (var c : children()) {
            if (c instanceof Button b && "Back".equals(b.getMessage().getString())) b.setMessage(CommonComponents.GUI_DONE);
        }
    }

    @Override
    protected void buildContent() {
        int t = contentTop(), bot = contentBottom(), band = bandBottom();
        if (Paint.term()) {
            aw = Math.min(width - 40, 960);
            ax = (width - aw) / 2;
            ay = t;
            ah = bot - t;
        } else if (Paint.vanilla()) {
            aw = Math.min(width - 40, 862);
            ax = (width - aw) / 2;
            ay = t;
            ah = band - t;
        } else if (HontunTheme.hvanilla()) {
            aw = Math.min(width - 40, 880);
            ax = (width - aw) / 2;
            ay = t + 4;
            ah = bot - ay;
        } else {
            ax = contentLeft();
            aw = contentWidth();
            ay = t + 4;
            ah = bot - ay;
        }
        sbW = ServerPages.t6(aw >= 600 ? 150 : 100, 128, 156, 140, 150, 150);
        hx = ax + ServerPages.t6(sbW + 12, 142, 162, 154, 166, 160);
        vx = Paint.term() ? ax + 166 : hx;
        vw = ServerPages.t6(ax + aw - vx, ax + aw - vx, ax + aw - vx, aw - 168, aw - 182, aw - 178);
        vy = ServerPages.t6(t + 52, t + 44, t + 47, t + 50, t + 50, ay + 48);
        vh = Math.max(0, ServerPages.t6(bot, bot - 8, bot - 14, band - 14, band - 16, ay + ah - 12) - vy);
        cardY = ServerPages.t6(ay, ay, ay, t + 7, t + 7, ay + 4);
        logoX = ax + ServerPages.t6((sbW - LOGO) / 2, 48, 59, 54, 58, 59);
        logoY = ServerPages.t6(ay, ay, ay, t + 8, t + 8, ay + 6);

        nav.clear();
        int y = ServerPages.t6(t + 52, t + 52, t + 54, t + 58, t + 58, ay + 52);
        int groupStep = ServerPages.t6(20, 22, 22, 22, 20, 24), step = ServerPages.t6(22, 22, 22, 22, 20, 18);
        for (int gi = 0; gi < GROUPS.length; gi++) {
            if (GROUP_NAMES[gi] != null) y += groupStep;
            for (Page p : GROUPS[gi]) {
                nav.add(new int[]{p.ordinal(), y});
                y += step;
            }
        }

        int cbx = ax + ServerPages.t6(0, 0, 0, 8, 8, 8);
        int cby = ServerPages.t6(bot - 20, bot - 16, bot - 16, band - 34, band - 26, ay + ah - 22);
        int cbw = ServerPages.t6(sbW, 120, 150, 124, 132, 134), cbh = ServerPages.t6(20, 16, 16, 20, 20, 16);
        copyButton = addRenderableWidget(Button.builder(Component.literal("Copy as text"), b -> copyAll())
                .bounds(cbx, cby, cbw, cbh).build());

        search = null;

        saveButton = null;
        if (viewer) {
            int[] r = viewerRect();
            saveButton = addRenderableWidget(Button.builder(Component.literal("Save PNG"), b -> saveIcon())
                    .bounds(r[0] + 10, r[1] + r[3] - 24, 70, 16).build());
        }

        pageButtons.clear();
        rebuild();
    }

    void rebuild() {
        for (Button b : pageButtons) removeWidget(b);
        pageButtons.clear();
        pageSpecs.clear();
        doc = buildDoc(vw);
        if (HontunTheme.smog() && doc.height > vh) doc = buildDoc(vw - 9);
        clampScroll();
        for (Doc.Btn spec : doc.buttons) {
            Button b = Button.builder(Component.literal(spec.label()), x -> {
                        if (spec.act() == null) return;
                        spec.act().run();
                        if (doneText(spec.label()) != null) {
                            doneLabel = spec.label();
                            doneAt = Util.getMillis();
                        }
                    })
                    .bounds(0, 0, spec.width(), btnH()).build();
            b.active = spec.act() != null;
            pageButtons.add(addRenderableWidget(b));
            pageSpecs.add(spec);
        }
        syncSearch();
        builtAt = Util.getMillis();
    }

    private Doc buildDoc(int w) {
        counts.clear();
        Doc d;
        try {
            d = ServerPages.build(this, w);
        } catch (Throwable t) {
            d = new Doc();
            d.add(new Doc.Para(Paint.c("Could not build this page: " + t), Paint.red(), 0));
        }
        d.layout(w, 16);
        return d;
    }

    private int clipPad() {
        return HontunTheme.smog() ? 4 : Paint.pad();
    }

    private boolean inDoc(double mx, double my) {
        int cp = clipPad();
        return mx >= vx - cp && mx < vx + vw + cp && my >= vy && my < vy + vh;
    }

    private int navX() {
        return ax + ServerPages.t6(0, 0, 0, 8, 6, 14);
    }

    private int navW() {
        return ServerPages.t6(sbW, 120, 150, 124, 136, 122);
    }

    private static int navH() {
        return Paint.term() ? 18 : 20;
    }

    private boolean navHit(double mx, double my, int y) {
        int x = navX();
        return mx >= x && mx < x + navW() && my >= y && my < y + navH();
    }

    private int scrimTop() {
        return HontunTheme.modern2() ? vy + 1 : vy;
    }

    private void syncSearch() {
        Doc.Item a = doc.searchAnchor;
        if (a == null || doc.searchW <= 0 || doc.searchH <= 0) {
            if (search != null) removeWidget(search);
            search = null;
            return;
        }
        if (search != null && search.boxW() == doc.searchW && search.boxH() == doc.searchH) return;
        boolean focused = search != null && search.isFocused();
        if (search != null) removeWidget(search);
        search = HontunTextBox.search(font, vx + doc.searchX, vy + a.y, doc.searchW, doc.searchH, "Search players");
        search.setHint(searchHint());
        search.setValue(query);
        search.setResponder(s -> {
            if (s.equals(query)) return;
            query = s;
            scroll = 0;
            rebuild();
        });
        addRenderableWidget(search);
        if (focused) setFocused(search);
    }

    private static Component searchHint() {
        Component c = Paint.c("Search players");
        if (Paint.vanilla() || HontunTheme.hvanilla()) return c.copy().withStyle(EditBox.SEARCH_HINT_STYLE);
        return c.copy().withColor(Paint.dim() & 0xFFFFFF);
    }

    void clearSearch() {
        query = "";
        scroll = 0;
        if (search != null) search.setValue("");
        rebuild();
    }

    private void placeSearch() {
        if (search == null) return;
        Doc.Item a = doc.searchAnchor;
        if (a == null) {
            search.visible = false;
            return;
        }
        int x = vx + doc.searchX, y = vy - (int) scroll + a.y;
        search.setX(x + search.getX() - search.boxX());
        search.setY(y + search.getY() - search.boxY());
        search.visible = !viewer && y >= vy && y + search.boxH() <= vy + vh;
    }

    private static int btnH() {
        return HontunTheme.hvanilla() || Paint.vanilla() ? 20 : 16;
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, Math.max(0, doc.height - vh)));
    }

    private void copyAll() {
        ServerData s = server();
        String head = "Server: " + (s != null ? s.ip : "singleplayer") + "\n" + page.label + "\n";
        copy(head + doc.copy());
        copiedAt = Util.getMillis();
    }

    private int[] viewerRect() {
        int w = 276, h = 312, top = scrimTop();
        int cx = vx + vw / 2, cy = (top + ay + ah) / 2;
        return new int[]{cx - w / 2, Math.max(top, cy - h / 2), w, h};
    }

    private Path iconPath() {
        ServerData s = server();
        String name = (s != null ? s.ip : "server").replaceAll("[^A-Za-z0-9._-]", "_") + ".png";
        return FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("icons").resolve(name);
    }

    private void saveIcon() {
        ServerData s = server();
        byte[] bytes = s != null ? s.getIconBytes() : null;
        if (bytes == null) return;
        try {
            Path p = iconPath();
            Files.createDirectories(p.getParent());
            Files.write(p, bytes);
            savedPath = p.toString();
            savedAt = Util.getMillis();
        } catch (Throwable ignored) {
        }
    }

    private void openIconFolder() {
        try {
            Path p = iconPath().getParent();
            Files.createDirectories(p);
            OpenPath.open(p);
        } catch (Throwable ignored) {
        }
    }

    void saveChannel(String channel, byte[] data, boolean json) {
        if (channel == null || data == null) return;
        try {
            Path dir = FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("channels");
            Files.createDirectories(dir);
            Files.write(dir.resolve(channel.replaceAll("[^A-Za-z0-9._-]", "_") + (json ? ".json" : ".bin")), data);
        } catch (Throwable ignored) {
        }
    }

    void openFolder(Path p) {
        try {
            OpenPath.open(p);
        } catch (Throwable ignored) {
        }
    }

    private Identifier iconTexture() {
        ServerData s = server();
        if (!faviconTried && s != null) {
            faviconTried = true;
            byte[] bytes = s.getIconBytes();
            if (bytes != null) {
                try {
                    favicon = FaviconTexture.forServer(minecraft.getTextureManager(), s.ip);
                    favicon.upload(NativeImage.read(bytes));
                } catch (Throwable t) {
                    favicon = null;
                }
            }
        }
        return favicon != null ? favicon.textureLocation() : UNKNOWN_ICON;
    }

    private static Identifier pingIcon(ServerData s) {
        if (s == null || s.ping < 0) return Identifier.withDefaultNamespace("server_list/unreachable");
        long p = s.ping;
        int n = p < 150 ? 5 : p < 300 ? 4 : p < 600 ? 3 : p < 1000 ? 2 : 1;
        return Identifier.withDefaultNamespace("server_list/ping_" + n);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractBackground(g, mx, my, delta);

        if (Paint.vanilla()) {
            HontunCards.vanillaListBackground(g, 0, contentTop(), width, bandBottom() - contentTop(), 0);
            HontunCards.vanillaSeparators(g, 0, contentTop(), bandBottom(), width);
            g.text(font, getTitle(), width / 2 - font.width(getTitle()) / 2, 12, 0xFFFFFFFF, true);
        }
        if (Paint.term()) {
            TerminalWidgets.panel(g, ax, ay, 150, ah);
            TerminalWidgets.panel(g, ax + 154, ay, aw - 154, 44);
            TerminalWidgets.panel(g, ax + 154, ay + 48, aw - 154, ah - 48);
            new TerminalWidgets.Px().hline(ax, ax + 150, ay + 43, TerminalWidgets.line()).submit(g);
        }
        drawSidebar(g, mx, my);
        drawRules(g);
        drawHeader(g, mx, my);

        int cp = clipPad();
        g.enableScissor(vx - cp, vy, vx + vw + cp, vy + vh);
        int oy = vy - (int) scroll;
        for (Doc.Item it : doc.all()) {
            if (oy + it.y + it.h < vy || oy + it.y > vy + vh) continue;
            it.draw(g, vx, oy, viewer ? -1 : mx, viewer ? -1 : my);
        }
        g.disableScissor();

        if (vh > 0 && doc.height > vh) {
            int th = Math.min(vh, Math.max(32, vh * vh / doc.height));
            int tt = vy + (int) ((vh - th) * (scroll / (doc.height - vh)));
            int sx = HontunTheme.smog() ? vx + vw - 7 : vx + vw + (Paint.pad() - Paint.scrollbarW()) / 2;
            Paint.scrollbar(g, sx, vy, vy + vh, tt, th);
        }

        float vp = viewerProgress();
        if (vp > 0) {
            boolean live = viewer && vp >= 1;
            drawViewer(g, live ? mx : -1, live ? my : -1, vp);
        }
    }

    private void drawRules(GuiGraphicsExtractor g) {
        int t = contentTop();
        if (Paint.term()) return;
        if (HontunTheme.smog()) {
            Paint.sep(g, ax, ay + CARD_H + 4, aw);
        } else if (HontunTheme.modern2()) {
            int top = ay + CARD_H + 5;
            Paint.vsep(g, ax + sbW, top, ay + ah - top);
            Paint.divider(g, ax, ax + aw - 10, ay + CARD_H + 4);
        } else if (HontunTheme.modern1()) {
            g.fill(ax, t + 46, ax + aw, t + 47, HontunTheme.argb(0x70, HontunTheme.overlay0()));
            Paint.vsep(g, ax + 156, t + 47, contentBottom() - (t + 47));
        } else if (Paint.vanilla()) {
            Paint.sep(g, ax, t + 48, aw);
            Paint.vsep(g, ax + 148, t, 48);
            Paint.vsep(g, ax + 148, t + 50, ah - 50);
        } else {
            Paint.sep(g, ax, t + 48, aw);
        }
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mx, int my) {
        int lx = logoX, ly = logoY;
        boolean lh = !viewer && mx >= lx - 4 && mx < lx + LOGO + 4 && my >= ly - 4 && my < ly + LOGO + 4;
        if (lh || viewer) Paint.row(g, lx - 4, ly - 4, LOGO + 8, LOGO + 8, viewer, lh);
        g.blit(RenderPipelines.GUI_TEXTURED, iconTexture(), lx, ly, 0f, 0f, LOGO, LOGO, 64, 64, 64, 64);

        boolean icons = !Paint.term() && !Paint.vanilla();
        boolean shift = HontunTheme.modern2() || HontunTheme.modern1();
        int nx = navX(), nw = navW(), nh = navH();
        int iconX = ax + ServerPages.t6(12, 10, 8, 14, 0, 0);
        int labelX = ax + ServerPages.t6(27, 24, 23, 28, 12, 18);
        int countR = ax + ServerPages.t6(sbW - 10, 112, 142, 126, 136, 132);
        int groupX = ax + ServerPages.t6(12, 10, 8, 16, 12, 18);
        int groupDy = ServerPages.t6(9, 13, 12, 11, 12, 13);
        int idx = 0;
        for (int gi = 0; gi < GROUPS.length; gi++) {
            if (GROUP_NAMES[gi] != null) {
                String name = Paint.term() ? "# " + GROUP_NAMES[gi] : GROUP_NAMES[gi];
                Paint.text(g, Paint.sm(name), groupX, nav.get(idx)[1] - groupDy, Paint.group());
            }
            for (int k = 0; k < GROUPS[gi].length; k++, idx++) {
                Page p = GROUPS[gi][k];
                int y = nav.get(idx)[1];
                boolean active = p == page;
                boolean hover = !viewer && navHit(mx, my, y);
                Paint.navRow(g, nx, y, nw, nh, active, hover);
                int dx = active && shift ? 3 : 0;
                int ty = y + (nh - 8) / 2;
                if (icons) Paint.icon(g, p.ordinal(), iconX + dx, ty, active ? Paint.acc() : Paint.dim());
                Paint.text(g, Paint.c(p.label), labelX + dx, ty, Paint.nav(active));
                String n = counts.get(p);
                if (n != null) {
                    Component c = Paint.sm(n);
                    Paint.text(g, c, countR - Paint.w(c), ty, active && !Paint.vanilla() ? Paint.acc() : Paint.dim());
                }
            }
        }
        if (copyButton != null) {
            copyButton.setMessage(Component.literal(Util.getMillis() - copiedAt < 1500 ? "Copied" : "Copy as text"));
        }
    }

    private void drawHeader(GuiGraphicsExtractor g, int mx, int my) {
        ServerData s = server();
        int cy = cardY;
        if (s == null) {
            Paint.text(g, Paint.c("Singleplayer"), hx, cy + 4, Paint.text());
            return;
        }
        Identifier icon = iconTexture();
        if (HontunTheme.terminal()) {
            HontunServerCard.background(g, hx, cy + 1, CARD_W, CARD_H - 2, false, false);
            HontunServerCard.terminalContent(g, s, icon, pingIcon(s), hx + 2, cy + 1, hx + CARD_W - 2, false);
        } else if (HontunTheme.modern2() || HontunTheme.smog()) {
            HontunServerCard.background(g, hx, cy + 1, CARD_W, CARD_H - 2, false, false);
            HontunServerCard.content(g, s, icon, pingIcon(s), hx + 2, cy + 1, hx + CARD_W - 2);
        } else {
            vanillaCard(g, s, icon, hx + 2, cy + 1, hx + CARD_W - 2);
        }

        var addr = Paint.c(s.ip);
        int aw2 = Paint.w(addr);
        int right = ax + aw - 10, ix = right - Paint.iconW(Paint.COPY), axr = ix - 5 - aw2, ty = cy + 14;
        addrX = -1;
        if (axr > hx + CARD_W + 8) {
            addrX = axr;
            addrR = right;
            addrY = ty;
            boolean h = !viewer && mx >= axr && mx < right && my >= ty - 3 && my < ty + 11;
            float f = Doc.flash("ip:" + s.ip);
            int base = h ? Paint.acc() : Paint.sub();
            int col = f >= 0 ? Doc.flashColor(f, base) : base;
            Paint.text(g, addr, axr, ty, col);
            if (h) g.fill(axr, ty + 9, axr + aw2, ty + 10, col);
            Paint.icon(g, f >= 0 && f < 0.75f ? Paint.CHECK : Paint.COPY, ix, ty, col);
        }
    }

    private void vanillaCard(GuiGraphicsExtractor g, ServerData s, Identifier icon, int x, int y, int right) {
        g.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, 32, 32, 64, 64, 64, 64);
        int statusX = right - 15;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, pingIcon(s), statusX, y, 10, 8);
        if (s.status != null) {
            statusX -= font.width(s.status) + 5;
            g.text(font, s.status, statusX, y + 1, 0xFF808080, true);
        }
        g.text(font, s.name, x + 35, y + 1, 0xFFFFFFFF, true);
        if (s.motd != null) {
            var lines = font.split(font.substrByWidth(s.motd, (right - 12 - x - 35) * 3), right - 12 - x - 35);
            for (int i = 0; i < Math.min(2, lines.size()); i++) g.text(font, lines.get(i), x + 35, y + 12 + 9 * i, 0xFF808080, true);
        }
    }

    private void drawViewer(GuiGraphicsExtractor g, int mx, int my, float p) {
        int cp = clipPad();
        g.fill(vx - cp, scrimTop(), vx + vw + cp, ay + ah, HontunTheme.argb(Math.round(0x90 * p), HontunTheme.crust()));
        int[] r = viewerRect();
        int x = r[0], y = r[1], w = r[2], h = r[3];
        boolean anim = p < 1;
        if (anim) {
            float pcx = x + w / 2f, pcy = y + h / 2f, lcx = logoX + LOGO / 2f, lcy = logoY + LOGO / 2f;
            float s0 = LOGO / 256f, sc = s0 + (1 - s0) * p;
            g.pose().pushMatrix();
            g.pose().translate(lcx + (pcx - lcx) * p, lcy + (pcy - lcy) * p);
            g.pose().scale(sc, sc);
            g.pose().translate(-pcx, -pcy);
        }
        drawViewerPanel(g, mx, my, x, y, w, h);
        if (anim) g.pose().popMatrix();
    }

    private void drawViewerPanel(GuiGraphicsExtractor g, int mx, int my, int x, int y, int w, int h) {
        Paint.well(g, x, y, w, h);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, HontunTheme.argb(0xF0, HontunTheme.base()));
        ServerData s = server();
        byte[] bytes = s != null ? s.getIconBytes() : null;
        String info = bytes != null ? "64 × 64 PNG, " + String.format(Locale.ROOT, "%.1f kB", bytes.length / 1024.0) : "No icon";
        Paint.text(g, Paint.c("Server icon"), x + 10, y + 8, Paint.text());
        Paint.text(g, Paint.c(info), x + 16 + Paint.w(Paint.c("Server icon")), y + 8, Paint.dim());
        int ix = x + (w - 256) / 2, iy = y + 22;
        for (int cy = 0; cy < 256; cy += 16) {
            for (int cx = 0; cx < 256; cx += 16) {
                boolean dark = ((cx + cy) / 16) % 2 == 0;
                g.fill(ix + cx, iy + cy, ix + cx + 16, iy + cy + 16, HontunTheme.argb(0xFF, dark ? HontunTheme.surface1() : HontunTheme.surface2()));
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, iconTexture(), ix, iy, 0f, 0f, 256, 256, 64, 64, 64, 64);
        String path = savedPath != null ? savedPath : iconPath().toString();
        int px0 = x + 88, py0 = y + h - 20;
        var pc = Paint.c(fitPath("hontun/icons/" + iconPath().getFileName(), x + w - 10 - px0));
        pathW = Paint.w(pc);
        boolean ph = mx >= px0 && mx < px0 + pathW && my >= py0 - 2 && my < py0 + 10;
        Paint.text(g, pc, px0, py0, Paint.acc());
        if (ph) {
            g.fill(px0, py0 + 9, px0 + Paint.w(pc), py0 + 10, Paint.acc());
            Paint.tooltip(g, Doc.Tip.of(List.of(Paint.c("Show in folder"), Paint.c(path))), mx, my, ax, ay, ax + aw, ay + ah);
        }
    }

    private static String fitPath(String s, int max) {
        if (Paint.w(Paint.c(s)) <= max) return s;
        String f = s.substring(s.lastIndexOf('/') + 1);
        if (Paint.w(Paint.c("…/" + f)) <= max) return "…/" + f;
        for (int cut = 1; cut < f.length(); cut++) {
            int a = (f.length() - cut) / 2;
            String t = f.substring(0, a) + "…" + f.substring(a + cut);
            if (Paint.w(Paint.c(t)) <= max) return t;
        }
        return "…";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        for (int i = 0; i < pageButtons.size(); i++) {
            Button b = pageButtons.get(i);
            Doc.Btn spec = pageSpecs.get(i);
            Doc.Item a = spec.anchor();
            int bh = b.getHeight();
            int bx = vx + a.x + a.w - spec.rightInset() - spec.width();
            int by = vy - (int) scroll + a.y + (a.h - bh) / 2;
            b.setX(bx);
            b.setY(by);
            b.visible = !viewer && by >= vy && by + bh <= vy + vh;
            String dt = doneText(spec.label());
            b.setMessage(Component.literal(dt != null && done(spec) ? dt : spec.label()));
        }
        if (saveButton != null) {
            saveButton.visible = viewer && viewerProgress() >= 1;
            saveButton.setMessage(Component.literal(Util.getMillis() - savedAt < 1500 ? "Saved" : "Save PNG"));
        }
        placeSearch();
        super.extractRenderState(g, mx, my, delta);
        if (HontunTheme.modern2()) {
            buttonIcons(g);
            copyIcon(g);
        }
        if (!viewer && inDoc(mx, my)) {
            int oy = vy - (int) scroll;
            Doc.Span sp = doc.spanAt(vx, oy, mx, my);
            Doc.Tip tip = sp != null ? sp.tooltip() : null;
            if (tip == null) {
                Doc.Line l = doc.lineAt(vx, oy, mx, my);
                if (l != null) tip = l.tip;
            }
            boolean flashing = sp != null && Doc.flash(sp.key) >= 0 && Util.getMillis() - Doc.flashAt < 900;
            if (tip != null && !flashing) Paint.tooltip(g, tip, mx, my, ax, ay, ax + aw, ay + ah);
        }
        drawToast(g);
    }

    private void drawToast(GuiGraphicsExtractor g) {
        if (Doc.flashKey == null) return;
        long t = Util.getMillis() - Doc.flashAt;
        if (t < 0 || t >= 900) return;
        float f = t / 900f, rise = easeOut(Math.min(1f, t / 260f));
        float a = Math.min(1f, t / 90f) * (f < 0.6f ? 1f : 1f - (f - 0.6f) / 0.4f);
        g.nextStratum();
        Paint.toast(g, Paint.sm("Copied"), flashX, flashY - 20 - Math.round(6 * rise), a);
    }

    private void buttonIcons(GuiGraphicsExtractor g) {
        for (int i = 0; i < pageButtons.size(); i++) {
            Button b = pageButtons.get(i);
            Doc.Btn spec = pageSpecs.get(i);
            if (!b.visible || spec.icon() < 0) continue;
            int sub = Paint.sub();
            if (done(spec)) {
                Paint.icon(g, Paint.CHECK, b.getX() + 7, b.getY() + 4, Paint.green());
                continue;
            }
            int col = !b.active ? (sub & 0x00FFFFFF) | 0x60000000 : b.isHovered() ? Paint.acc() : sub;
            Paint.icon(g, spec.icon(), b.getX() + 7, b.getY() + 4, col);
        }
    }

    private void copyIcon(GuiGraphicsExtractor g) {
        if (copyButton == null || !copyButton.visible) return;
        int lx = copyButton.getX() + (copyButton.getWidth() - font.width(copyButton.getMessage())) / 2;
        boolean ok = Util.getMillis() - copiedAt < 1500;
        Paint.icon(g, ok ? Paint.CHECK : Paint.COPY, lx - 11, copyButton.getY() + 4, ok ? Paint.green() : Paint.sub());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        double mx = e.x(), my = e.y();
        if (viewer) {
            int[] r = viewerRect();
            if (saveButton != null && saveButton.isMouseOver(mx, my)) return super.mouseClicked(e, doubleClick);
            int px0 = r[0] + 88, py0 = r[1] + r[3] - 20;
            if (mx >= px0 && mx < px0 + pathW && my >= py0 - 2 && my < py0 + 10) {
                openIconFolder();
                return true;
            }
            if (mx < r[0] || mx >= r[0] + r[2] || my < r[1] || my >= r[1] + r[3]) closeViewer();
            return true;
        }
        if (super.mouseClicked(e, doubleClick)) return true;

        if (mx >= logoX - 4 && mx < logoX + LOGO + 4 && my >= logoY - 4 && my < logoY + LOGO + 4) {
            openViewer();
            return true;
        }
        for (int[] n : nav) {
            if (navHit(mx, my, n[1])) {
                setPage(Page.values()[n[0]]);
                return true;
            }
        }
        ServerData s = server();
        if (s != null && addrX >= 0 && mx >= addrX && mx < addrR && my >= addrY - 3 && my < addrY + 11) {
            copy(s.ip);
            flash("ip:" + s.ip, mx, addrY - 3);
            return true;
        }
        if (inDoc(mx, my)) {
            int oy = vy - (int) scroll, ix = (int) mx, iy = (int) my;
            Doc.Span sp = doc.spanAt(vx, oy, ix, iy);
            if (sp != null && sp.act != null) {
                sp.act.run();
                if (sp.key != null) flash(sp.key, mx, my);
                return true;
            }
            Doc.Line l = doc.lineAt(vx, oy, ix, iy);
            if (l != null && l.act != null) {
                l.act.run();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!viewer && inDoc(mx, my)) {
            scroll -= sy * 20;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        if (viewer && e.key() == InputConstants.KEY_ESCAPE) {
            closeViewer();
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    protected boolean opaqueBackdrop() {
        return minecraft.level == null;
    }

    @Override
    public void tick() {
        super.tick();
        if (Util.getMillis() - builtAt > 1000 && !viewer) rebuild();
    }

    @Override
    public void removed() {
        if (favicon != null) favicon.close();
        favicon = null;
        super.removed();
    }
}
