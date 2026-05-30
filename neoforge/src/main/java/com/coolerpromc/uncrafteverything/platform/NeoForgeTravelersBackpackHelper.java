package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.ITravelersBackpackHelper;
import com.tiviacz.travelersbackpack.init.ModItems;
import net.minecraft.world.item.Item;

public class NeoForgeTravelersBackpackHelper implements ITravelersBackpackHelper {
    @Override
    public Item getTankItem() {
        return ModItems.BACKPACK_TANK.get();
    }
}
