package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.widget.HontunTextBox;
import cz.honzasik.hontun.gui.widget.ProxyCardList;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.systems.proxies.ProxyType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HontunProxiesScreen extends HontunScreen {
    private static final int LEFT_PANE = 150;

    private HontunTextBox nameField, addressField, portField, userField, passField;

    private String nameText = "", addrText = "", portText = "", userText = "", passText = "";
    private ProxyCardList list;
    private double savedScroll;
    private int typeIdx = 0;
    private String status = "";

    public HontunProxiesScreen(Screen parent) {
        super(Component.literal("Proxies"), parent);
    }

    private ProxyType type() { return typeIdx == 0 ? ProxyType.Socks5 : ProxyType.Socks4; }

    @Override
    protected void buildContent() {
        int top = contentTop();
        int bottom = contentBottom();
        int paneX = contentLeft();
        int lh = font.lineHeight;

        addCentered("Add proxy", paneX, LEFT_PANE, top, HontunTheme.subtext1());

        int avail = bottom - top;
        boolean roomy = avail >= 190;
        int fieldH = roomy ? 18 : 14;
        int btnH = roomy ? 16 : 13;
        int step = fieldH + (roomy ? 2 : 1);
        int btnStep = btnH + (roomy ? 4 : 2);

        int y = top + lh + (roomy ? 4 : 2);
        nameField = field(paneX, y, "name (optional)", 64, nameText, s -> nameText = s, fieldH); y += step;
        addressField = field(paneX, y, "address", 128, addrText, s -> addrText = s, fieldH); y += step;
        portField = field(paneX, y, "port", 5, portText, s -> portText = s, fieldH).numeric(); y += step;

        addRenderableWidget(Button.builder(Component.literal(type().name()), b -> cycleType())
                .bounds(paneX, y, LEFT_PANE, btnH).build());
        y += btnStep;

        userField = field(paneX, y, "username (optional)", 64, userText, s -> userText = s, fieldH); y += step;
        passField = field(paneX, y, "password (optional)", 64, passText, s -> passText = s, fieldH).masked();
        y += step + (roomy ? 4 : 2);

        addRenderableWidget(Button.builder(Component.literal("Add proxy"), b -> addProxy())
                .bounds(paneX, y, LEFT_PANE, btnH).build());
        y += btnStep;
        addRenderableWidget(Button.builder(Component.literal("Check all"), b -> checkAll())
                .bounds(paneX, y, LEFT_PANE, btnH).build());

        int listX = paneX + LEFT_PANE + 12;
        int listW = contentRight() - listX;
        list = new ProxyCardList(minecraft, listW, bottom - top, top, this::toggle, this::delete);
        list.setX(listX);
        List<Proxy> proxies = new ArrayList<>();
        for (Proxy p : Proxies.get()) proxies.add(p);
        list.set(proxies);
        list.setScroll(savedScroll);
        addRenderableWidget(list);

        if (!status.isEmpty()) {
            boolean good = status.startsWith("Add") || status.startsWith("Check");
            addCentered(status, contentLeft(), contentWidth(), statusY(),
                    good ? HontunTheme.green() : HontunTheme.textDim());
        }
    }

    private HontunTextBox field(int x, int y, String hint, int maxLen, String initial, Consumer<String> sink, int h) {
        HontunTextBox e = HontunTextBox.plain(font, x, y, LEFT_PANE, h, hint)
                .placeholder(hint).maxLength(maxLen).text(initial);
        e.onChange(sink);
        addRenderableWidget(e);
        return e;
    }

    private void addCentered(String text, int boxX, int boxW, int y, int rgb) {
        Component c = Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        int tw = font.width(c);
        addRenderableWidget(new StringWidget(boxX + (boxW - tw) / 2, y, tw, font.lineHeight, c, font));
    }

    private void cycleType() {
        if (list != null) savedScroll = list.getScroll();
        typeIdx = (typeIdx + 1) % 2;
        rebuildWidgets();
    }

    private void toggle(Proxy p) {
        if (list != null) savedScroll = list.getScroll();
        boolean on = Boolean.TRUE.equals(p.enabled.get());
        Proxies.get().setEnabled(p, !on);
        minecraft.execute(this::rebuildWidgets);
    }

    private void delete(Proxy p) {
        if (list != null) savedScroll = list.getScroll();
        Proxies.get().remove(p);
        minecraft.execute(this::rebuildWidgets);
    }

    private void checkAll() {
        if (list != null) savedScroll = list.getScroll();
        try {
            Proxies.get().checkProxies(true);
            status = "Checking proxies…";
        } catch (Throwable t) {
            status = "Check failed";
        }
        rebuildWidgets();
    }

    private void addProxy() {
        String addr = addressField.getValue().trim();
        if (addr.isEmpty()) { status = "Enter an address"; rebuildWidgets(); return; }
        int port;
        try {
            port = Integer.parseInt(portField.getValue().trim());
        } catch (NumberFormatException e) {
            status = "Invalid port";
            rebuildWidgets();
            return;
        }
        if (port < 1 || port > 65535) { status = "Port out of range"; rebuildWidgets(); return; }

        String name = nameField.getValue().trim();
        if (name.isEmpty()) name = addr + ":" + port;

        Proxy.Builder b = new Proxy.Builder()
                .type(type())
                .address(addr)
                .port(port)
                .name(name)
                .enabled(false);
        String user = userField.getValue().trim();
        String pass = passField.getValue().trim();
        if (!user.isEmpty()) b.username(user);
        if (!pass.isEmpty()) b.password(pass);

        if (list != null) savedScroll = list.getScroll();
        status = Proxies.get().add(b.build()) ? "Added " + name : "Already exists";
        rebuildWidgets();
    }
}
