package com.coolerpromc.uncrafteverything.screen;

import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.RegistryHandler;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableMenu;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;

public class UEMenuTypes {
    public static final RegistryHandler<MenuType<?>, MenuType<UncraftingTableMenu>> UNCRAFTING_TABLE_MENU = Services.REGISTRY.registerMenu("uncrafting_table_menu", UncraftingTableMenu::new, BlockPos.STREAM_CODEC);
    public static final RegistryHandler<MenuType<?>, MenuType<AutoUncraftingTableMenu>> AUTO_UNCRAFTING_TABLE_MENU = Services.REGISTRY.registerMenu("auto_uncrafting_table_menu", AutoUncraftingTableMenu::new, BlockPos.STREAM_CODEC);

    public static void load() {
    }
}
