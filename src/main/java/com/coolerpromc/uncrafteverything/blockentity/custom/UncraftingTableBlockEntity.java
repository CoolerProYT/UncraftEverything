package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class UncraftingTableBlockEntity extends BlockEntity implements MenuProvider {
    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private Player player;

    private final ItemStackHandler inputHandler = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            getOutputStacks();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                PacketDistributor.sendToPlayersNear((ServerLevel) level, null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 10, new UncraftingTableDataPayload(getBlockPos(), currentRecipes));
            }
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        this.player = player;
        return new UncraftingTableMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("input", inputHandler.serializeNBT(registries));
        tag.put("output", outputHandler.serializeNBT(registries));
        ListTag listTag = new ListTag();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            CompoundTag recipeTag = new CompoundTag();
            recipeTag.put("recipe", recipe.serializeNbt(registries));
            listTag.add(recipeTag);
        }
        tag.put("current_recipes", listTag);
        if (currentRecipe != null) {
            tag.put("current_recipe", currentRecipe.serializeNbt(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        inputHandler.deserializeNBT(registries, tag.getCompound("input"));
        outputHandler.deserializeNBT(registries, tag.getCompound("output"));
        if (tag.contains("current_recipes", ListTag.TAG_LIST)){
            ListTag listTag = tag.getList("current_recipes", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe"), registries));
            }
        }
        if (tag.contains("current_recipe", Tag.TAG_COMPOUND)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(tag.getCompound("current_recipe"), registries);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (inputHandler.getStackInSlot(0).isEmpty()) {
            currentRecipes.clear();
            currentRecipe = null;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            return;
        }

        ItemStack inputStack = this.inputHandler.getStackInSlot(0);

        List<RecipeHolder<?>> recipes = serverLevel.recipeAccess().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            return false;
        }).toList();

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        for (RecipeHolder<?> r : recipes) {
            if (r.value() instanceof ShapedRecipe shapedRecipe) {
                // Get all possible combinations of ingredients
                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(shapedRecipe.getIngredients());

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem(), shapedRecipe.result.getCount()));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()),
                                    new ItemStack(stack.getItem(), stack.getCount() + 1));
                        } else {
                            outputStack.addOutput(new ItemStack(item, 1));
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe) {
                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(shapelessRecipe.ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem(), shapelessRecipe.result.getCount()));

                    for (Item item : ingredientCombination) {
                        if (item != Items.AIR) {
                            if (outputStack.getOutputs().contains(item.getDefaultInstance())) {
                                ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                                outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()),
                                        new ItemStack(stack.getItem(), stack.getCount() + 1));
                            } else {
                                outputStack.addOutput(new ItemStack(item, 1));
                            }
                        }
                    }
                    outputs.add(outputStack);
                }
            }
        }

        this.currentRecipes = outputs;

        if (!currentRecipes.isEmpty()) {
            this.currentRecipe = outputs.getFirst();
        }
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(List<Optional<Ingredient>> ingredients) {
        List<List<Item>> result = new ArrayList<>();
        result.add(new ArrayList<>()); // Start with an empty combination

        for (Optional<Ingredient> optIngredient : ingredients) {
            List<List<Item>> newCombinations = new ArrayList<>();

            // If ingredient is not present, add AIR to all existing combinations
            if (optIngredient.isEmpty()) {
                for (List<Item> combination : result) {
                    List<Item> newCombination = new ArrayList<>(combination);
                    newCombination.add(Items.AIR);
                    newCombinations.add(newCombination);
                }
            } else {
                Ingredient ingredient = optIngredient.get();
                List<Item> possibleItems = ingredient.getValues().stream().map(Holder::value).toList();

                // For each existing combination, create new combinations with each possible item
                for (List<Item> combination : result) {
                    if (possibleItems.isEmpty()) {
                        // If no possible items, add AIR
                        List<Item> newCombination = new ArrayList<>(combination);
                        newCombination.add(Items.AIR);
                        newCombinations.add(newCombination);
                    } else {
                        // Add each possible item to create new combinations
                        for (Item itemStack : possibleItems) {
                            List<Item> newCombination = new ArrayList<>(combination);
                            newCombination.add(itemStack);
                            newCombinations.add(newCombination);
                        }
                    }
                }
            }

            result = newCombinations;
        }

        return result;
    }

    // Helper method to get all possible combinations of ingredients for shapeless recipes
    private List<List<Item>> getAllShapelessIngredientCombinations(List<Ingredient> ingredients) {
        List<List<Item>> result = new ArrayList<>();
        result.add(new ArrayList<>()); // Start with an empty combination

        for (Ingredient ingredient : ingredients) {
            List<List<Item>> newCombinations = new ArrayList<>();
            List<Item> possibleItems = ingredient.getValues().stream().map(Holder::value).toList();

            // For each existing combination, create new combinations with each possible item
            for (List<Item> combination : result) {
                if (possibleItems.isEmpty()) {
                    // If no possible items, add AIR
                    List<Item> newCombination = new ArrayList<>(combination);
                    newCombination.add(Items.AIR);
                    newCombinations.add(newCombination);
                } else {
                    // Add each possible item to create new combinations
                    for (Item itemStack : possibleItems) {
                        List<Item> newCombination = new ArrayList<>(combination);
                        newCombination.add(itemStack);
                        newCombinations.add(newCombination);
                    }
                }
            }

            result = newCombinations;
        }

        return result;
    }

    public void handleButtonClick(String data){
        if (hasRecipe()){
            List<ItemStack> outputs = currentRecipe.getOutputs();

            for (int i = 0; i < outputs.size(); i++) {
                ItemStack output = outputs.get(i);
                if (i < outputHandler.getSlots()) {
                    ItemStack slotStack = outputHandler.getStackInSlot(i);

                    if (slotStack.isEmpty()) {
                        outputHandler.setStackInSlot(i, output.copy());
                    } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                        slotStack.grow(output.getCount());
                        outputHandler.setStackInSlot(i, slotStack);
                    }
                }
            }

            inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
            setChanged();

            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
        else{
            this.player.displayClientMessage(Component.literal("No recipe or output slot found!"), true);
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;
    }

    private boolean hasRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (i >= outputHandler.getSlots()) return false;

            ItemStack slotStack = outputHandler.getStackInSlot(i);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(slotStack, result)) {
                return false;
            }

            if (slotStack.getCount() + result.getCount() > slotStack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }
}
