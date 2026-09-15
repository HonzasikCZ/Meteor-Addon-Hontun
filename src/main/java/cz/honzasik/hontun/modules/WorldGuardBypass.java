package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class WorldGuardBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> flyKickBypass = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-fly-kick")
        .description("Every ~second report a little downward motion instead of moving, so flight-timer kicks don't fire.")
        .defaultValue(true)
        .build()
    );

    private int tick = 0;

    public WorldGuardBypass() {
        super(Hontun.CATEGORY, "world-guard-bypass", "Position-spoof test for WorldGuard region protection.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        info("Activated WorldGuard bypass.");
        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().flying = true;
        mc.player.onUpdateAbilities();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.options == null) return;

        if (mc.options.keyUp.isDown())         moveForward();
        else if (mc.options.keyDown.isDown())  moveBackward();
        else if (mc.options.keyLeft.isDown())  strafeLeft();
        else if (mc.options.keyRight.isDown()) strafeRight();
        else if (mc.options.keyJump.isDown())  moveUp();
        else if (mc.options.keyShift.isDown()) moveDown();
    }

    private void moveForward() {
        double yaw = Math.toRadians(mc.player.getYRot());
        sendMovementPacket(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    private void moveBackward() {
        double yaw = Math.toRadians(mc.player.getYRot());
        sendMovementPacket(Math.sin(yaw), 0, -Math.cos(yaw));
    }

    private void strafeLeft() {
        double yaw = Math.toRadians(mc.player.getYRot() - 90);
        sendMovementPacket(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    private void strafeRight() {
        double yaw = Math.toRadians(mc.player.getYRot() + 90);
        sendMovementPacket(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    private void moveUp() {
        sendMovementPacket(0, 0.06, 0);
    }

    private void moveDown() {
        sendMovementPacket(0, -0.06, 0);
    }

    private void sendMovementPacket(double x, double y, double z) {
        double speed = 0.06;
        double yspeed = 0.12;
        double xOffset = x * speed;
        double yOffset = y * yspeed;
        double zOffset = z * speed;

        tick++;

        if (flyKickBypass.get() && tick % 20 == 0) {
            mc.player.setPos(mc.player.getX(), mc.player.getY() - 0.12, mc.player.getZ());
            return;
        }

        boolean onGround = mc.player.onGround();
        boolean hColl = mc.player.horizontalCollision;

        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
            mc.player.getX() + xOffset, mc.player.getY() + yOffset, mc.player.getZ() + zOffset,
            onGround, hColl));

        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
            mc.player.getX() + 420, mc.player.getY(), mc.player.getZ() + 420,
            onGround, hColl));
    }
}
