package com.dannysaot.colossalmod.entity;

import com.dannysaot.colossalmod.ColossalMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * The nape entity sits at the back of the Colossal Titan's head.
 * It is the entity that the shifter player rides/peaks out of.
 * Matches Danny's AoT ColossalTitanNapeEntity offsets:
 *   NAPE_OFFSET_BACK  = 6.0 blocks
 *   NAPE_HEIGHT_OFFSET = 52.0 blocks
 */
public class ArminsColossalNapeEntity extends PathAwareEntity {

    // Offset constants matching Danny's AoT
    public static final double NAPE_OFFSET_BACK   = 6.0;
    public static final double NAPE_HEIGHT_OFFSET  = 52.0;
    public static final int    NAPE_HITS_TO_INCAPACITATE = 3;
    public static final float  NAPE_DAMAGE         = 25.0F;
    public static final int    NAPE_HIT_COOLDOWN   = 20;

    private static final TrackedData<Optional<UUID>> DATA_PARENT_UUID =
        DataTracker.registerData(ArminsColossalNapeEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Boolean> DATA_PEAKING =
        DataTracker.registerData(ArminsColossalNapeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int napeHitCount   = 0;
    private int napeHitCooldown = 0;
    private ArminsColossalEntity cachedParent = null;

    public ArminsColossalNapeEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setInvisible(false);
        this.setInvulnerable(false);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_PARENT_UUID, Optional.empty());
        builder.add(DATA_PEAKING, false);
    }

    // ---- Parent titan link ----

    public void setParentUuid(UUID uuid) {
        dataTracker.set(DATA_PARENT_UUID, Optional.ofNullable(uuid));
    }

    public Optional<UUID> getParentUuid() {
        return dataTracker.get(DATA_PARENT_UUID);
    }

    public ArminsColossalEntity getParentTitan() {
        if (cachedParent != null && !cachedParent.isRemoved()) return cachedParent;
        if (getWorld() instanceof ServerWorld sw) {
            getParentUuid().ifPresent(uuid -> {
                if (sw.getEntity(uuid) instanceof ArminsColossalEntity titan) {
                    cachedParent = titan;
                }
            });
        }
        return cachedParent;
    }

    // ---- Peaking ----

    public boolean isPeaking() { return dataTracker.get(DATA_PEAKING); }
    public void setPeaking(boolean peaking) { dataTracker.set(DATA_PEAKING, peaking); }

    // ---- Tick: follow the titan's neck ----

    @Override
    public void tick() {
        super.tick();
        if (napeHitCooldown > 0) napeHitCooldown--;

        ArminsColossalEntity parent = getParentTitan();
        if (parent == null || parent.isRemoved()) {
            this.discard();
            return;
        }

        // Position nape at titan's neck (back + height offset)
        double yawRad  = Math.toRadians(parent.getYaw());
        double offsetX = -Math.sin(yawRad) * NAPE_OFFSET_BACK;
        double offsetZ =  Math.cos(yawRad) * NAPE_OFFSET_BACK;

        this.setPos(
            parent.getX() + offsetX,
            parent.getY() + NAPE_HEIGHT_OFFSET,
            parent.getZ() + offsetZ
        );
        this.setYaw(parent.getYaw());
    }

    // ---- Damage: nape hit system ----

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient()) return false;
        if (napeHitCooldown > 0) return false;

        // Only blades deal nape damage
        // For simplicity we accept any damage here and count hits
        napeHitCount++;
        napeHitCooldown = NAPE_HIT_COOLDOWN;

        ArminsColossalEntity parent = getParentTitan();

        if (napeHitCount >= NAPE_HITS_TO_INCAPACITATE) {
            napeHitCount = 0;
            // Force peek the shifter out
            setPeaking(true);
            if (parent != null) {
                parent.getShifterUuid().ifPresent(shifterUuid -> {
                    if (getWorld() instanceof ServerWorld sw &&
                        sw.getEntity(shifterUuid) instanceof ServerPlayerEntity player) {
                        player.sendMessage(
                            net.minecraft.text.Text.literal("§cYour nape was hit! You are exposed!"), false);
                    }
                });
            }
        } else {
            // Deal damage to the parent titan
            if (parent != null) {
                parent.damage(source, NAPE_DAMAGE);
            }
        }
        return true;
    }

    @Override public boolean isCollidable() { return true; }
    @Override public boolean isPushable()   { return false; }
    @Override protected void initGoals()    {} // No AI
}
