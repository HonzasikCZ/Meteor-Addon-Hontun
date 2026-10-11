package cz.honzasik.hontun.mixin.network.resourcepack;

import cz.honzasik.hontun.utils.PackTracker;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.network.Connection;
import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;

@Mixin(DownloadedPackSource.class)
public abstract class PackProgressMixin {
    @Inject(method = "createDownloadNotifier", at = @At("RETURN"), cancellable = true)
    private void hontun$trackDownload(int totalCount, CallbackInfoReturnable<HttpUtil.DownloadProgressListener> cir) {
        cir.setReturnValue(PackTracker.wrap(cir.getReturnValue(), totalCount));
    }

    @Inject(method = "createPackResponseSender", at = @At("RETURN"), cancellable = true)
    private static void hontun$trackFeedback(Connection connection, CallbackInfoReturnable<PackLoadFeedback> cir) {
        cir.setReturnValue(PackTracker.wrap(cir.getReturnValue()));
    }

    @Inject(method = "pushPack", at = @At("HEAD"))
    private void hontun$pushed(UUID id, URL url, String hash, CallbackInfo ci) {
        PackTracker.pushed(id);
    }

    @Inject(method = "pushLocalPack", at = @At("HEAD"))
    private void hontun$pushedLocal(UUID id, Path path, CallbackInfo ci) {
        PackTracker.pushed(id);
    }

    @Inject(method = "popPack", at = @At("HEAD"))
    private void hontun$popped(UUID id, CallbackInfo ci) {
        PackTracker.popped(id);
    }
}
