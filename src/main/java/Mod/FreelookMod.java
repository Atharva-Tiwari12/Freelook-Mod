package yourmod.freelook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.lang.reflect.Field;

@Mod(modid = "freelookmod", name = "Freelook Mod", version = "1.0", clientSideOnly = true)
public class FreelookMod {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static KeyBinding freelookKey;
    private static boolean isFreelooking = false;
    private static int previousPerspective = 0;
    private static float savedYaw, savedPitch;
    private static float cameraYaw, cameraPitch;

    public FreelookMod() {
        MinecraftForge.EVENT_BUS.register(this);
        freelookKey = new KeyBinding("Freelook", Keyboard.KEY_R, "Freelook Mod");
        ClientRegistry.registerKeyBinding(freelookKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (freelookKey.isKeyDown()) {
            if (!isFreelooking) {
                previousPerspective = mc.gameSettings.thirdPersonView;
                mc.gameSettings.thirdPersonView = 1; // Force 3rd-person back view

                savedYaw = mc.thePlayer.rotationYaw;
                savedPitch = mc.thePlayer.rotationPitch;

                cameraYaw = savedYaw + 180.0F;
                cameraPitch = savedPitch;
                isFreelooking = true;
            }
        } else if (isFreelooking) {
            mc.gameSettings.thirdPersonView = previousPerspective;
            isFreelooking = false;
        }
    }


    @SubscribeEvent
    public void onRenderTick(net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent event) {
        if (isFreelooking) {
            mc.gameSettings.thirdPersonView = 1; // Ensure we're in 3rd person

            float sensitivity = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float deltaX = Mouse.getDX() * sensitivity * 0.15F;
            float deltaY = Mouse.getDY() * sensitivity * 0.15F;

            cameraYaw += deltaX;
            cameraPitch -= deltaY;
            cameraPitch = MathHelper.clamp_float(cameraPitch, -90.0F, 90.0F);

            // Keep player model in the same direction
            mc.thePlayer.rotationYaw = savedYaw;
            mc.thePlayer.rotationPitch = savedPitch;
        }
    }

    @SubscribeEvent
    public void onCameraSetup(net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event) {
        if (isFreelooking) {
            event.yaw = cameraYaw;
            event.pitch = cameraPitch;
        }
    }

    @SubscribeEvent
    public void onCameraDistanceAdjust(EntityViewRenderEvent.CameraSetup event) {
        if (isFreelooking) {
            try {
                Field thirdPersonField = mc.entityRenderer.getClass().getDeclaredField("thirdPersonDistance");
                thirdPersonField.setAccessible(true);
                thirdPersonField.setFloat(mc.entityRenderer, 2.5F); // Adjust this value as needed
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static float getCameraYaw() {
        return isFreelooking ? cameraYaw : mc.thePlayer.rotationYaw;
    }

    public static float getCameraPitch() {
        return isFreelooking ? cameraPitch : mc.thePlayer.rotationPitch;
    }
}
