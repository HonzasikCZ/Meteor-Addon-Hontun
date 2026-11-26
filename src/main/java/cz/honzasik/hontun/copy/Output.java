package cz.honzasik.hontun.copy;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Output {
    public final Target target;
    public final Map<String, String> files = new LinkedHashMap<>();
    public final Notes notes = new Notes();
    public String error;

    public Output(Target target) {
        this.target = target;
    }

    public Output fail(String message) {
        this.error = message;
        return this;
    }
}
