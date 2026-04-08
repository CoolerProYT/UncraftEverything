package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.util.Status;

public class UncraftEverythingClientConfig {
    public static boolean autoMoveToInventory = true;
    public static int noRecipeFoundColor = 0xFFff615c;
    public static int noSuitableOutputSlotColor = 0xFFfc8b49;
    public static int notEnoughExpColor = 0xFFf2ff7a;
    public static int notEnoughInputItemColor = 0xFFffef40;
    public static int notEmptyShulkerColor = 0xFFe48aff;
    public static int restrictedByConfigColor = 0xFF4f4f4f;
    public static int damagedItemColor = 0xFFff615c;
    public static int enchantedItemColor = 0xFFff615c;
    public static int lockedItemColor = 0xFFff615c;
    public static int progressionNotDefinedColor = 0xFFff615c;

    public static void updateCache(boolean autoMove, int noRecipeFound, int noSuitableOutputSlot, int notEnoughExp, int notEnoughInputItem, int notEmptyShulker, int restrictedByConfig, int damagedItem, int enchantedItem, int lockedItem, int progressionNotDefined) {
        autoMoveToInventory = autoMove;
        noRecipeFoundColor = noRecipeFound;
        noSuitableOutputSlotColor = noSuitableOutputSlot;
        notEnoughExpColor = notEnoughExp;
        notEnoughInputItemColor = notEnoughInputItem;
        notEmptyShulkerColor = notEmptyShulker;
        restrictedByConfigColor = restrictedByConfig;
        damagedItemColor = damagedItem;
        enchantedItemColor = enchantedItem;
        lockedItemColor = lockedItem;
        progressionNotDefinedColor = progressionNotDefined;
        onChanged();
    }

    public static void onChanged() {
        Status.NO_RECIPE_FOUND.setOverlay(noRecipeFoundColor);
        Status.NO_SUITABLE_OUTPUT_SLOT.setOverlay(noSuitableOutputSlotColor);
        Status.NOT_ENOUGH_EXP.setOverlay(notEnoughExpColor);
        Status.NOT_ENOUGH_INPUT_ITEM.setOverlay(notEnoughInputItemColor);
        Status.NOT_EMPTY_SHULKER.setOverlay(notEmptyShulkerColor);
        Status.RESTRICTED_BY_CONFIG.setOverlay(restrictedByConfigColor);
        Status.DAMAGED_ITEM.setOverlay(damagedItemColor);
        Status.ENCHANTED_ITEM.setOverlay(enchantedItemColor);
        Status.LOCKED_ITEM.setOverlay(lockedItemColor);
        Status.PROGRESSION_NOT_DEFINED.setOverlay(progressionNotDefinedColor);
    }
}