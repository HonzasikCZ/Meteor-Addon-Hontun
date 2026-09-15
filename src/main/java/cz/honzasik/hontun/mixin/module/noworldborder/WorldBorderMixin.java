package cz.honzasik.hontun.mixin.module.noworldborder;

import cz.honzasik.hontun.modules.NoWorldBorder;
import cz.honzasik.hontun.utils.MCUtil;
import cz.honzasik.hontun.utils.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {
    @Inject(
        method = "isInsideCloseToBorder(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hontun$noLocalBorderCollision(Entity entity, AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if (entity == MCUtil.MC.player && Util.isActiveAnd(NoWorldBorder.class, m -> m.collision.get())) {
            cir.setReturnValue(false);
        }
    }
}
