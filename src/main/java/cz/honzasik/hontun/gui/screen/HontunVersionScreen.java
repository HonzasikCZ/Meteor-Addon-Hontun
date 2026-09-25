package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.widget.HontunDotButton;
import cz.honzasik.hontun.gui.widget.VersionGridList;
import cz.honzasik.hontun.utils.VfpBridge;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HontunVersionScreen extends HontunScreen {
    private VersionGridList list;
    private double savedScroll;

    private final java.util.function.Consumer<Object> perServerSink;
    private final java.util.function.Supplier<Object> perServerCurrent;

    public HontunVersionScreen(Screen parent) {
        super(Component.literal("Protocol Version"), parent);
        this.perServerSink = null;
        this.perServerCurrent = null;
    }

    public HontunVersionScreen(Screen parent, String serverName,
                               java.util.function.Supplier<Object> current,
                               java.util.function.Consumer<Object> sink) {
        super(Component.literal("Version · " + serverName), parent);
        this.perServerSink = sink;
        this.perServerCurrent = current;
    }

    @Override
    protected void buildContent() {
        if (!VfpBridge.available()) {
            addRenderableWidget(Button.builder(Component.literal("ViaFabricPlus is not installed"), b -> onClose())
                    .bounds(contentLeft(), contentTop() + 40, contentWidth(), 20).build());
            return;
        }

        boolean perServer = perServerSink != null;
        Object current = perServer ? perServerCurrent.get() : VfpBridge.current();
        int top = contentTop();
        int listTop = top;

        if (perServer) {
            boolean cur = current == null;
            addRenderableWidget(new HontunDotButton(contentLeft(), top, contentWidth(), 20,
                    "Use global version", cur, b -> select(null)));
            listTop = top + 26;
        } else {
            Object auto = VfpBridge.autoDetect();
            if (auto != null) {
                boolean cur = auto.equals(current);
                addRenderableWidget(new HontunDotButton(contentLeft(), top, contentWidth(), 20,
                        "Auto Detect (1.7+)", cur, b -> select(auto)));
                listTop = top + 26;
            }
        }

        Object auto = perServer ? null : VfpBridge.autoDetect();
        java.util.List<VfpBridge.Version> grid = new java.util.ArrayList<>();
        for (VfpBridge.Version v : VfpBridge.versions()) {
            if (auto != null && auto.equals(v.handle())) continue;
            grid.add(v);
        }

        list = new VersionGridList(minecraft, contentWidth(), contentBottom() - listTop, listTop, this::select);
        list.setX(contentLeft());
        list.set(grid, current);
        list.setScroll(savedScroll);
        addRenderableWidget(list);
    }

    private void select(Object handle) {
        if (perServerSink != null) {
            perServerSink.accept(handle);
            if (list != null) savedScroll = list.getScroll();
            rebuildWidgets();
            return;
        }
        if (handle == null) return;
        VfpBridge.setTarget(handle);

        if (list != null) savedScroll = list.getScroll();
        rebuildWidgets();
    }
}
