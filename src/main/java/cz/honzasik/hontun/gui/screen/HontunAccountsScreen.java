package cz.honzasik.hontun.gui.screen;

import com.mojang.authlib.GameProfile;
import cz.honzasik.hontun.gui.widget.AccountCardList;
import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.gui.widget.HontunTextBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountCache;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.MicrosoftLogin;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import meteordevelopment.meteorclient.systems.accounts.types.SessionAccount;
import meteordevelopment.meteorclient.systems.accounts.types.TheAlteningAccount;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HontunAccountsScreen extends HontunScreen {
    private int leftPane() { return Math.min(186, Math.max(150, contentWidth() / 4)); }

    private HontunTextBox valueField;
    private AccountCardList list;
    private double savedScroll;
    private String status = "";
    private String filter = "";

    private int modelCardX, modelCardY, modelCardW, modelCardH;
    private boolean loggedDot;
    private float loggedDotX, loggedDotY;
    private int addBoxX, addBoxY, addBoxW, addBoxH;

    private String valueText = "";

    public HontunAccountsScreen(Screen parent) {
        super(Component.literal("Accounts"), parent);
    }

    @Override
    protected void buildContent() {
        int top = contentTop();
        int bottom = contentBottom();
        int paneX = contentLeft();
        int lh = font.lineHeight;

        int captionH = (lh + 2) * 3 + 6;

        boolean roomy = (bottom - top) >= 210;
        int padIn = roomy ? 12 : 8;
        int gapField = roomy ? 12 : 6;
        int gapRows = roomy ? 8 : 4;
        int btnH = roomy ? 18 : 16;
        int capGap = roomy ? 16 : 10;

        int addBoxInner = padIn + 18 + gapField + btnH + gapRows + btnH + padIn;
        int fixed = captionH + capGap + addBoxInner;
        int modelH = Math.max(0, Math.min(176, (bottom - top) - fixed - 12));
        if (modelH < 40) modelH = 0;
        int modelGap = modelH > 0 ? 8 : 0;
        int modelW = Math.min(124, modelH * 2 / 3);

        int blockH = modelH + modelGap + fixed;
        int startY = top + Math.max(0, ((bottom - top) - blockH) / 2);

        int modelY = startY;
        int captionY = modelY + modelH + modelGap;
        int addBoxTop = captionY + captionH + capGap;
        int addLabelY = addBoxTop - 4;
        int fieldY = addBoxTop + padIn;
        int row1Y = fieldY + 18 + gapField;
        int row2Y = row1Y + btnH + gapRows;

        modelCardX = paneX;
        modelCardY = modelY - 6;
        modelCardW = leftPane();
        modelCardH = modelH + modelGap + 4 + captionH;
        addBoxX = paneX;
        addBoxY = addBoxTop;
        addBoxW = leftPane();
        addBoxH = (row2Y + btnH) - addBoxTop + padIn;

        try {
            GameProfile prof = minecraft.getGameProfile();
            if (prof != null && modelH >= 40) {
                PlayerSkinWidget model = new PlayerSkinWidget(modelW, modelH, minecraft.getEntityModels(),
                        minecraft.getSkinManager().createLookup(prof, false));
                model.setX(paneX + (leftPane() - modelW) / 2);
                model.setY(modelY);
                addRenderableWidget(model);
            }
        } catch (Throwable ignored) {
        }

        User u = minecraft.getUser();
        addCentered(u != null ? u.getName() : "-", paneX, leftPane(), captionY, HontunTheme.textLight());
        addCentered(currentTypeLabel(), paneX, leftPane(), captionY + lh + 2, HontunTheme.textDim());
        loggedDot = false;
        if (cz.honzasik.hontun.gui.widget.HontunRound.smoothDots()) {
            int ly = captionY + (lh + 2) * 2;
            Component lc = cz.honzasik.hontun.utils.HontunFont.apply(Component.literal("Logged in")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(HontunTheme.green()))));
            int tw = font.width(lc);
            int gx = paneX + (leftPane() - (tw + 7)) / 2;
            addRenderableWidget(new StringWidget(gx + 7, ly, tw, font.lineHeight, lc, font));
            loggedDot = true;
            loggedDotX = gx + 1.75f;
            loggedDotY = ly + 4f;
        } else {
            addCentered("● Logged in", paneX, leftPane(), captionY + (lh + 2) * 2, HontunTheme.green());
        }

        if (HontunTheme.modern2()) {
            addLabel("Add account", paneX + 11, addLabelY, HontunTheme.accentHi());
        } else {
            addCentered("Add account", paneX, leftPane(), addLabelY, HontunTheme.subtext1());
        }

        int innerX = paneX + padIn;
        int innerW = leftPane() - 2 * padIn;

        valueField = HontunTextBox.plain(font, innerX, fieldY, innerW, 18, "value")
                .placeholder("username / token").maxLength(512).text(valueText);
        valueField.onChange(s -> valueText = s);
        addRenderableWidget(valueField);

        int bw = (innerW - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("Offline"), b -> addOffline())
                .bounds(innerX, row1Y, bw, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Microsoft"), b -> addMicrosoft())
                .bounds(innerX + bw + 4, row1Y, bw, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Altening"), b -> addToken(safeAltening()))
                .bounds(innerX, row2Y, bw, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Session"), b -> addToken(safeSession()))
                .bounds(innerX + bw + 4, row2Y, bw, btnH).build());

        int listX = paneX + leftPane() + 12;
        int listW = contentRight() - listX;

        HontunTextBox search = HontunTextBox.search(font, listX, top, listW, 18, "Search")
                .placeholder("search accounts…").text(filter);

        search.onChange(s -> { filter = s; refreshList(); });
        search.onClear(() -> { filter = ""; refreshList(); });
        addRenderableWidget(search);

        int listY = top + 22;
        list = new AccountCardList(minecraft, listW, bottom - listY, listY, this::login, this::delete);
        list.setX(listX);
        refreshList();
        list.setScroll(savedScroll);
        addRenderableWidget(list);

        if (!status.isEmpty()) {
            boolean good = status.startsWith("Add") || status.startsWith("Logged");
            int rgb = good ? HontunTheme.green() : HontunTheme.textDim();
            if (HontunTheme.smog()) {
                addCentered(status, px, pw, statusY(), rgb);
            } else {
                addCentered(status, contentLeft(), contentWidth(), statusY(), rgb);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g, mouseX, mouseY, delta);

        if (HontunTheme.smog()) {
            cz.honzasik.hontun.gui.widget.HontunRound.card(g, modelCardX, modelCardY, modelCardW, modelCardH, 5,
                    HontunTheme.argb(0xB0, HontunTheme.base()), HontunTheme.argb(0xFF, HontunTheme.overlay2()));
            cz.honzasik.hontun.gui.widget.HontunRound.card(g, addBoxX, addBoxY, addBoxW, addBoxH, 5,
                    HontunTheme.argb(0x66, 0x000000), HontunTheme.argb(0x55, HontunTheme.overlay2()));
            if (loggedDot) {
                cz.honzasik.hontun.gui.widget.HontunRound.dot(g, loggedDotX, loggedDotY, 1.75f,
                        HontunTheme.argb(0xFF, HontunTheme.green()));
            }
            return;
        }
        if (!HontunTheme.modern2()) return;

        HontunShapes.fillClipped(g, modelCardX, modelCardY, modelCardW, modelCardH, 6, 6,
                HontunTheme.argb(0xC0, HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, modelCardX, modelCardY, modelCardW, modelCardH, 6, 6,
                HontunTheme.argb(0x70, HontunTheme.overlay0()));
        HontunShapes.groupBox(g, addBoxX, addBoxY, addBoxW, addBoxH, font.width("Add account"));
    }

    private void refreshList() {
        if (list == null) return;
        String f = filter.trim().toLowerCase(java.util.Locale.ROOT);
        List<Account<?>> accounts = new ArrayList<>();
        for (Account<?> a : Accounts.get()) {
            String n = a.getUsername();
            if (f.isEmpty() || (n != null && n.toLowerCase(java.util.Locale.ROOT).contains(f))) accounts.add(a);
        }

        Account<?> active = pickActive();
        list.set(accounts, a -> a == active);
    }

    private static Account<?> pickActive() {
        User u = Minecraft.getInstance().getUser();
        if (u == null) return null;
        UUID id = u.getProfileId();
        if (id != null) {
            String want = id.toString().replace("-", "");
            for (Account<?> a : Accounts.get()) {
                AccountCache c = a.getCache();
                if (c != null && c.uuid != null && !c.uuid.isEmpty()
                        && c.uuid.replace("-", "").equalsIgnoreCase(want)) {
                    return a;
                }
            }
        }
        for (Account<?> a : Accounts.get()) {
            if (a.getUsername() != null && a.getUsername().equalsIgnoreCase(u.getName())) return a;
        }
        return null;
    }

    private void addLabel(String text, int x, int y, int rgb) {
        Component c = Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        addRenderableWidget(new StringWidget(x, y, font.width(c), font.lineHeight, c, font));
    }

    private void addCentered(String text, int boxX, int boxW, int y, int rgb) {
        int cap = Math.max(8, boxW - 12);
        Component c = HontunCards.clip(font, text, cap).copy()
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        Component draw = cz.honzasik.hontun.utils.HontunFont.apply(c);
        int tw = font.width(draw);

        addRenderableWidget(new StringWidget(boxX + (boxW - tw) / 2, y, tw, font.lineHeight, draw, font));
    }

    private String value() { return valueField == null ? "" : valueField.getValue().trim(); }

    private Account<?> safeAltening() { String v = value(); return v.isEmpty() ? null : new TheAlteningAccount(v); }
    private Account<?> safeSession()  { String v = value(); return v.isEmpty() ? null : new SessionAccount(v); }

    private void addOffline() {
        String name = value();
        if (name.isEmpty()) { status = "Enter a username first"; rebuildWidgets(); return; }
        if (list != null) savedScroll = list.getScroll();
        CrackedAccount acc = new CrackedAccount(name);
        try { acc.fetchInfo(); } catch (Throwable ignored) {}
        if (!Accounts.get().exists(acc)) {
            Accounts.get().add(acc);
            Accounts.get().save();
            status = "Added " + name;
        } else {
            status = "Already added";
        }
        rebuildWidgets();
    }

    private void addToken(Account<?> acc) {
        if (acc == null) { status = "Paste a token first"; rebuildWidgets(); return; }
        if (list != null) savedScroll = list.getScroll();
        status = "Adding…";
        rebuildWidgets();
        MeteorExecutor.execute(() -> {
            boolean ok = false;
            try { ok = acc.fetchInfo(); } catch (Throwable ignored) {}
            boolean fok = ok;
            minecraft.execute(() -> {
                if (fok && !Accounts.get().exists(acc)) {
                    Accounts.get().add(acc);
                    Accounts.get().save();
                    status = "Added " + acc.getUsername();
                } else {
                    status = fok ? "Already added" : "Invalid/expired token";
                }
                rebuildWidgets();
            });
        });
    }

    private void addMicrosoft() {
        if (list != null) savedScroll = list.getScroll();
        try {
            String url = MicrosoftLogin.getRefreshToken(token -> {
                if (token == null) {
                    minecraft.execute(() -> { status = "Microsoft login failed/cancelled"; rebuildWidgets(); });
                    return;
                }
                MicrosoftAccount acc = new MicrosoftAccount(token);
                boolean ok = false;
                try { ok = acc.fetchInfo(); } catch (Throwable ignored) {}
                boolean fok = ok;
                minecraft.execute(() -> {
                    if (fok) {
                        Accounts.get().add(acc);
                        Accounts.get().save();
                        status = "Added " + acc.getUsername();
                    } else {
                        status = "Microsoft: failed to fetch info";
                    }
                    rebuildWidgets();
                });
            });
            try { minecraft.keyboardHandler.setClipboard(url); } catch (Throwable ignored) {}
            status = "Microsoft: sign in via browser (link copied)";
        } catch (Throwable t) {
            status = "Microsoft login error";
        }
        rebuildWidgets();
    }

    private void delete(Account<?> acc) {
        Accounts.get().remove(acc);
        Accounts.get().save();

        minecraft.execute(this::refreshList);
    }

    private void login(Account<?> acc) {
        if (list != null) savedScroll = list.getScroll();
        status = "Logging in…";
        rebuildWidgets();
        MeteorExecutor.execute(() -> {
            boolean ok = false;
            try {
                acc.fetchInfo();
                ok = acc.login();
            } catch (Throwable ignored) {}
            boolean fok = ok;
            minecraft.execute(() -> {
                status = fok ? "Logged in as " + acc.getUsername() : "Login failed";
                rebuildWidgets();
            });
        });
    }

    @SuppressWarnings("unused")
    private static boolean isActive(Account<?> acc) {
        User u = Minecraft.getInstance().getUser();
        if (u == null || acc == null) return false;
        UUID id = u.getProfileId();
        AccountCache c = acc.getCache();
        if (id != null && c != null && c.uuid != null && !c.uuid.isEmpty()
                && c.uuid.replace("-", "").equalsIgnoreCase(id.toString().replace("-", ""))) {
            return true;
        }
        return acc.getUsername() != null && acc.getUsername().equalsIgnoreCase(u.getName());
    }

    private static String currentTypeLabel() {
        User u = Minecraft.getInstance().getUser();
        if (u == null) return "-";
        for (Account<?> acc : Accounts.get()) {
            if (isActive(acc)) return acc.getType().name();
        }
        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + u.getName()).getBytes(StandardCharsets.UTF_8));
        return offline.equals(u.getProfileId()) ? "Offline" : "Premium";
    }
}
