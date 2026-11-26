package cz.honzasik.hontun.copy.capture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record ItemInfo(String id, int count, boolean glint, Integer customModelData, String headTexture, String headName,
                       Map<String, String> components, int dataVersion) {

    public static ItemInfo of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer cmd = null;
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data != null && !data.floats().isEmpty()) cmd = Math.round(data.floats().getFirst());
        String texture = null;
        String name = null;
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile != null) {
            GameProfile gp = profile.partialProfile();
            if (gp != null) {
                for (Property p : gp.properties().get("textures")) {
                    texture = p.value();
                    break;
                }
                if (gp.name() != null && !gp.name().isEmpty()) name = gp.name();
            }
            if (name == null) name = profile.name().orElse(null);
        }
        return new ItemInfo(id, stack.getCount(), stack.hasFoil(), cmd, texture, name, components(stack), SharedConstants.getCurrentVersion().dataVersion().version());
    }

    private static Map<String, String> components(ItemStack stack) {
        Map<String, String> out = new LinkedHashMap<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return out;
        try {
            Tag encoded = ItemStack.CODEC.encodeStart(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).result().orElse(null);
            if (encoded instanceof CompoundTag root && root.get("components") instanceof CompoundTag comps) {
                for (String key : comps.keySet()) {
                    Tag t = comps.get(key);
                    if (t != null) out.put(key, t.toString());
                }
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    public String path() {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    public String material() {
        return path().toUpperCase(Locale.ROOT);
    }

    public boolean isHead() {
        return "minecraft:player_head".equals(id);
    }
}
