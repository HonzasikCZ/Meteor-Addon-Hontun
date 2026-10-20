package cz.honzasik.hontun.captcha;

import cz.honzasik.hontun.Hontun;

import java.io.InputStream;

public final class CaptchaModel {
    private static volatile CaptchaNet net;
    private static volatile boolean tried;

    private CaptchaModel() {}

    public static CaptchaNet get() {
        if (net == null && !tried) {
            synchronized (CaptchaModel.class) {
                if (net == null && !tried) {
                    tried = true;
                    try (InputStream json = res("model.json"); InputStream weights = res("weights.bin")) {
                        if (json != null && weights != null) net = CaptchaNet.load(json, weights);
                    } catch (Throwable t) {
                        Hontun.LOG.warn("[Hontun] captcha model load failed: {}", t.toString());
                    }
                }
            }
        }
        return net;
    }

    private static InputStream res(String name) {
        return CaptchaModel.class.getResourceAsStream("/assets/hontun/captcha/" + name);
    }
}
