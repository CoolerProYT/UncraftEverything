package com.coolerpromc.uncrafteverything.util;

import java.lang.reflect.Field;

public class ModCompatibilities {
    public static int getMaxStackSize(){
        String stackableItems = "com.dplayend.stackableitems.handler.HandlerConfig$Server";
        String fieldName = "defaultStackSize";
        try{
            Class<?> handlerConfigClass = Class.forName("com.dplayend.stackableitems.handler.HandlerConfig");

            Field serverField = handlerConfigClass.getDeclaredField("SERVER");
            serverField.setAccessible(true);
            Object serverInstance = serverField.get(null); // because it's static

            Class<?> serverClass = serverField.getType();
            Field defaultStackSizeField = serverClass.getDeclaredField("defaultStackSize");
            defaultStackSizeField.setAccessible(true);
            Object value = defaultStackSizeField.get(serverInstance);

            return (int) value;
        } catch (Exception e) {
            return -1;
        }
    }
}
