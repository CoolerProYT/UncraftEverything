package com.coolerpromc.uncrafteverything.screen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableMenu;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class UEMenuTypes {
    public static final MenuType<UncraftingTableMenu> UNCRAFTING_TABLE_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_menu"), new ExtendedMenuType<>(UncraftingTableMenu::new, BlockPos.STREAM_CODEC));
    public static final MenuType<AutoUncraftingTableMenu> AUTO_UNCRAFTING_TABLE_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "auto_uncrafting_table_menu"), new ExtendedMenuType<>(AutoUncraftingTableMenu::new, BlockPos.STREAM_CODEC));

    public static void register() {

    }
}
