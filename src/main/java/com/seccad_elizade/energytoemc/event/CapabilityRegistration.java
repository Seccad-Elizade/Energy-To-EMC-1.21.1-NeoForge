package com.seccad_elizade.energytoemc.event;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// NOTICE: The @EventBusSubscriber annotation is REMOVED from here
public class CapabilityRegistration {

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        EnergyToEmc.LOGGER.info("EnergyToEMC: Registering Capabilities...");

        // Converter
        event.registerBlockEntity(PECapabilities.EMC_STORAGE_CAPABILITY, ModBlockEntities.EMC_CONVERTER_BE.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.EMC_CONVERTER_BE.get(), (be, side) -> be.getEnergyStorage());

        // Pipe
        event.registerBlockEntity(PECapabilities.EMC_STORAGE_CAPABILITY, ModBlockEntities.EMC_PIPE_BE.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.EMC_PIPE_BE.get(), (be, side) -> be.getEnergyStorage());

        // Capacitor
        event.registerBlockEntity(PECapabilities.EMC_STORAGE_CAPABILITY, ModBlockEntities.EMC_CAPACITOR_BE.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.EMC_CAPACITOR_BE.get(), (be, side) -> be.getEnergyStorage());
    }
}