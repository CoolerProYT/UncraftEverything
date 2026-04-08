package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.NeoForgeUncraftEverythingClientConfig;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@EventBusSubscriber(modid = Constants.MODID)
public class UEModBusEvent {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get(), (blockEntity, direction) -> {
            if (direction == Direction.DOWN){
                return new WorldlyContainerWrapper(blockEntity.getOutputHandler(), null);
            }
            return new WorldlyContainerWrapper(blockEntity.getInputHandler(), null);
        });
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == NeoForgeUncraftEverythingClientConfig.CONFIG_SPEC) {
            NeoForgeUncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == NeoForgeUncraftEverythingClientConfig.CONFIG_SPEC) {
            NeoForgeUncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }
}
