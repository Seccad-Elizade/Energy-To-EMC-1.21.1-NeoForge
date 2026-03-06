package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.block.EmcCapacitorBlock;
import com.seccad_elizade.energytoemc.block.EmcConverterBlock;
import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EnergyToEmc.MODID);

    public static final DeferredBlock<Block> EMC_CONVERTER = BLOCKS.register("emc_converter",
            () -> new EmcConverterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(4.0f, 12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<Block> EMC_PIPE = BLOCKS.register("emc_pipe",
            () -> new EmcPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .sound(SoundType.COPPER_GRATE)
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
                    .dynamicShape()
            ));

    public static final DeferredBlock<Block> BASIC_CAPACITOR = BLOCKS.register("basic_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(3.5f), 100_000L));

    public static final DeferredBlock<Block> ADVANCED_CAPACITOR = BLOCKS.register("hardened_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(5.0f), 1_000_000L));

    public static final DeferredBlock<Block> ELITE_CAPACITOR = BLOCKS.register("reinforced_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(7.0f), 10_000_000L));

    public static final DeferredBlock<Block> ULTIMATE_CAPACITOR = BLOCKS.register("atomic_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(10.0f).lightLevel(state -> 3), 100_000_000L));

    public static final DeferredBlock<Block> CREATIVE_CAPACITOR = BLOCKS.register("singularity_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(-1.0f).lightLevel(state -> 15), Long.MAX_VALUE));

    private static BlockBehaviour.Properties capacitorProps(float strength) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops()
                .mapColor(MapColor.COLOR_BLACK);

        if (strength < 0) {
            return props.strength(-1.0f, 3600000.0F);
        }
        return props.strength(strength, strength * 2.5f);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}