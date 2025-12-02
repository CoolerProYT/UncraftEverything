package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UncraftingTableRecipe {
    private final ItemStack input;
    private final List<ItemStack> outputs = new ArrayList<>();
    public static final Codec<UncraftingTableRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("input").forGetter(UncraftingTableRecipe::getInput),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("outputs").forGetter(UncraftingTableRecipe::getOutputs)
    ).apply(instance, UncraftingTableRecipe::new));
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

    public boolean contains(Tuple<Item, DataComponentPatch> tuple){
        for (ItemStack output : this.outputs){
            if (ItemStack.isSameItemSameComponents(output, new ItemStack(tuple.getA().builtInRegistryHolder(), 1, tuple.getB()))){
                return true;
            }
        }
        return false;
    }

    public int indexOf(Tuple<Item, DataComponentPatch> tuple){
        for (int i = 0;i < this.outputs.size();i++){
            ItemStack output = this.outputs.get(i);
            if (ItemStack.isSameItemSameComponents(output, new ItemStack(tuple.getA().builtInRegistryHolder(), 1, tuple.getB()))){
                return i;
            }
        }
        return -1;
    }

    public ItemStack getStack(Tuple<Item, DataComponentPatch> tuple){
        for (ItemStack output : this.outputs) {
            if (ItemStack.isSameItemSameComponents(output, new ItemStack(tuple.getA().builtInRegistryHolder(), 1, tuple.getB()))) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }
}
