package cz.honzasik.hontun.gui.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;

import java.util.List;

public final class HontunServerForm {
    public static final int LABEL_GAP = 12;
    private static final int TITLE_GAP = 14;
    private static final int FIELD_GAP = 8;
    private static final int SECTION_GAP = 16;
    private static final int BUTTON_GAP = 4;
    private static final int ACTION_GAP = 12;

    private HontunServerForm() {}

    public static void layout(int width, int height, List<EditBox> fields, List<AbstractWidget> options, List<AbstractWidget> actions) {
        int total = 9 + TITLE_GAP;
        for (int i = 0; i < fields.size(); i++) total += LABEL_GAP + fields.get(i).getHeight() + (i + 1 < fields.size() ? FIELD_GAP : 0);
        total += SECTION_GAP;
        for (int i = 0; i < options.size(); i++) total += options.get(i).getHeight() + (i + 1 < options.size() ? BUTTON_GAP : 0);
        if (!options.isEmpty()) total += ACTION_GAP;
        for (int i = 0; i < actions.size(); i++) total += actions.get(i).getHeight() + (i + 1 < actions.size() ? BUTTON_GAP : 0);

        int x = width / 2 - 100;
        int y = Math.max(8, (height - total) / 2) + 9 + TITLE_GAP;
        for (EditBox field : fields) {
            y += LABEL_GAP;
            field.setX(x);
            field.setY(y);
            y += field.getHeight() + FIELD_GAP;
        }
        y += SECTION_GAP - FIELD_GAP;
        for (AbstractWidget w : options) {
            w.setX(x);
            w.setY(y);
            y += w.getHeight() + BUTTON_GAP;
        }
        if (!options.isEmpty()) y += ACTION_GAP - BUTTON_GAP;
        for (AbstractWidget w : actions) {
            w.setX(x);
            w.setY(y);
            y += w.getHeight() + BUTTON_GAP;
        }
    }

    public static int titleY(EditBox first) {
        return first.getY() - LABEL_GAP - TITLE_GAP - 9;
    }
}
