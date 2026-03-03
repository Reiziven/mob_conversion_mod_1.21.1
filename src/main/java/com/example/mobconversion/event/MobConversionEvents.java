package com.example.mobconversion.event;

import com.example.mobconversion.util.EntityPoolManager;
import com.example.mobconversion.util.MobConversionManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MobConversionEvents {

    @SubscribeEvent
    public void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
            if (entityId != null && EntityPoolManager.getAllTriggerIds().contains(entityId.toString())) {
                MobConversionManager.onLivingEntityTick(serverLevel, livingEntity);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
        if (entityId != null && EntityPoolManager.getAllTriggerIds().contains(entityId.toString())) {
            MobConversionManager.clearCooldown(livingEntity.getUUID());
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
            if (entityId != null && EntityPoolManager.getAllTriggerIds().contains(entityId.toString())) {
                MobConversionManager.clearCooldown(livingEntity.getUUID());
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
            if (entityId != null) {
                boolean isTrigger = EntityPoolManager.getAllTriggerIds().contains(entityId.toString());
                if (isTrigger) {
                    org.apache.logging.log4j.LogManager.getLogger("MobConversion").info("Trigger entity {} was hurt, checking for conversion...", entityId);
                    MobConversionManager.onLivingEntityAttacked(serverLevel, livingEntity, event.getSource().getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MobConversionManager.tickParticles();
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("mobconversion_nodrops")) {
            event.getDrops().clear();
        }
    }
}
