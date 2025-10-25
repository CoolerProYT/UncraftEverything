package com.coolerpromc.uncrafteverything.util;

public enum Status {
    NO_RECIPE_FOUND(0, "screen.uncrafteverything.no_recipe_found"),
    NO_SUITABLE_OUTPUT_SLOT(1, "screen.uncrafteverything.no_suitable_output_slot"),
    NOT_ENOUGH_EXP(2, "screen.uncrafteverything.not_enough_exp"),
    NOT_ENOUGH_INPUT_ITEM(3, "screen.uncrafteverything.not_enough_input"),
    NOT_EMPTY_SHULKER(4, "screen.uncrafteverything.not_empty_shulker"),
    RESTRICTED_BY_CONFIG(5, "screen.uncrafteverything.restricted_by_config"),
    DAMAGED_ITEM(6, "screen.uncrafteverything.damaged_item"),
    ENCHANTED_ITEM(7, "screen.uncrafteverything.enchanted_item"),
    LOCKED_ITEM(8, "screen.uncrafteverything.locked_item"),
    PROGRESSION_NOT_DEFINED(9, "screen.uncrafteverything.progression_not_defined"),
    BLANK(-1, "screen.uncrafteverything.blank");

    private final int index;
    private final String translationKey;

    Status(int index, String translationKey){
        this.index = index;
        this.translationKey = translationKey;
    }

    public int getIndex() {
        return index;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static Status byIndex(int index){
        for (Status status : Status.values()){
            if (status.index == index){
                return status;
            }
        }
        return BLANK;
    }
}