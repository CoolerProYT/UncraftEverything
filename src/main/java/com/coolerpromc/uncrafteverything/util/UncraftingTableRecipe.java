package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final Codec<UncraftingTableRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("input").forGetter(UncraftingTableRecipe::getInput),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("outputs").forGetter(UncraftingTableRecipe::getOutputs)
    ).apply(instance, UncraftingTableRecipe::new));
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

    public void addOutput(ItemStack output) {
        outputs.add(output);
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

    public boolean contains(Pair<Item, ComponentChanges> tuple){
        for (ItemStack output : this.outputs){
            if (ItemStack.areItemsAndComponentsEqual(output, new ItemStack(tuple.getLeft().getRegistryEntry(), 1, tuple.getRight()))){
                return true;
            }
        }
        return false;
    }

    public int indexOf(Pair<Item, ComponentChanges> tuple){
        for (int i = 0;i < this.outputs.size();i++){
            ItemStack output = this.outputs.get(i);
            if (ItemStack.areItemsAndComponentsEqual(output, new ItemStack(tuple.getLeft().getRegistryEntry(), 1, tuple.getRight()))){
                return i;
            }
        }
        return -1;
    }

    public ItemStack getStack(Pair<Item, ComponentChanges> tuple){
        for (ItemStack output : this.outputs) {
            if (ItemStack.areItemsAndComponentsEqual(output, new ItemStack(tuple.getLeft().getRegistryEntry(), 1, tuple.getRight()))) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }
}