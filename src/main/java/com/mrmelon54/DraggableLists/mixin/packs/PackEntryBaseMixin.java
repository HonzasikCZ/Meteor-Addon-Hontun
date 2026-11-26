package com.mrmelon54.DraggableLists.mixin.packs;

import com.mrmelon54.DraggableLists.duck.PackEntryDuck;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PackSelectionModel.EntryBase.class)
public abstract class PackEntryBaseMixin implements PackEntryDuck {
    @Shadow
    @Final
    private Pack pack;

    @Shadow
    protected abstract List<Pack> getSelfList();

    @Shadow
    protected abstract void move(int direction);

    @Override
    public Pack dl$pack() {
        return pack;
    }

    @Override
    public void dl$moveNextTo(Pack anchor, boolean after) {
        if (pack == anchor || pack.isFixedPosition()) return;

        List<Pack> list = getSelfList();
        int current = list.indexOf(pack);
        int anchorIndex = list.indexOf(anchor);
        if (current < 0 || anchorIndex < 0) return;

        int target = anchorIndex + (after ? 1 : 0);
        if (target > current) target--;

        int first = 0;
        while (first < list.size() && list.get(first).isFixedPosition()) first++;
        int last = list.size() - 1;
        while (last >= 0 && list.get(last).isFixedPosition()) last--;
        if (first > last) return;
        target = Mth.clamp(target, first, last);

        if (target == current) return;
        move(target - current);
    }
}
