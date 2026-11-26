package cz.honzasik.hontun.mixin.client.copy;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayInvoker {
    @Invoker("createTransformation")
    static Transformation hontun$transformation(SynchedEntityData data) {
        throw new AssertionError();
    }

    @Invoker("getBillboardConstraints")
    Display.BillboardConstraints hontun$billboard();

    @Invoker("getPackedBrightnessOverride")
    int hontun$brightness();

    @Invoker("getViewRange")
    float hontun$viewRange();

    @Invoker("getShadowRadius")
    float hontun$shadowRadius();

    @Invoker("getShadowStrength")
    float hontun$shadowStrength();

    @Invoker("getGlowColorOverride")
    int hontun$glowColor();
}
