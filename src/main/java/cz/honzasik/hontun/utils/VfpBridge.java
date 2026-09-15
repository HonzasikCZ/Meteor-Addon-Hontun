package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.Hontun;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class VfpBridge {
    public record Version(Object handle, String name, int id) {}

    private static final String MOD_ID = "viafabricplus";

    private static boolean globalInit;
    private static boolean globalOk;
    private static boolean absenceLogged;

    private static Method mApi;
    private static Method mTargetVersion;
    private static Method mSetTarget;
    private static Method mLegacyGetTarget;
    private static Method mLegacySetTarget;
    private static boolean legacy;

    private static Method mGetProtocols;
    private static Method mGetName;
    private static Method mGetVersion;
    private static Object autoDetect;

    private static boolean perServerInit;
    private static boolean perServerOk;
    private static Method mGetForced;
    private static Method mSetForced;
    private static Method mSetPassedDirect;

    private VfpBridge() {}

    public static boolean available() {
        boolean present;
        try {
            present = FabricLoader.getInstance().isModLoaded(MOD_ID);
        } catch (Throwable t) {
            present = false;
        }
        if (!present) logOnce("ViaFabricPlus not installed");
        return present;
    }

    private static synchronized void logOnce(String reason) {
        if (absenceLogged) return;
        absenceLogged = true;
        Hontun.LOG.info("[Hontun] {} - version selection is disabled, everything else works normally.", reason);
    }

    private static synchronized void initGlobal() {
        if (globalInit) return;
        globalInit = true;
        try {
            Class<?> pv = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            try {
                mGetProtocols = pv.getMethod("getReversedProtocols");
            } catch (NoSuchMethodException e) {
                mGetProtocols = pv.getMethod("getProtocols");
            }
            mGetName = pv.getMethod("getName");
            mGetVersion = pv.getMethod("getVersion");

            try {
                Class<?> entry = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
                Class<?> api = Class.forName("com.viaversion.viafabricplus.api.ViaFabricPlusAPI");
                mApi = entry.getMethod("api");
                mTargetVersion = api.getMethod("targetVersion");
                mSetTarget = api.getMethod("setTargetVersion", pv);
                legacy = false;
            } catch (Throwable modern) {
                Class<?> translator = Class.forName("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator");
                mLegacyGetTarget = translator.getMethod("getTargetVersion");
                mLegacySetTarget = translator.getMethod("setTargetVersion", pv);
                legacy = true;
            }

            autoDetect = resolveAutoDetect();
            globalOk = true;
        } catch (Throwable t) {
            globalOk = false;
            logOnce("ViaFabricPlus present but its API does not match (" + t.getClass().getSimpleName() + ")");
        }
    }

    private static Object resolveAutoDetect() {
        try {
            Class<?> detector = Class.forName("com.viaversion.viafabricplus.protocoltranslator.util.ProtocolVersionDetector");
            return detector.getField("AUTO_DETECT_VERSION").get(null);
        } catch (Throwable ignored) {
        }
        try {
            Class<?> translator = Class.forName("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator");
            return translator.getField("AUTO_DETECT_PROTOCOL").get(null);
        } catch (Throwable ignored) {
        }
        try {
            for (Object handle : rawProtocols()) {
                if (((Integer) mGetVersion.invoke(handle)) == -2) return handle;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean ready() {
        if (!available()) return false;
        initGlobal();
        return globalOk;
    }

    private static List<?> rawProtocols() throws Exception {
        Object result = mGetProtocols.invoke(null);
        return result instanceof List<?> list ? list : List.of();
    }

    public static List<Version> versions() {
        List<Version> out = new ArrayList<>();
        if (!ready()) return out;
        try {
            for (Object handle : rawProtocols()) {
                String name = String.valueOf(mGetName.invoke(handle));
                int id = (Integer) mGetVersion.invoke(handle);
                out.add(new Version(handle, name, id));
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    public static Object autoDetect() {
        return ready() ? autoDetect : null;
    }

    public static Object current() {
        if (!ready()) return null;
        try {
            return legacy ? mLegacyGetTarget.invoke(null) : mTargetVersion.invoke(mApi.invoke(null));
        } catch (Throwable t) {
            return null;
        }
    }

    public static String currentName() {
        Object handle = current();
        return handle == null ? "?" : nameOf(handle);
    }

    public static String nameOf(Object handle) {
        if (handle == null || !ready()) return "?";
        try {
            return String.valueOf(mGetName.invoke(handle));
        } catch (Throwable t) {
            return "?";
        }
    }

    public static int idOf(Object handle) {
        if (handle == null || !ready()) return -1;
        try {
            return (Integer) mGetVersion.invoke(handle);
        } catch (Throwable t) {
            return -1;
        }
    }

    public static boolean setTarget(Object handle) {
        if (handle == null || !ready()) return false;
        try {
            if (legacy) mLegacySetTarget.invoke(null, handle);
            else mSetTarget.invoke(mApi.invoke(null), handle);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static synchronized void initPerServer() {
        if (perServerInit) return;
        perServerInit = true;
        try {
            Class<?> accessor = Class.forName("com.viaversion.viafabricplus.injection.access.core.IServerData");
            mGetForced = accessor.getMethod("viaFabricPlus$forcedVersion");
            for (Method m : accessor.getMethods()) {
                if (m.getName().equals("viaFabricPlus$forceVersion") && m.getParameterCount() == 1) mSetForced = m;
                else if (m.getName().equals("viaFabricPlus$passDirectConnectScreen") && m.getParameterCount() == 1) mSetPassedDirect = m;
            }
            perServerOk = mGetForced != null && mSetForced != null;
        } catch (Throwable t) {
            perServerOk = false;
        }
    }

    public static boolean perServerAvailable(Object serverData) {
        if (serverData == null || !available()) return false;
        initPerServer();
        return perServerOk;
    }

    public static Object forcedVersion(Object serverData) {
        if (!perServerAvailable(serverData)) return null;
        try {
            return mGetForced.invoke(serverData);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean setForcedVersion(Object serverData, Object handle) {
        if (!perServerAvailable(serverData)) return false;
        try {
            mSetForced.invoke(serverData, handle);
            if (mSetPassedDirect != null) mSetPassedDirect.invoke(serverData, false);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
