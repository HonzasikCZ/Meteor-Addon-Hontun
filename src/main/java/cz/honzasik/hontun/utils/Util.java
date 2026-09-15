package cz.honzasik.hontun.utils;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Util {
    public static final Random RANDOM = new Random();

    public static String leftZeroPad(String str, int size) {
        int pads = size - str.length();
        if (pads <= 0) return str;
        return "0".repeat(pads) + str;
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<? super T> initializer) {
        initializer.accept(object);
        return object;
    }

    public static boolean isActive(Class<? extends Module> clazz) {
        return Modules.get().isActive(clazz);
    }

    public static <T extends Module> boolean isActiveAnd(Class<T> clazz, Predicate<T> predicate) {
        T module = Modules.get().get(clazz);
        return module != null && module.isActive() && predicate.test(module);
    }
}
