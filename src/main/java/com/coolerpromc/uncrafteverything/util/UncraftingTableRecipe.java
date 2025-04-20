package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

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

    public NbtCompound serializeNbt() {
        NbtCompound tag = new NbtCompound();
        tag.put("input", input.writeNbt(new NbtCompound()));
        NbtList listTag = new NbtList();
        for (ItemStack itemStack : outputs) {
            NbtCompound itemTag = new NbtCompound();
            itemTag.put("output", itemStack.writeNbt(new NbtCompound()));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(NbtCompound tag) {
        ItemStack input = ItemStack.fromNbt(tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", NbtElement.LIST_TYPE)) {
            NbtList listTag = tag.getList("outputs", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.fromNbt(itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}