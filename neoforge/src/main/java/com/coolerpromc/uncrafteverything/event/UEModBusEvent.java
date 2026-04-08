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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@EventBusSubscriber(modid = Constants.MODID)
public class UEModBusEvent {
    static ResourceHandler<ItemResource> outputHandler = null;
    static ResourceHandler<ItemResource> inputHandler = null;

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get(), (blockEntity, direction) -> {
            if (outputHandler == null){
                outputHandler = new WorldlyContainerWrapper(blockEntity.getOutputHandler(), null);
            }
            if (inputHandler == null){
                inputHandler = new WorldlyContainerWrapper(blockEntity.getInputHandler(), null);
            }
            if (direction == Direction.DOWN){
                return outputHandler;
            }
            return inputHandler;
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
