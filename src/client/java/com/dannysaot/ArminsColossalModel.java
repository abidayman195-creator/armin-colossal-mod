package com.dannysaot.colossalmod.client;

import com.dannysaot.colossalmod.ColossalMod;
import com.dannysaot.colossalmod.entity.ArminsColossalEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * GeoModel pointing to the geo/animation/texture files for Armin's Colossal.
 * DefaultedEntityGeoModel auto-resolves:
 *   geo:        assets/colossalmod/geo/entity/armins_colossal.geo.json
 *   texture:    assets/colossalmod/textures/entity/armins_colossal.png
 *   animations: assets/colossalmod/animations/entity/armins_colossal.animation.json
 * based on the Identifier you pass in, so keep filenames matching "armins_colossal".
 */
public class ArminsColossalModel extends DefaultedEntityGeoModel<ArminsColossalEntity> {

    public ArminsColossalModel() {
        super(Identifier.of(ColossalMod.MOD_ID, "armins_colossal"));
    }
}
