package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final Codec<UncraftingTableRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("input").forGetter(UncraftingTableRecipe::getInput),
            ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(UncraftingTableRecipe::getOutputs)
    ).apply(instance, UncraftingTableRecipe::new));

    public UncraftingTableRecipe(ItemStack input) {
        this.input = input;
    }

    public UncraftingTableRecipe(ItemStack input, List<ItemStack> outputs) {
        this.input = input;
        this.outputs.addAll(outputs);
    }

    public boolean addOutput(ItemStack output) {
        return outputs.add(output);
    }

    public void setOutput(int index, ItemStack output) {
        outputs.set(index, output);
    }

    public ItemStack getInput() {
        return input;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("input", input.toTag(new CompoundTag()));
        ListTag listTag = new ListTag();
        for (ItemStack itemStack : outputs) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("output", itemStack.toTag(new CompoundTag()));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(CompoundTag tag) {
        ItemStack input = ItemStack.fromTag(tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", 9)) {
            ListTag listTag = tag.getList("outputs", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.fromTag(itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}