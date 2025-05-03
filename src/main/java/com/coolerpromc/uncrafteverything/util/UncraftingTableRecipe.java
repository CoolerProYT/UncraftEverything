package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final Codec<UncraftingTableRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("input").forGetter(UncraftingTableRecipe::getInput),
            Codec.list(ItemStack.CODEC).fieldOf("outputs").forGetter(UncraftingTableRecipe::getOutputs)
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

    public CompoundNBT serializeNbt() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("input", input.save(new CompoundNBT()));
        ListNBT listTag = new ListNBT();
        for (ItemStack itemStack : outputs) {
            CompoundNBT itemTag = new CompoundNBT();
            itemTag.put("output", itemStack.save(new CompoundNBT()));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(CompoundNBT tag) {
        ItemStack input = ItemStack.of(tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", Constants.NBT.TAG_LIST)) {
            ListNBT listTag = tag.getList("outputs", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundNBT itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.of(itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}
