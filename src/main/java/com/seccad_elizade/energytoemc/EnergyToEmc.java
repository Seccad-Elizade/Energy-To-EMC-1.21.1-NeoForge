package com.seccad_elizade.energytoemc;

import com.mojang.logging.LogUtils;
import com.seccad_elizade.energytoemc.event.CapabilityRegistration; // Import your registration class
import com.seccad_elizade.energytoemc.network.PayloadPipeMode;
import com.seccad_elizade.energytoemc.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(EnergyToEmc.MODID)
public class EnergyToEmc {
    public static final String MODID = "energytoemc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnergyToEmc(IEventBus modEventBus) {
        // Core Registries
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        // --- Capability Registration (Manual Listener) ---
        // This replaces the @EventBusSubscriber to fix the "removal" warnings
        modEventBus.addListener(CapabilityRegistration::registerCaps);

        // Network Event Listener
        modEventBus.addListener(this::registerNetworking);
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");

        registrar.playToServer(
                PayloadPipeMode.TYPE,
                PayloadPipeMode.CODEC,
                PayloadPipeMode::handleData
        );

        LOGGER.info("EnergyToEMC: Networking Payloads Registered.");
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}