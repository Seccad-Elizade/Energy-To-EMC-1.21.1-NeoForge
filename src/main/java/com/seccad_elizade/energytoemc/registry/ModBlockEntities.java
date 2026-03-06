package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import com.seccad_elizade.energytoemc.block.entity.EmcConverterBlockEntity;
import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EnergyToEmc.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmcConverterBlockEntity>> EMC_CONVERTER_BE =
            BLOCK_ENTITIES.register("emc_converter", () ->
                    BlockEntityType.Builder.of(EmcConverterBlockEntity::new,
                            ModBlocks.EMC_CONVERTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmcPipeBlockEntity>> EMC_PIPE_BE =
            BLOCK_ENTITIES.register("emc_pipe", () ->
                    BlockEntityType.Builder.of(EmcPipeBlockEntity::new,
                            ModBlocks.EMC_PIPE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmcCapacitorBlockEntity>> EMC_CAPACITOR_BE =
            BLOCK_ENTITIES.register("emc_capacitor", () ->
                    BlockEntityType.Builder.of(EmcCapacitorBlockEntity::new,
                            ModBlocks.BASIC_CAPACITOR.get(),
                            ModBlocks.ADVANCED_CAPACITOR.get(),
                            ModBlocks.ELITE_CAPACITOR.get(),
                            ModBlocks.ULTIMATE_CAPACITOR.get(),
                            ModBlocks.CREATIVE_CAPACITOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}