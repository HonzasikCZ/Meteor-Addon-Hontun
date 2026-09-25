package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.accounts.Account;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AccountCardList extends ObjectSelectionList<AccountCardList.AccountEntry> {
    private static final int ITEM_H = 36;
    private static final int DEL = 20;

    private final Consumer<Account<?>> onLogin;
    private final Consumer<Account<?>> onDelete;

    public AccountCardList(Minecraft mc, int width, int height, int y,
                           Consumer<Account<?>> onLogin, Consumer<Account<?>> onDelete) {
        super(mc, width, height, y, ITEM_H);
        this.onLogin = onLogin;
        this.onDelete = onDelete;
    }

    @Override protected double scrollRate() { return ITEM_H * 2.0; }

    @Override public int getRowWidth() { return getWidth() - 28; }

    @Override protected void extractListBackground(GuiGraphicsExtractor g) {}
    @Override protected void extractListSeparators(GuiGraphicsExtractor g) {}

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!HontunTheme.modern() || !scrollable()) { super.extractScrollbar(g, mouseX, mouseY); return; }
        HontunShapes.scrollbar(g, scrollBarX(), getY(), getBottom(), scrollbarWidth(),
                scrollBarY(), scrollerHeight());
    }

    @Override
    protected void extractSelection(GuiGraphicsExtractor g, AccountEntry e, int outlineColor) {
        if (HontunTheme.modern()) return;
        HontunCards.vanillaSelection(g, e.getX(), e.getY(), e.getWidth(), e.getHeight(), isFocused());
    }

    public double getScroll() { return scrollAmount(); }
    public void setScroll(double s) { setScrollAmount(s); }

    public void set(List<Account<?>> accounts, Predicate<Account<?>> isActive) {
        clearEntries();
        for (Account<?> a : accounts) addEntry(new AccountEntry(a, isActive.test(a)));
    }

    public class AccountEntry extends ObjectSelectionList.Entry<AccountEntry> {
        private final Account<?> account;
        private final boolean active;
        private final ResolvableProfile profile;

        AccountEntry(Account<?> account, boolean active) {
            this.account = account;
            this.active = active;
            this.profile = ResolvableProfile.createUnresolved(account.getUsername());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float pt) {
            Font f = Minecraft.getInstance().font;
            boolean modern = HontunTheme.modern();
            int x = getX(), w = getWidth();
            int inset = modern ? 3 : 0;
            int cy = getY() + inset;
            int ch = getHeight() - inset * 2;

            HontunCards.row(g, x, cy, w, ch, active, hovered, HontunTheme.accent());

            int hs = modern ? ch - 8 : 32;
            int hx = modern ? x + 8 + HontunCards.rowTextInset(active) : x + 2;
            int hy = modern ? cy + 4 : cy + 2;

            if (HontunTheme.modern2()) HontunShapes.iconFrame(g, hx - 2, hy - 2, hs + 4);
            PlayerFaceExtractor.extractRenderState(g, profile, hx, hy, hs);

            int tx = modern ? hx + hs + 8 : hx + 35;
            int nameY = modern ? cy + 6 : cy + 3;
            int subY = modern ? cy + 6 + f.lineHeight + 3 : cy + 14;

            int room = Math.max(0, delX() - 4 - tx);

            int nameCol = active ? HontunTheme.textLight() : HontunTheme.subtext1();
            g.text(f, cz.honzasik.hontun.utils.HontunFont.apply(HontunCards.clip(f, account.getUsername(), room)), tx, nameY,
                    HontunTheme.argb(0xFF, nameCol), true);
            int typeCol = active ? HontunTheme.green() : HontunTheme.textDim();
            if (active && HontunRound.smoothDots()) {
                Component typeText = cz.honzasik.hontun.utils.HontunFont.apply(
                        HontunCards.clip(f, account.getType().name(), room));
                int typeW = f.width(typeText);
                int col = HontunTheme.argb(0xFF, typeCol);
                g.text(f, typeText, tx, subY, col, true);
                int restRoom = room - typeW - 12;
                if (restRoom > 8) {
                    HontunRound.dot(g, tx + typeW + 6f, subY + 4f, 1.6f, col);
                    g.text(f, cz.honzasik.hontun.utils.HontunFont.apply(HontunCards.clip(f, "Logged in", restRoom)),
                            tx + typeW + 11, subY, col, true);
                }
            } else {
                String type = active ? account.getType().name() + "  • Logged in" : account.getType().name();
                g.text(f, cz.honzasik.hontun.utils.HontunFont.apply(HontunCards.clip(f, type, room)), tx, subY, HontunTheme.argb(0xFF, typeCol), true);
            }

            if (hovered) HontunCards.deleteButton(g, f, delX(), delY(), DEL, mouseX, mouseY);
        }

        private int delX() { return getX() + getWidth() - DEL - (HontunTheme.modern() ? 6 : 2); }

        private int delY() {
            int i = HontunTheme.modern() ? 3 : 0;
            return getY() + i + ((getHeight() - 2 * i) - DEL) / 2;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
            int bx = delX(), by = delY();

            if (e.x() >= bx && e.x() < bx + DEL && e.y() >= by && e.y() < by + DEL) {
                onDelete.accept(account);
            } else {
                setSelected(this);
                onLogin.accept(account);
            }
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(account.getUsername());
        }
    }
}
