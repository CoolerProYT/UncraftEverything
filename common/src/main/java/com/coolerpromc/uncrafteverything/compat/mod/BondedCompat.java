package com.coolerpromc.uncrafteverything.compat.mod;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BondedCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("unstriplog");

    private static DataComponentType<?> ITEM_LEVEL_CONTAINER;
    private static DataComponentType<?> APPLIED_BONUSES_CONTAINER;
    private static DataComponentType<?> MAX_DAMAGE_MODIFIERS;

    static {
        try {
            Class<?> dataComponentsClass = Class.forName("com.iamkaf.bonded.registry.DataComponents");

            ITEM_LEVEL_CONTAINER = getComponentField(dataComponentsClass, "ITEM_LEVEL_CONTAINER");
            APPLIED_BONUSES_CONTAINER = getComponentField(dataComponentsClass, "APPLIED_BONUSES_CONTAINER");
            MAX_DAMAGE_MODIFIERS = getComponentField(dataComponentsClass, "MAX_DAMAGE_MODIFIERS");

        } catch (ClassNotFoundException e) {
            LOGGER.info("[UnstripLog] Bonded not found, skipping compat.");
        } catch (Exception e) {
            LOGGER.error("[UnstripLog] Failed to load Bonded compat.", e);
        }
    }

    private static DataComponentType<?> getComponentField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getField(fieldName);

        Object supplier = field.get(null);

        Method getMethod = supplier.getClass().getDeclaredMethod("get");
        getMethod.setAccessible(true);

        return (DataComponentType<?>) getMethod.invoke(supplier);
    }

    public static void removeComponents(ItemStack stack) {
        if (ITEM_LEVEL_CONTAINER != null) {
            stack.remove(ITEM_LEVEL_CONTAINER);
        }

        if (APPLIED_BONUSES_CONTAINER != null) {
            stack.remove(APPLIED_BONUSES_CONTAINER);
        }

        if (MAX_DAMAGE_MODIFIERS != null) {
            stack.remove(MAX_DAMAGE_MODIFIERS);
        }
    }
}