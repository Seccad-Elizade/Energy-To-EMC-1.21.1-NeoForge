package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorMenu;
import com.seccad_elizade.energytoemc.screen.EmcConverterMenu;
import com.seccad_elizade.energytoemc.screen.EmcPipeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, EnergyToEmc.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EmcConverterMenu>> EMC_CONVERTER_MENU =
            MENUS.register("emc_converter_menu", () -> IMenuTypeExtension.create(EmcConverterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EmcPipeMenu>> EMC_PIPE_MENU =
            MENUS.register("emc_pipe_menu", () -> IMenuTypeExtension.create(EmcPipeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EmcCapacitorMenu>> EMC_CAPACITOR_MENU =
            MENUS.register("emc_capacitor_menu", () -> IMenuTypeExtension.create(EmcCapacitorMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}