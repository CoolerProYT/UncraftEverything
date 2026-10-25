package com.coolerpromc.uncrafteverything.compat.mod;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class WeaponModCompat {
    public static void removeComponents(ItemStack inputStack) {
        if (!BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("weaponmod")){
            return;
        }

        removeComponent(inputStack, "reload");
        removeComponent(inputStack, "halb");

        DataComponentType<?> bayonetDamage = component("bayonet-damage");
        if (bayonetDamage != null && inputStack.get(bayonetDamage) instanceof Short damage && damage == 0){
            inputStack.remove(bayonetDamage);
        }
    }

    private static void removeComponent(ItemStack inputStack, String path) {
        DataComponentType<?> component = component(path);
        if (component != null){
            inputStack.remove(component);
        }
    }

    private static DataComponentType<?> component(String path) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("weaponmod", path));
    }
}
