package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UncraftingRecipeCategory implements RecipeCategory<UncraftingRecipeDisplay> {
    public static Identifier ID = new Identifier(UncraftEverything.MODID, "plugins/uncrafting_table");

    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(UEBlocks.UNCRAFTING_TABLE);
    }

    @Override
    public int getDisplayWidth(UncraftingRecipeDisplay display) {
        return 146;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return ID;
    }

    @Override
    public @NotNull String getCategoryName() {
        return new TranslatableText("block.uncrafteverything.uncrafting_table").getString();
    }

    @Override
    public int getDisplayHeight() {
        return 64;
    }

    @Override
    public List<Widget> setupDisplay(UncraftingRecipeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        widgets.add(Widgets.createCategoryBase(new Rectangle(bounds.x, bounds.y, 135, 64)));
        widgets.add(Widgets.createArrow(new Point(bounds.x + 35, bounds.y + 24)));
        bounds.setSize(146, 56);
        widgets.add(Widgets.createSlotBackground(new Point(bounds.x + 10, bounds.y + 24)));
        for (int i = 0; i < 9; i ++){
            widgets.add(Widgets.createSlotBackground(new Point(bounds.x + 72 + 18 * (i % 3), bounds.y + 6 + (i / 3) * 18)));
        }
        widgets.add(Widgets.createSlot(new Point(bounds.x + 10, bounds.y + 24)).entries(Collections.singleton(display.getInputEntries().get(0).get(0))).markInput());
        for (int i = 0; i < display.getResultingEntries().size(); i ++){
            if (display.getResultingEntries().get(i) != null){
                widgets.add(Widgets.createSlot(new Point(bounds.x + 72 + 18 * (i % 3), bounds.y + 6 + (i / 3) * 18)).entries(display.getResultingEntries().get(i)).markOutput());
            }
        }
        return widgets;
    }
}
