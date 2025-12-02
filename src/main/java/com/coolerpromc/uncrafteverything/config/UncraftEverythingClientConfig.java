package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.util.Status;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class UncraftEverythingClientConfig {
    public static final UncraftEverythingClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.BooleanValue autoMoveToInventory;
    public final ModConfigSpec.IntValue noRecipeFoundColor;
    public final ModConfigSpec.IntValue noSuitableOutputSlotColor;
    public final ModConfigSpec.IntValue notEnoughExpColor;
    public final ModConfigSpec.IntValue notEnoughInputItemColor;
    public final ModConfigSpec.IntValue notEmptyShulkerColor;
    public final ModConfigSpec.IntValue restrictedByConfigColor;
    public final ModConfigSpec.IntValue damagedItemColor;
    public final ModConfigSpec.IntValue enchantedItemColor;
    public final ModConfigSpec.IntValue lockedItemColor;
    public final ModConfigSpec.IntValue progressionNotDefinedColor;

    static {
        Pair<UncraftEverythingClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(UncraftEverythingClientConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingClientConfig(ModConfigSpec.Builder builder){
        builder.push("AutoMove");
        autoMoveToInventory = builder.comment("Auto move uncrafted items to player inventory, drops to world if inventory is full.").define("autoMoveToInventory", true);
        builder.pop();

        builder.push("StatusColor");
        noRecipeFoundColor = builder.comment("Overlay color for No Recipe Found").defineInRange("noRecipeFound", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        noSuitableOutputSlotColor = builder.comment("Overlay color for No Suitable Output Slot").defineInRange("noSuitableOutputSlotColor", 0xFFfc8b49, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEnoughExpColor = builder.comment("Overlay color for Not Enough Exp").defineInRange("notEnoughExpColor", 0xFFf2ff7a, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEnoughInputItemColor = builder.comment("Overlay color for Not Enough Input Item").defineInRange("notEnoughInputItemColor", 0xFFffef40, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEmptyShulkerColor = builder.comment("Overlay color for Not Empty Shulker").defineInRange("notEmptyShulkerColor", 0xFFe48aff, Integer.MIN_VALUE, Integer.MAX_VALUE);
        restrictedByConfigColor = builder.comment("Overlay color for Restricted By Config").defineInRange("restrictedByConfigColor", 0xFF4f4f4f, Integer.MIN_VALUE, Integer.MAX_VALUE);
        damagedItemColor = builder.comment("Overlay color for Damaged Item").defineInRange("damagedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        enchantedItemColor = builder.comment("Overlay color for Enchanted Item").defineInRange("enchantedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        lockedItemColor = builder.comment("Overlay color for Locked Item").defineInRange("lockedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        progressionNotDefinedColor = builder.comment("Overlay color for Progression Not Defined").defineInRange("progressionNotDefinedColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();
    }

    public void onChanged(){
        Status.NO_RECIPE_FOUND.setOverlay(noRecipeFoundColor.getAsInt());
        Status.NO_SUITABLE_OUTPUT_SLOT.setOverlay(noSuitableOutputSlotColor.getAsInt());
        Status.NOT_ENOUGH_EXP.setOverlay(notEnoughExpColor.getAsInt());
        Status.NOT_ENOUGH_INPUT_ITEM.setOverlay(notEnoughInputItemColor.getAsInt());
        Status.NOT_EMPTY_SHULKER.setOverlay(notEmptyShulkerColor.getAsInt());
        Status.RESTRICTED_BY_CONFIG.setOverlay(restrictedByConfigColor.getAsInt());
        Status.DAMAGED_ITEM.setOverlay(damagedItemColor.getAsInt());
        Status.ENCHANTED_ITEM.setOverlay(enchantedItemColor.getAsInt());
        Status.LOCKED_ITEM.setOverlay(lockedItemColor.getAsInt());
        Status.PROGRESSION_NOT_DEFINED.setOverlay(progressionNotDefinedColor.getAsInt());
    }
}
