package com.example.watcher;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public class WatcherEntity extends HostileEntity {

    public WatcherEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createWatcherAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new StalkGoal(this));
    }

    @Override
    public boolean isFireImmune() { return true; }

    /** El susto: paquete al cliente, sonido, ceguera y desaparece. */
    public void jumpscare(ServerPlayerEntity p) {
        ServerPlayNetworking.send(p, WatcherMod.JUMPSCARE_PACKET, PacketByteBufs.create());
        getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.HOSTILE, 3.0f, 0.5f);
        getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 3.0f, 0.7f);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 0, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2, false, false));
        this.discard();
    }

    /** Se mueve solo cuando NO lo miras. Si lo miras fijo demasiado, desaparece. */
    static class StalkGoal extends Goal {
        private final WatcherEntity mob;
        private PlayerEntity target;
        private int watchedTicks = 0;
        private int ambientCooldown = 200;

        StalkGoal(WatcherEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            target = mob.getWorld().getClosestPlayer(mob, 64);
            return target != null && !target.isSpectator() && !target.isCreative();
        }

        @Override
        public boolean shouldContinue() {
            return target != null && target.isAlive() && !target.isSpectator()
                    && mob.squaredDistanceTo(target) < 80 * 80;
        }

        private boolean isWatched() {
            Vec3d look = target.getRotationVec(1.0f).normalize();
            Vec3d to = mob.getEyePos().subtract(target.getEyePos()).normalize();
            return look.dotProduct(to) > 0.55 && target.canSee(mob);
        }

        @Override
        public void tick() {
            double d2 = mob.squaredDistanceTo(target);
            mob.getLookControl().lookAt(target, 30f, 30f);

            if (--ambientCooldown <= 0) {
                ambientCooldown = 300 + mob.getRandom().nextInt(300);
                mob.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.AMBIENT, 0.6f, 0.5f);
            }

            if (isWatched()) {
                mob.getNavigation().stop();
                mob.setVelocity(0, mob.getVelocity().y, 0);
                if (++watchedTicks > 140) { mob.discard(); return; }
            } else {
                watchedTicks = Math.max(0, watchedTicks - 2);
                mob.getNavigation().startMovingTo(target, 1.5);
            }

            if (d2 < 2.5 * 2.5 && target instanceof ServerPlayerEntity sp) {
                mob.jumpscare(sp);
            }
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            watchedTicks = 0;
        }
    }
}
