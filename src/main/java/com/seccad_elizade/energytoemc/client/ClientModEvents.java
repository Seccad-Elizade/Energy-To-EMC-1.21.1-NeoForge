package com.seccad_elizade.energytoemc.client;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.registry.ModBlocks;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import com.seccad_elizade.energytoemc.screen.EmcPipeScreen;
import com.seccad_elizade.energytoemc.screen.EmcConverterScreen;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = EnergyToEmc.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.EMC_PIPE.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EMC_PIPE_MENU.get(), EmcPipeScreen::new);
        event.register(ModMenuTypes.EMC_CONVERTER_MENU.get(), EmcConverterScreen::new);
        event.register(ModMenuTypes.EMC_CAPACITOR_MENU.get(), EmcCapacitorScreen::new);
    }
}