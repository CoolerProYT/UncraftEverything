package com.coolerpromc.uncrafteverything.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class JEIUncraftingTableRecipe {
    private final ItemStack input;
    private final List<Ingredient> outputs = new ArrayList<>();

    public JEIUncraftingTableRecipe(ItemStack input, List<Ingredient> outputs) {
        this.input = input;
        this.outputs.addAll(outputs);
    }

    public ItemStack getInput() {
        return input;
    }

    public List<Ingredient> getOutputs() {
        return outputs;
    }

    /*public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("input", input.save(new CompoundTag()));
        ListTag listTag = new ListTag();
        for (Ingredient itemStack : outputs) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("output", itemStack.save(new CompoundTag()));
            listTag.add(itemTag);
        }
        tag.put("outputs", listTag);
        return tag;
    }

    public static JEIUncraftingTableRecipe deserializeNbt(CompoundTag tag) {
        ItemStack input = ItemStack.of(tag.getCompound("input"));
        List<ItemStack> outputs = new ArrayList<>();

        if (tag.contains("outputs", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("outputs", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                outputs.add(ItemStack.of(itemTag.getCompound("output")));
            }
        }

        return new JEIUncraftingTableRecipe(input, outputs);
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

    public static JEIUncraftingTableRecipe readFromBuf(FriendlyByteBuf packetByteBuf){
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

        return new JEIUncraftingTableRecipe(input, outputs);
    }*/
}
