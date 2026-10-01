package cz.honzasik.hontun.gui.theme.style;

import cz.honzasik.hontun.gui.api.animation.Animation;
import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;

public final class StyleAnimation {
    private final Animation animation;
    private final long inMs;
    private final long outMs;
    private final int steps;

    private StyleAnimation(Easing easing, long inMs, long outMs, int steps, Direction initial) {
        this.inMs = Math.max(0, inMs);
        this.outMs = Math.max(0, outMs);
        this.steps = steps;
        if (initial == null) this.animation = new Animation(easing, this.inMs);
        else this.animation = new Animation(easing, initial.isForwards() ? this.inMs : this.outMs, initial);
    }

    public static StyleAnimation of(AnimSpec spec, HontunGuiTheme theme) {
        return create(spec, theme, null);
    }

    public static StyleAnimation of(AnimSpec spec, HontunGuiTheme theme, boolean forwardAtStart) {
        return create(spec, theme, forwardAtStart ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    private static StyleAnimation create(AnimSpec spec, HontunGuiTheme theme, Direction initial) {
        if (spec == null) spec = AnimSpec.NONE;
        Easing easing = spec.userDriven() ? theme.guiAnimationEasing() : spec.easing();
        long in = spec.userDriven() ? theme.guiAnimationDuration() : spec.inMs();
        long out = spec.userDriven() ? theme.guiAnimationDuration() : spec.outMs();
        return new StyleAnimation(easing == null ? Easing.LINEAR : easing, in, out, spec.steps(), initial);
    }

    private long durationFor(Direction direction) {
        return direction.isForwards() ? inMs : outMs;
    }

    public void forward() {
        if (inMs == 0) {
            animation.finishedAt(Direction.FORWARDS);
            return;
        }
        if (animation.getDirection().isForwards() && !animation.isIdle()) return;
        if (animation.isIdle()) start(Direction.FORWARDS);
        else animation.reverse(inMs);
    }

    public void backward() {
        if (outMs == 0) {
            finishedAt(Direction.BACKWARDS);
            return;
        }
        if (!animation.getDirection().isForwards()) return;
        if (animation.isIdle()) finishedAt(Direction.BACKWARDS);
        else animation.reverse(outMs);
    }

    public void start() {
        start(Direction.FORWARDS);
    }

    public void start(Direction direction) {
        animation.setDuration(durationFor(direction));
        animation.start(direction);
    }

    public void finishedAt(Direction direction) {
        animation.setDuration(durationFor(direction));
        animation.finishedAt(direction);
    }

    public void finished(Direction direction) {
        finishedAt(direction);
    }

    public void reverse() {
        animation.reverse(durationFor(animation.getDirection().opposite()));
    }

    public void reset() {
        animation.setDuration(inMs);
        animation.reset();
    }

    public double progress() {
        double p = animation.getProgress();
        if (steps > 0) return Math.floor(p * steps) / steps;
        return p;
    }

    public double getProgress() {
        return progress();
    }

    public double rawProgress() {
        return animation.getProgress();
    }

    public boolean running() {
        return animation.isRunning();
    }

    public boolean isRunning() {
        return animation.isRunning();
    }

    public boolean isFinished() {
        return animation.isFinished();
    }

    public boolean isIdle() {
        return animation.isIdle();
    }

    public boolean forwards() {
        return animation.getDirection().isForwards();
    }

    public Direction direction() {
        return animation.getDirection();
    }

    public int steps() {
        return steps;
    }
}
