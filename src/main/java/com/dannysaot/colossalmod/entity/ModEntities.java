package com.dannysaot.colossalmod.entity;

import com.dannysaot.colossalmod.ColossalMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<ArminsColossalEntity> ARMINS_COLOSSAL = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(ColossalMod.MOD_ID, "armins_colossal"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ArminsColossalEntity::new)
            .dimensions(EntityDimensions.fixed(12.0F, 60.0F))
            .trackRangeBlocks(128)
            .trackedUpdateRate(1)
            .build()
    );

    public static final EntityType<ArminsColossalNapeEntity> ARMINS_COLOSSAL_NAPE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(ColossalMod.MOD_ID, "armins_colossal_nape"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ArminsColossalNapeEntity::new)
            .dimensions(EntityDimensions.fixed(2.0F, 2.0F))
            .trackRangeBlocks(128)
            .trackedUpdateRate(1)
            .build()
    );

    public static void registerModEntities() {
        FabricDefaultAttributeRegistry.register(
            ARMINS_COLOSSAL, ArminsColossalEntity.createArminsColossalAttributes());
        FabricDefaultAttributeRegistry.register(
            ARMINS_COLOSSAL_NAPE, PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0D).build());
    }
}
