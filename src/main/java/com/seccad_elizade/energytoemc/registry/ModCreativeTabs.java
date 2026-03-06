package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnergyToEmc.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENERGY_TO_EMC_TAB =
            CREATIVE_MODE_TABS.register("energytoemc_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.energytoemc"))
                            .icon(() -> new ItemStack(ModItems.ATOMIC_CAPACITOR.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.WRENCH.get());

                                output.accept(ModItems.EMC_CONVERTER_ITEM.get());
                                output.accept(ModItems.EMC_PIPE_ITEM.get());

                                output.accept(ModItems.BASIC_CAPACITOR.get());
                                output.accept(ModItems.HARDENED_CAPACITOR.get());
                                output.accept(ModItems.REINFORCED_CAPACITOR.get());
                                output.accept(ModItems.ATOMIC_CAPACITOR.get());
                                output.accept(ModItems.SINGULARITY_CAPACITOR.get());

                                output.accept(ModItems.UPGRADE_TIER_1.get());
                                output.accept(ModItems.UPGRADE_TIER_2.get());
                                output.accept(ModItems.UPGRADE_TIER_3.get());
                                output.accept(ModItems.UPGRADE_TIER_4.get());
                                output.accept(ModItems.UPGRADE_TIER_5.get());

                                output.accept(ModItems.PIPE_UPGRADE_1.get());
                                output.accept(ModItems.PIPE_UPGRADE_2.get());
                                output.accept(ModItems.PIPE_UPGRADE_3.get());
                                output.accept(ModItems.PIPE_UPGRADE_4.get());
                                output.accept(ModItems.PIPE_UPGRADE_5.get());
                            })
                            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}