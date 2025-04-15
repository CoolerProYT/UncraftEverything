package com.coolerpromc.uncrafteverything.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingTableRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            UncraftingTableRecipe::getInput,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
            UncraftingTableRecipe::getOutputs,
            UncraftingTableRecipe::new
    );

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

    public CompoundTag serializeNbt(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("input", input.saveOptional(provider));
        ListTag listTag = new ListTag();
        for (ItemStack itemStack : outputs) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("output", itemStack.saveOptional(provider));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(CompoundTag tag, HolderLookup.Provider provider) {
        ItemStack input = ItemStack.parseOptional(provider, tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("outputs", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.parseOptional(provider, itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}
