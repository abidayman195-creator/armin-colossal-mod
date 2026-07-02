package com.dannysaot.colossalmod.entity;

import com.dannysaot.colossalmod.network.TransformPayload;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArminsColossalEntity extends HostileEntity implements GeoEntity {

    // Tracked data
    private static final TrackedData<Optional<UUID>> DATA_SHIFTER_UUID =
        DataTracker.registerData(ArminsColossalEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Boolean> DATA_IS_ATTACKING =
        DataTracker.registerData(ArminsColossalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_IS_STEAMING =
        DataTracker.registerData(ArminsColossalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_IS_KICKING =
        DataTracker.registerData(ArminsColossalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> DATA_TRANSFORMATION_TICKS =
        DataTracker.registerData(ArminsColossalEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Animations
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("animation.armins_colossal.idle");
    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("animation.armins_colossal.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.armins_colossal.attack");
    private static final RawAnimation STEAM  = RawAnimation.begin().thenPlay("animation.armins_colossal.steam");
    private static final RawAnimation KICK   = RawAnimation.begin().thenPlay("animation.armins_colossal.kick");

    // Cooldowns (matching Danny's AoT values)
    private int attackCooldown = 0;
    private int steamCooldown  = 0;
    private int kickCooldown   = 0;
    private int steamActiveTicks = 0;

    // Constants matching Danny's AoT Colossal
    public static final int ATTACK_COOLDOWN_TICKS  = 20;
    public static final int STEAM_COOLDOWN_TICKS   = 100;
    public static final int KICK_COOLDOWN_TICKS    = 30;
    public static final int TRANSFORM_TICKS        = 40;
    public static final double STEAM_RADIUS        = 60.0;
    public static final double STEAM_MIN_DIST      = 5.0;
    public static final double STEAM_BASE_PUSH     = 0.35;
    public static final double STEAM_MAX_PUSH      = 0.8;
    public static final int BLINDNESS_DURATION     = 100; // ~5 seconds
    public static final double KICK_RANGE          = 15.0;
    public static final double KICK_HALF_WIDTH     = 5.0;
    public static final float KICK_DAMAGE          = 50.0F;

    public ArminsColossalEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_SHIFTER_UUID, Optional.empty());
        builder.add(DATA_IS_ATTACKING, false);
        builder.add(DATA_IS_STEAMING, false);
        builder.add(DATA_IS_KICKING, false);
        builder.add(DATA_TRANSFORMATION_TICKS, 0);
    }

    public static DefaultAttributeContainer.Builder createArminsColossalAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5000.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.12D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 50.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 20.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(2, new WanderAroundGoal(this, 0.6D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient()) {
            if (attackCooldown > 0) attackCooldown--;
            if (steamCooldown  > 0) steamCooldown--;
            if (kickCooldown   > 0) kickCooldown--;

            // Steam active tick — push players continuously while steaming
            if (steamActiveTicks > 0) {
                steamActiveTicks--;
                applySteamPush();
                if (steamActiveTicks == 0) {
                    dataTracker.set(DATA_IS_STEAMING, false);
                }
            }

            // Transformation smoke
            int tx = dataTracker.get(DATA_TRANSFORMATION_TICKS);
            if (tx > 0) {
                dataTracker.set(DATA_TRANSFORMATION_TICKS, tx - 1);
                spawnSteamParticles(30);
            }

            // Disable AI when shifter is riding
            if (getShifterUuid().isPresent()) {
                setTarget(null);
            }
        } else {
            if (dataTracker.get(DATA_IS_STEAMING)) spawnSteamParticles(8);
            if (dataTracker.get(DATA_TRANSFORMATION_TICKS) > 0) spawnSteamParticles(15);
        }
    }

    // ---- Abilities (matching Danny's AoT Colossal) ----

    public void performAttack(int type) {
        if (getWorld().isClient()) return;
        switch (type) {
            case 0 -> punch();
            case 1 -> steam();
            case 2 -> kick();
        }
    }

    private void punch() {
        if (attackCooldown > 0) return;
        attackCooldown = ATTACK_COOLDOWN_TICKS;
        triggerAttackAnimation();
        Vec3d look = getRotationVec(1.0F);
        Box hitBox = getBoundingBox().expand(getWidth() * 1.5).offset(look.multiply(getWidth()));
        LivingEntity shifter = getShifterEntity();
        getWorld().getEntitiesByClass(LivingEntity.class, hitBox,
            e -> e != this && e != shifter).forEach(e -> {
                e.damage(getDamageSources().mobAttack(this),
                    (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                e.addVelocity(look.x * 2, 0.5, look.z * 2);
            });
    }

    /**
     * Steam: identical to Danny's AoT Colossal.
     * Pushes players in a 60-block radius with intensity based on distance.
     * Applies blindness to nearby entities.
     * Continues pushing for ~2 seconds.
     */
    private void steam() {
        if (steamCooldown > 0) return;
        steamCooldown = STEAM_COOLDOWN_TICKS;
        dataTracker.set(DATA_IS_STEAMING, true);
        steamActiveTicks = 40; // 2 seconds of active steam
        triggerSteamAnimation();
        spawnSteamParticles(200);
        playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 8.0F, 0.5F);

        // Initial blindness burst
        LivingEntity shifter = getShifterEntity();
        getWorld().getEntitiesByClass(LivingEntity.class,
            getBoundingBox().expand(STEAM_RADIUS),
            e -> e != this && e != shifter).forEach(e -> {
                e.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, BLINDNESS_DURATION, 0));
                e.damage(getDamageSources().onFire(), 8.0F);
            });
    }

    /**
     * Apply steam push each tick while steaming — same as Danny's ColossalSteamHandler.
     * Push strength varies from BASE_PUSH_STRENGTH to MAX_PUSH_STRENGTH based on distance.
     */
    private void applySteamPush() {
        LivingEntity shifter = getShifterEntity();
        Vec3d titanPos = getPos();
        getWorld().getEntitiesByClass(LivingEntity.class,
            getBoundingBox().expand(STEAM_RADIUS),
            e -> e != this && e != shifter).forEach(e -> {
                double dx = e.getX() - titanPos.x;
                double dz = e.getZ() - titanPos.z;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < STEAM_MIN_DIST) return;

                double intensity = 1.0 - (dist / STEAM_RADIUS);
                double pushStrength = STEAM_BASE_PUSH + (STEAM_MAX_PUSH - STEAM_BASE_PUSH) * intensity;

                double pushX = (dx / dist) * pushStrength;
                double pushZ = (dz / dist) * pushStrength;
                double pushY = 0.45 * intensity;

                Vec3d currentVel = e.getVelocity();
                e.setVelocity(
                    currentVel.x * 0.8 + pushX,
                    currentVel.y + pushY,
                    currentVel.z * 0.8 + pushZ
                );
                e.velocityModified = true;
            });
    }

    /**
     * Kick: same geometry as Danny's AoT — forward cone with half-width and range.
     */
    private void kick() {
        if (kickCooldown > 0) return;
        kickCooldown = KICK_COOLDOWN_TICKS;
        triggerKickAnimation();
        Vec3d look = getRotationVec(1.0F).multiply(1, 0, 1).normalize();
        Vec3d right = new Vec3d(-look.z, 0, look.x);

        LivingEntity shifter = getShifterEntity();
        getWorld().getEntitiesByClass(LivingEntity.class,
            getBoundingBox().expand(KICK_RANGE + KICK_HALF_WIDTH, 10, KICK_RANGE + KICK_HALF_WIDTH),
            e -> e != this && e != shifter).forEach(e -> {
                Vec3d toTarget = e.getPos().subtract(getPos());
                double forward = toTarget.dotProduct(look);
                double lateral = Math.abs(toTarget.dotProduct(right));
                if (forward > 0 && forward < KICK_RANGE && lateral < KICK_HALF_WIDTH) {
                    e.damage(getDamageSources().mobAttack(this), KICK_DAMAGE);
                    e.addVelocity(look.x * 3, 1.0, look.z * 3);
                }
            });
    }

    // ---- Shifter UUID ----

    public void setShifterUuid(UUID uuid) {
        dataTracker.set(DATA_SHIFTER_UUID, Optional.ofNullable(uuid));
    }

    public Optional<UUID> getShifterUuid() {
        return dataTracker.get(DATA_SHIFTER_UUID);
    }

    private LivingEntity getShifterEntity() {
        return getShifterUuid().map(uuid -> {
            if (getWorld() instanceof ServerWorld sw) {
                return (LivingEntity) sw.getEntity(uuid);
            }
            return null;
        }).orElse(null);
    }

    public void startTransformation() {
        dataTracker.set(DATA_TRANSFORMATION_TICKS, TRANSFORM_TICKS);
        playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 8.0F, 0.5F);
        spawnSteamParticles(150);
    }

    private void spawnSteamParticles(int count) {
        World world = getWorld();
        for (int i = 0; i < count; i++) {
            double ox = (world.random.nextDouble() - 0.5) * getWidth() * 2;
            double oy = world.random.nextDouble() * getHeight();
            double oz = (world.random.nextDouble() - 0.5) * getWidth() * 2;
            world.addParticle(ParticleTypes.CLOUD,
                getX() + ox, getY() + oy, getZ() + oz, 0, 0.15, 0);
        }
    }

    // ---- Sounds ----

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENTITY_GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_GENERIC_HURT; }
    @Override protected float getSoundVolume() { return 8.0F; }
    @Override public boolean isPushable() { return !hasPassengers(); }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, this::attackPredicate)
            .triggerableAnim("attack", ATTACK)
            .triggerableAnim("steam",  STEAM)
            .triggerableAnim("kick",   KICK));
    }

    private PlayState movementPredicate(AnimationState<ArminsColossalEntity> state) {
        state.getController().setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    private PlayState attackPredicate(AnimationState<ArminsColossalEntity> state) {
        return PlayState.CONTINUE;
    }

    public void triggerAttackAnimation() { triggerAnim("attack", "attack"); }
    public void triggerSteamAnimation()  { triggerAnim("attack", "steam");  }
    public void triggerKickAnimation()   { triggerAnim("attack", "kick");   }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
