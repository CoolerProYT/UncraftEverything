package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.util.Status;

public class UncraftEverythingClientConfig {
    public static final UncraftEverythingClientConfig CONFIG = new UncraftEverythingClientConfig();
    private final ConfigSpec CONFIG_SPEC;

    public final ConfigValue<Boolean> autoMoveToInventory;
    public final ConfigValue<Integer> noRecipeFoundColor;
    public final ConfigValue<Integer> noSuitableOutputSlotColor;
    public final ConfigValue<Integer> notEnoughExpColor;
    public final ConfigValue<Integer> notEnoughInputItemColor;
    public final ConfigValue<Integer> notEmptyShulkerColor;
    public final ConfigValue<Integer> restrictedByConfigColor;
    public final ConfigValue<Integer> damagedItemColor;
    public final ConfigValue<Integer> enchantedItemColor;
    public final ConfigValue<Integer> lockedItemColor;
    public final ConfigValue<Integer> progressionNotDefinedColor;

    public static void init(){
    }

    private UncraftEverythingClientConfig(){
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.TOML).side(ConfigSide.CLIENT).comment("UncraftEverything Client Configuration");

        autoMoveToInventory = builder.defineBoolean("Behaviour.autoMoveToInventory", true, "Auto move uncrafted items to player inventory, drops to world if inventory is full.");
        noRecipeFoundColor = builder.defineInt("StatusColor.noRecipeFound", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for No Recipe Found");
        noSuitableOutputSlotColor = builder.defineInt("StatusColor.noSuitableOutputSlotColor", 0xFFfc8b49, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for No Suitable Output Slot");
        notEnoughExpColor = builder.defineInt("StatusColor.notEnoughExpColor", 0xFFf2ff7a, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Not Enough Exp");
        notEnoughInputItemColor = builder.defineInt("StatusColor.notEnoughInputItemColor", 0xFFffef40, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Not Enough Input Item");
        notEmptyShulkerColor = builder.defineInt("StatusColor.notEmptyShulkerColor", 0xFFe48aff, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Not Empty Shulker");
        restrictedByConfigColor = builder.defineInt("StatusColor.restrictedByConfigColor", 0xFF4f4f4f, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Restricted By Config");
        damagedItemColor = builder.defineInt("StatusColor.damagedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Damaged Item");
        enchantedItemColor = builder.defineInt("StatusColor.enchantedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Enchanted Item");
        lockedItemColor = builder.defineInt("StatusColor.lockedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Locked Item");
        progressionNotDefinedColor = builder.defineInt("StatusColor.progressionNotDefinedColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE, "Overlay color for Progression Not Defined");
        CONFIG_SPEC = builder.watchForChanges().build();

        CONFIG_SPEC.addReloadListener(() -> {
            Status.NO_RECIPE_FOUND.setOverlay(noRecipeFoundColor());
            Status.NO_SUITABLE_OUTPUT_SLOT.setOverlay(noSuitableOutputSlotColor());
            Status.NOT_ENOUGH_EXP.setOverlay(notEnoughExpColor());
            Status.NOT_ENOUGH_INPUT_ITEM.setOverlay(notEnoughInputItemColor());
            Status.NOT_EMPTY_SHULKER.setOverlay(notEmptyShulkerColor());
            Status.RESTRICTED_BY_CONFIG.setOverlay(restrictedByConfigColor());
            Status.DAMAGED_ITEM.setOverlay(damagedItemColor());
            Status.ENCHANTED_ITEM.setOverlay(enchantedItemColor());
            Status.LOCKED_ITEM.setOverlay(lockedItemColor());
            Status.PROGRESSION_NOT_DEFINED.setOverlay(progressionNotDefinedColor());
        });
    }

    public boolean autoMoveToInventory() { return autoMoveToInventory.get(); }
    public int noRecipeFoundColor() { return noRecipeFoundColor.get(); }
    public int noSuitableOutputSlotColor() { return noSuitableOutputSlotColor.get(); }
    public int notEnoughExpColor() { return notEnoughExpColor.get(); }
    public int notEnoughInputItemColor() { return notEnoughInputItemColor.get(); }
    public int notEmptyShulkerColor() { return notEmptyShulkerColor.get(); }
    public int restrictedByConfigColor() { return restrictedByConfigColor.get(); }
    public int damagedItemColor() { return damagedItemColor.get(); }
    public int enchantedItemColor() { return enchantedItemColor.get(); }
    public int lockedItemColor() { return lockedItemColor.get(); }
    public int progressionNotDefinedColor() { return progressionNotDefinedColor.get(); }

    public void save(){
        CONFIG_SPEC.save();
    }
}