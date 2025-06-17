package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;

public class UncraftingRecipeCategory implements DisplayCategory<UncraftingRecipeDisplay> {
    @Override
    public CategoryIdentifier<? extends UncraftingRecipeDisplay> getCategoryIdentifier() {
        return UncraftingRecipeDisplay.CATEGORY_IDENTIFIER;
    }

    @Override
    public ITextComponent getTitle() {
        return new TranslationTextComponent("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(UEBlocks.UNCRAFTING_TABLE.get());
    }

    @Override
    public int getDisplayWidth(UncraftingRecipeDisplay display) {
        return 146;
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
        bounds.setSize(135, 56);
        widgets.add(Widgets.createSlotBackground(new Point(bounds.x + 10, bounds.y + 24)));
        for (int i = 0; i < 9; i ++){
            widgets.add(Widgets.createSlotBackground(new Point(bounds.x + 72 + 18 * (i % 3), bounds.y + 6 + (i / 3) * 18)));
        }
        widgets.add(Widgets.createSlot(new Point(bounds.x + 10, bounds.y + 24)).entries(display.getInputEntries().get(0)).markInput());
        for (int i = 0; i < display.getOutputEntries().size(); i ++){
            widgets.add(Widgets.createSlot(new Point(bounds.x + 72 + 18 * (i % 3), bounds.y + 6 + (i / 3) * 18)).entries(display.getOutputEntries().get(i)).markOutput());
        }
        return widgets;
    }
}
