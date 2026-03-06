package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.item.ConversionUpgradeItem;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import com.seccad_elizade.energytoemc.item.WrenchItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnergyToEmc.MODID);

    public static final DeferredItem<BlockItem> EMC_CONVERTER_ITEM = ITEMS.registerSimpleBlockItem("emc_converter", ModBlocks.EMC_CONVERTER);
    public static final DeferredItem<BlockItem> EMC_PIPE_ITEM = ITEMS.registerSimpleBlockItem("emc_pipe", ModBlocks.EMC_PIPE);

    public static final DeferredItem<BlockItem> BASIC_CAPACITOR = ITEMS.registerSimpleBlockItem("basic_emc_capacitor", ModBlocks.BASIC_CAPACITOR);
    public static final DeferredItem<BlockItem> HARDENED_CAPACITOR = ITEMS.registerSimpleBlockItem("hardened_emc_capacitor", ModBlocks.ADVANCED_CAPACITOR);
    public static final DeferredItem<BlockItem> REINFORCED_CAPACITOR = ITEMS.registerSimpleBlockItem("reinforced_emc_capacitor", ModBlocks.ELITE_CAPACITOR);
    public static final DeferredItem<BlockItem> ATOMIC_CAPACITOR = ITEMS.registerSimpleBlockItem("atomic_emc_capacitor", ModBlocks.ULTIMATE_CAPACITOR);

    public static final DeferredItem<BlockItem> SINGULARITY_CAPACITOR = ITEMS.register("singularity_emc_capacitor",
            () -> new BlockItem(ModBlocks.CREATIVE_CAPACITOR.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<PipeUpgradeItem> PIPE_UPGRADE_1 = ITEMS.register("pipe_upgrade_1",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1), 0, 360));
    public static final DeferredItem<PipeUpgradeItem> PIPE_UPGRADE_2 = ITEMS.register("pipe_upgrade_2",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1), 1, 1200));
    public static final DeferredItem<PipeUpgradeItem> PIPE_UPGRADE_3 = ITEMS.register("pipe_upgrade_3",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1), 2, 4000));
    public static final DeferredItem<PipeUpgradeItem> PIPE_UPGRADE_4 = ITEMS.register("pipe_upgrade_4",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1), 3, 20000));
    public static final DeferredItem<PipeUpgradeItem> PIPE_UPGRADE_5 = ITEMS.register("pipe_upgrade_5",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1), 4, 100000));

    public static final DeferredItem<ConversionUpgradeItem> UPGRADE_TIER_1 = ITEMS.register("upgrade_tier_1",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1), 3));
    public static final DeferredItem<ConversionUpgradeItem> UPGRADE_TIER_2 = ITEMS.register("upgrade_tier_2",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1), 6));
    public static final DeferredItem<ConversionUpgradeItem> UPGRADE_TIER_3 = ITEMS.register("upgrade_tier_3",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1), 9));
    public static final DeferredItem<ConversionUpgradeItem> UPGRADE_TIER_4 = ITEMS.register("upgrade_tier_4",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1), 12));
    public static final DeferredItem<ConversionUpgradeItem> UPGRADE_TIER_5 = ITEMS.register("upgrade_tier_5",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1), 15));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}