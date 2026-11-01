package com.coolerpromc.uncrafteverything.screen;

import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.custom.*;

import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class UEMenuTypes {
    public static final Supplier<MenuType<UncraftingTableMenu>> UNCRAFTING_TABLE_MENU =
            Services.REGISTRY.registerMenu("uncrafting_table_menu", UncraftingTableMenu::new);
    public static final Supplier<MenuType<AutoUncraftingTableMenu>> AUTO_UNCRAFTING_TABLE_MENU =
            Services.REGISTRY.registerMenu(
                    "auto_uncrafting_table_menu", AutoUncraftingTableMenu::new);

    public static void register() {}
}
