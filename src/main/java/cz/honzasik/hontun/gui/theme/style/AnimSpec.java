package cz.honzasik.hontun.gui.theme.style;

import cz.honzasik.hontun.gui.api.animation.Easing;

public record AnimSpec(Easing easing, int inMs, int outMs, boolean userDriven, int steps) {
    public static final AnimSpec NONE = new AnimSpec(Easing.LINEAR, 0, 0, false, 0);
    public static final AnimSpec USER = new AnimSpec(Easing.LINEAR, 0, 0, true, 0);

    public static AnimSpec of(Easing easing, int inMs, int outMs) {
        return new AnimSpec(easing, inMs, outMs, false, 0);
    }

    public static AnimSpec stepped(Easing easing, int inMs, int outMs, int steps) {
        return new AnimSpec(easing, inMs, outMs, false, steps);
    }

    public AnimSpec withSteps(int steps) {
        return new AnimSpec(easing, inMs, outMs, userDriven, steps);
    }
}
