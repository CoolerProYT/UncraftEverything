package com.coolerpromc.uncrafteverything.util;

public enum Status {
    NO_RECIPE_FOUND(0, "screen.uncrafteverything.no_recipe_found", 0xFFff615c),
    NO_SUITABLE_OUTPUT_SLOT(1, "screen.uncrafteverything.no_suitable_output_slot", 0xFFfc8b49),
    NOT_ENOUGH_EXP(2, "screen.uncrafteverything.not_enough_exp", 0xFFf2ff7a),
    NOT_ENOUGH_INPUT_ITEM(3, "screen.uncrafteverything.not_enough_input", 0xFFffef40),
    NOT_EMPTY_SHULKER(4, "screen.uncrafteverything.not_empty_shulker", 0xFFe48aff),
    RESTRICTED_BY_CONFIG(5, "screen.uncrafteverything.restricted_by_config", 0xFF4f4f4f),
    DAMAGED_ITEM(6, "screen.uncrafteverything.damaged_item", 0xFFff615c),
    ENCHANTED_ITEM(7, "screen.uncrafteverything.enchanted_item", 0xFFff615c),
    LOCKED_ITEM(8, "screen.uncrafteverything.locked_item", 0xFFff615c),
    PROGRESSION_NOT_DEFINED(9, "screen.uncrafteverything.progression_not_defined", 0xFFff615c),
    BLANK(-1, "screen.uncrafteverything.blank", -1);

    private final int index;
    private final String translationKey;
    private int overlay;

    Status(int index, String translationKey, int overlay){
        this.index = index;
        this.translationKey = translationKey;
        this.overlay = overlay;
    }

    public int getIndex() {
        return index;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getOverlay() {
        return overlay;
    }

    public void setOverlay(int overlay) {
        this.overlay = overlay;
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