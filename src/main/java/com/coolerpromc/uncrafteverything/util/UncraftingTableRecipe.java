package com.coolerpromc.uncrafteverything.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final PacketCodec<RegistryByteBuf, UncraftingTableRecipe> STREAM_CODEC = PacketCodec.tuple(
            ItemStack.OPTIONAL_PACKET_CODEC,
            UncraftingTableRecipe::getInput,
            ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList()),
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

    public NbtCompound serializeNbt(RegistryWrapper.WrapperLookup provider) {
        NbtCompound tag = new NbtCompound();
        tag.put("input", input.toNbt(provider));
        NbtList listTag = new NbtList();
        for (ItemStack itemStack : outputs) {
            itemStack = itemStack.isEmpty() ? new ItemStack(Items.STRUCTURE_VOID) : itemStack.copy();
            NbtCompound itemTag = new NbtCompound();
            itemTag.put("output", itemStack.toNbt(provider));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup provider) {
        ItemStack input = ItemStack.fromNbt(provider, tag.getCompoundOrEmpty("input")).orElse(ItemStack.EMPTY);
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs")) {
            NbtList listTag = tag.getListOrEmpty("outputs");
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound itemTag = listTag.getCompoundOrEmpty(i);
                outputs.add(ItemStack.fromNbt(provider, itemTag.getCompoundOrEmpty("output")).filter(itemStack -> itemStack.getItem() != Items.STRUCTURE_VOID).orElse(ItemStack.EMPTY));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}