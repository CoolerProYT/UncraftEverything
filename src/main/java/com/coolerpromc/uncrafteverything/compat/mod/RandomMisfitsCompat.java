package com.coolerpromc.uncrafteverything.compat.mod;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class RandomMisfitsCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("unstriplog");

    private static DataComponentType<?> RANGE_KEY;
    private static DataComponentType<?> MODE_KEY;
    private static DataComponentType<?> FELLING_KEY;

    static {
        try {
            Class<?> modComponents = Class.forName("com.jahirtrap.randomisfits.init.ModComponents");
            RANGE_KEY   = getComponentField(modComponents, "RANGE_KEY");
            MODE_KEY    = getComponentField(modComponents, "MODE_KEY");
            FELLING_KEY = getComponentField(modComponents, "FELLING_KEY");
        } catch (ClassNotFoundException e) {
            LOGGER.info("[UnstripLog] RandomMisfits not found, skipping compat.");
        } catch (Exception e) {
            LOGGER.error("[UnstripLog] Failed to load RandomMisfits compat.", e);
        }
    }

    private static DataComponentType<?> getComponentField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getField(fieldName); // use getField() for public static fields
        field.setAccessible(true);
        return (DataComponentType<?>) field.get(null);
    }

    public static void removeComponent(ItemStack inputStack) {
        if (RANGE_KEY != null)   inputStack.remove(RANGE_KEY);
        if (MODE_KEY != null)    inputStack.remove(MODE_KEY);
        if (FELLING_KEY != null) inputStack.remove(FELLING_KEY);
    }
}