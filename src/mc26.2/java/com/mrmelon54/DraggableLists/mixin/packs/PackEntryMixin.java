package com.mrmelon54.DraggableLists.mixin.packs;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.api.DragItem;
import com.mrmelon54.DraggableLists.duck.PackRowDuck;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class PackEntryMixin extends ObjectSelectionList.Entry<TransferableSelectionList.Entry>
    implements DragItem, PackRowDuck {

    @Unique
    private static final Set<String> DL$ARROW_SPRITES = Set.of(
        "transferable_list/move_up",
        "transferable_list/move_up_highlighted",
        "transferable_list/move_down",
        "transferable_list/move_down_highlighted"
    );

    @Shadow
    @Final
    private PackSelectionModel.Entry pack;

    @WrapWithCondition(
        method = "extractContent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
        )
    )
    private boolean dl$hideArrows(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                  int x, int y, int width, int height) {
        return !(dl$arrowsHidden() && DL$ARROW_SPRITES.contains(sprite.getPath()));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void dl$swallowArrowClicks(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!dl$arrowsHidden()) return;
        if (pack.canSelect()) return;

        int relX = (int) event.x() - getContentX();
        int relY = (int) event.y() - getContentY();
        if (relX < 16 || relX >= 32 || relY < 0 || relY >= 32) return;

        cir.setReturnValue(true);
    }

    @Unique
    private static boolean dl$arrowsHidden() {
        return DraggableLists.config().hideResourcePackArrows;
    }

    @Override
    public String dl$stableId() {
        return pack.getId();
    }

    @Override
    public boolean dl$canDrag() {
        return !pack.isFixedPosition();
    }

    @Override
    public PackSelectionModel.Entry dl$model() {
        return pack;
    }
}
