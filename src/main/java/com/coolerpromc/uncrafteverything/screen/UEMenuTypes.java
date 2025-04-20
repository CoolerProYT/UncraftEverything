package com.coolerpromc.uncrafteverything.screen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class UEMenuTypes {
    public static final ScreenHandlerType<UncraftingTableMenu> UNCRAFTING_TABLE_MENU =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(UncraftEverything.MODID, "uncrafting_table_menu"),
                    new ExtendedScreenHandlerType<>(UncraftingTableMenu::new));

    public static void register() {

    }
}
