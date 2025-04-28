package com.coolerpromc.uncrafteverything.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PerItemExpCostConfig {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>(){}.getType();

    private static Map<String, Integer> perItemExp = new HashMap<>();
    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "uncrafteverything-exp.json");

    public static void load() {
        if (!CONFIG_FILE.exists()){
            saveDefaults();
        }

        try(FileReader reader = new FileReader(CONFIG_FILE)){
            perItemExp = GSON.fromJson(reader, MAP_TYPE);
        }
        catch (Exception e){
            System.out.println("Failed to load per item exp config!");
        }
    }

    public static void saveDefaults() {
        perItemExp.put("minecraft:diamond_sword", 2);
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(perItemExp, writer);
        } catch (Exception e) {
            System.out.println("Failed to save per item exp config!");
        }
    }

    public static Map<String, Integer> getPerItemExp() {
        return perItemExp;
    }
}
