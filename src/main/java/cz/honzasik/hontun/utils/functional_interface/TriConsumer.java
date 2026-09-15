package cz.honzasik.hontun.utils.functional_interface;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}
