package cz.honzasik.hontun.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public class CenterCommand extends Command {
    private static final String[] DIRS = {
        "South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"
    };

    public CenterCommand() {
        super("center", "Snaps you to the middle of your block, snaps your look to a clean direction, or both.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(ctx -> { position(); return SINGLE_SUCCESS; });
        builder.then(literal("position").executes(ctx -> { position(); return SINGLE_SUCCESS; }));
        builder.then(literal("look").executes(ctx -> { look(); return SINGLE_SUCCESS; }));
        builder.then(literal("both").executes(ctx -> { position(); look(); return SINGLE_SUCCESS; }));
    }

    private void position() {
        if (!Utils.canUpdate()) return;
        LocalPlayer p = Minecraft.getInstance().player;

        double x = Mth.floor(p.getX()) + 0.5;
        double y = p.getY();
        double z = Mth.floor(p.getZ()) + 0.5;

        p.setPos(x, y, z);
        p.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, p.onGround(), p.horizontalCollision));

        info("Centered to (highlight)%.1f(default), (highlight)%.1f(default).", x, z);
    }

    private void look() {
        if (!Utils.canUpdate()) return;
        LocalPlayer p = Minecraft.getInstance().player;

        int step = Math.round(Mth.wrapDegrees(p.getYRot()) / 45f);
        float yaw = step * 45f;
        float pitch = 0f;

        p.setYRot(yaw);
        p.setXRot(pitch);
        p.yRotO = yaw;
        p.xRotO = pitch;
        p.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision));

        String dir = DIRS[((step % 8) + 8) % 8];
        info("Look centered: yaw (highlight)%.0f(default), pitch (highlight)0(default) - facing (highlight)%s(default).", yaw, dir);
    }
}
