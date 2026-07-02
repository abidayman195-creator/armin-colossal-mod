package com.dannysaot.colossalmod.client;

import com.dannysaot.colossalmod.entity.ArminsColossalNapeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/**
 * Renders the nape entity as a small player model (the shifter peeking out).
 * When not peaking, it renders invisible.
 */
public class ArminsColossalNapeRenderer extends MobEntityRenderer<ArminsColossalNapeEntity, PlayerEntityModel<ArminsColossalNapeEntity>> {

    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/steve.png");

    public ArminsColossalNapeRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.3F);
    }

    @Override
    public Identifier getTexture(ArminsColossalNapeEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(ArminsColossalNapeEntity entity,
                                net.minecraft.client.render.Frustum frustum,
                                double x, double y, double z) {
        // Only render when peaking
        return entity.isPeaking() && super.shouldRender(entity, frustum, x, y, z);
    }
}
