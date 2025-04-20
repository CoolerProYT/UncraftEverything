package com.coolerpromc.uncrafteverything.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
        tag.put("input", input.encodeAllowEmpty(provider));
        NbtList listTag = new NbtList();
        for (ItemStack itemStack : outputs) {
            NbtCompound itemTag = new NbtCompound();
            itemTag.put("output", itemStack.encodeAllowEmpty(provider));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup provider) {
        ItemStack input = ItemStack.fromNbtOrEmpty(provider, tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", NbtElement.LIST_TYPE)) {
            NbtList listTag = tag.getList("outputs", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.fromNbtOrEmpty(provider, itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}