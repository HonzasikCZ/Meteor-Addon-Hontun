package cz.honzasik.hontun.mixin.client.death;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.honzasik.hontun.modules.DeathFreecam;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraDeathViewMixin {
    @WrapOperation(method = "modifyFovBasedOnDeathOrFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"))
    private boolean hontun$noDeathZoom(LivingEntity entity, Operation<Boolean> original) {
        return original.call(entity) && !DeathFreecam.deadFreecam();
    }
}
