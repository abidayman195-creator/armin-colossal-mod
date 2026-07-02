package com.dannysaot.colossalmod.client.hud;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders the Colossal Titan's health bar on the HUD while the player is transformed.
 * Matches Danny's AoT style: large bar at the bottom center of the screen.
 */
public class TitanHealthHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!(client.player.getVehicle() instanceof ArminsColossalEntity titan)) return;

        float health    = titan.getHealth();
        float maxHealth = titan.getMaxHealth();
        float ratio     = health / maxHealth;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int barW = 200;
        int barH = 12;
        int x = (screenW - barW) / 2;
        int y = screenH - 40;

        // Background
        context.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xFF000000);

        // Health bar color: green → yellow → red
        int barColor;
        if (ratio > 0.5f)      barColor = 0xFF00CC00;
        else if (ratio > 0.25f) barColor = 0xFFFFAA00;
        else                    barColor = 0xFFCC0000;

        int filledW = (int)(barW * ratio);
        context.fill(x, y, x + filledW, y + barH, barColor);

        // Empty part
        context.fill(x + filledW, y, x + barW, y + barH, 0xFF333333);

        // Text
        String label = String.format("Armin's Colossal  %.0f / %.0f", health, maxHealth);
        context.drawCenteredTextWithShadow(
            client.textRenderer, label,
            screenW / 2, y - 12, 0xFFFFFFFF);
    }
}
