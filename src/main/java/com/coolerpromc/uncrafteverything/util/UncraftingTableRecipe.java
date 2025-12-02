package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

    public boolean contains(Tuple<Item, CompoundTag> tuple){
        for (ItemStack output : this.outputs){
            if (ItemStack.isSameItemSameTags(output, new ItemStack(tuple.getA(), 1, tuple.getB()))){
                return true;
            }
        }
        return false;
    }

    public int indexOf(Tuple<Item, CompoundTag> tuple){
        for (int i = 0;i < this.outputs.size();i++){
            ItemStack output = this.outputs.get(i);
            if (ItemStack.isSameItemSameTags(output, new ItemStack(tuple.getA(), 1, tuple.getB()))){
                return i;
            }
        }
        return -1;
    }

    public ItemStack getStack(Tuple<Item, CompoundTag> tuple){
        for (ItemStack output : this.outputs) {
            if (ItemStack.isSameItemSameTags(output, new ItemStack(tuple.getA(), 1, tuple.getB()))) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public void writeToBuf(FriendlyByteBuf packetByteBuf){
        packetByteBuf.writeBoolean(!this.getInput().isEmpty());
        if (!this.getInput().isEmpty()) {
            packetByteBuf.writeItem(this.getInput());
        }

        packetByteBuf.writeVarInt(this.getOutputs().size());
        for (var output : this.getOutputs()) {
            packetByteBuf.writeBoolean(!output.isEmpty());
            if (!output.isEmpty()) {
                packetByteBuf.writeItem(output);
            }
        }
    }

    public static UncraftingTableRecipe readFromBuf(FriendlyByteBuf packetByteBuf){
        ItemStack input = ItemStack.EMPTY;
        if (packetByteBuf.readBoolean()) {
            input = packetByteBuf.readItem();
        }

        int count = packetByteBuf.readVarInt();
        List<ItemStack> outputs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ItemStack output = ItemStack.EMPTY;
            if (packetByteBuf.readBoolean()) {
                output = packetByteBuf.readItem();
            }
            outputs.add(output);
        }

        return new UncraftingTableRecipe(input, outputs);
    }
}
