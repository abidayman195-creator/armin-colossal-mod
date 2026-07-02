package com.dannysaot.colossalmod.client;

import com.dannysaot.colossalmod.client.hud.TitanHealthHud;
import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import com.dannysaot.colossalmod.entity.ModEntities;
import com.dannysaot.colossalmod.network.AttackPayload;
import com.dannysaot.colossalmod.network.TransformPayload;
import com.dannysaot.colossalmod.network.TransformRequestPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ColossalModClient implements ClientModInitializer {

    public static KeyBinding transformKey;
    public static KeyBinding steamKey;

    @Override
    public void onInitializeClient() {
        // Entity renderers
        EntityRendererRegistry.register(ModEntities.ARMINS_COLOSSAL, ArminsColossalRenderer::new);
        EntityRendererRegistry.register(ModEntities.ARMINS_COLOSSAL_NAPE, ArminsColossalNapeRenderer::new);

        // HUD — titan health bar
        HudRenderCallback.EVENT.register(new TitanHealthHud());

        // Keybinds
        transformKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colossalmod.transform", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.colossalmod"));
        steamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colossalmod.steam", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.colossalmod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (transformKey.wasPressed()) {
                boolean isTransformed = client.player.getVehicle() instanceof ArminsColossalEntity;
                ClientPlayNetworking.send(new TransformRequestPayload(isTransformed ? 1 : 0));
            }

            if (client.player.getVehicle() instanceof ArminsColossalEntity) {
                while (steamKey.wasPressed()) {
                    ClientPlayNetworking.send(new AttackPayload(1));
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TransformPayload.TYPE, (payload, context) -> {});
    }
}
