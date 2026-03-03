package com.example.mobconversion;

import com.example.mobconversion.config.MobConversionConfig;
import com.example.mobconversion.event.MobConversionEvents;
import com.example.mobconversion.util.EntityPoolManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

@Mod(MobConversionMod.MOD_ID)
public class MobConversionMod {
    public static final String MOD_ID = "mobconversion";
    private static final Logger LOGGER = LogManager.getLogger("MobConversion");

    public MobConversionMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOGGER.info("Initializing Mob Conversion Mod for Forge 1.20.1");

        Path configDir = FMLPaths.CONFIGDIR.get();
        migrateConfig(configDir, "villagerguard-common.toml", MOD_ID + "-common.toml");
        migrateConfig(configDir, "villagerguardconversion-common.toml", MOD_ID + "-common.toml");

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobConversionConfig.SPEC);
        modEventBus.addListener(this::onConfigReload);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.example.mobconversion.client.ModClientEvents.registerConfigScreen();
        }

        MinecraftForge.EVENT_BUS.register(new MobConversionEvents());
    }

    private void migrateConfig(Path dir, String oldName, String newName) {
        Path oldPath = dir.resolve(oldName);
        Path newPath = dir.resolve(newName);
        if (Files.exists(oldPath) && Files.notExists(newPath)) {
            try {
                Files.copy(oldPath, newPath);
                LOGGER.info("Migrated legacy config from {} to {}", oldName, newName);
            } catch (Exception e) {
                LOGGER.warn("Failed to migrate legacy config from {}", oldName, e);
            }
        }
    }

    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MOD_ID)) {
            EntityPoolManager.invalidateCache();
            LOGGER.info("Config reloaded, entity pool cache invalidated.");
        }
    }
}
