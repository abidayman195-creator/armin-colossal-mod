package com.dannysaot.colossalmod.client;

import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ArminsColossalRenderer extends GeoEntityRenderer<ArminsColossalEntity> {

    public ArminsColossalRenderer(EntityRendererFactory.Context context) {
        super(context, new ArminsColossalModel());
        // The titan model is huge in its native scale; GeckoLib handles scale via the geo file itself.
        // If it renders too big/small in-game relative to the hitbox, adjust shadowRadius or the model scale.
        this.shadowRadius = 6.0F;
    }
}
