package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
        tag.put("input", input.save(new CompoundTag()));
        ListTag listTag = new ListTag();
        for (ItemStack itemStack : outputs) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("output", itemStack.save(new CompoundTag()));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static UncraftingTableRecipe deserializeNbt(CompoundTag tag) {
        ItemStack input = ItemStack.of(tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("outputs", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.of(itemTag.getCompound("output")));
            }
        }

        return new UncraftingTableRecipe(input, outputs);
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
