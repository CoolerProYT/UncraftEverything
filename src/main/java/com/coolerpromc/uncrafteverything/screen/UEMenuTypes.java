package com.coolerpromc.uncrafteverything.screen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UEMenuTypes {
    public static final ScreenHandlerType<UncraftingTableMenu> UNCRAFTING_TABLE_MENU =
            Registry.register(Registry.SCREEN_HANDLER, new Identifier(UncraftEverything.MODID, "uncrafting_table_menu"),
                    new ExtendedScreenHandlerType<>(UncraftingTableMenu::new));

    public static void register() {

    }
}
