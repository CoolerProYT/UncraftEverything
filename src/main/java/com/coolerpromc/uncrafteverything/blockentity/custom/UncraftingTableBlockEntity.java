package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class UncraftingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {
    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private PlayerEntity player;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    public UncraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE, pos, state);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ImplementedInventory.super.setStack(slot, stack);
        getOutputStacks();
        if (world != null && !world.isClient() && slot == inputSlots[0]) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            for (ServerPlayerEntity playerEntity : PlayerLookup.around((ServerWorld) world, new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ()), 10)){
                ServerPlayNetworking.send(playerEntity, new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()));
            }
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        getOutputStacks();
        return ImplementedInventory.super.removeStack(slot);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return Arrays.stream(outputSlots).noneMatch(value -> value == slot);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this.player = player;
        return new UncraftingTableMenu(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        Inventories.writeNbt(nbt, inventory, registries);
        NbtList listTag = new NbtList();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            NbtCompound recipeTag = new NbtCompound();
            recipeTag.put("recipe", recipe.serializeNbt(registries));
            listTag.add(recipeTag);
        }
        nbt.put("current_recipes", listTag);
        if (currentRecipe != null) {
            nbt.put("current_recipe", currentRecipe.serializeNbt(registries));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        Inventories.readNbt(nbt, inventory, registries);
        if (nbt.contains("current_recipes", NbtList.LIST_TYPE)){
            NbtList listTag = nbt.getList("current_recipes", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe"), registries));
            }
        }
        if (nbt.contains("current_recipe", NbtElement.COMPOUND_TYPE)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(nbt.getCompound("current_recipe"), registries);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
    }

    public void getOutputStacks() {
        if (!(world instanceof ServerWorld serverLevel)) return;

        if (this.getStack(inputSlots[0]).isEmpty() || this.getStack(inputSlots[0]).getDamage() > 0) {
            System.out.println("Empty or damaged item in input slot");
            currentRecipes.clear();
            currentRecipe = null;
            markDirty();
            if (world != null && !world.isClient) {
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
            }
            return;
        }

        ItemStack inputStack = this.getStack(inputSlots[0]);

        List<RecipeEntry<?>> recipes = serverLevel.getRecipeManager().values().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            return false;
        }).toList();

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        for (RecipeEntry<?> r : recipes) {
            if (r.value() instanceof ShapedRecipe shapedRecipe) {
                // Get all possible combinations of ingredients
                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(shapedRecipe.getIngredients());

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem(), shapedRecipe.result.getCount()));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()),
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
                            if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                                ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                                outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()),
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
            this.currentRecipe = outputs.get(0);
        }
    }

    private List<Item> getItemsFromIngredient(Ingredient ingredient) {
        List<Item> items = new ArrayList<>();

        // Handle tag ingredients
        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().getMatchingItems().toList().isEmpty()) {
            for (var holder : ingredient.getCustomIngredient().getMatchingItems().toList()) {
                items.add(holder.value());
            }
        }
        // Handle regular item ingredients
        else {
            try {
                items = ingredient.getMatchingItems().toList().stream()
                        .map(RegistryEntry::value)
                        .distinct()
                        .toList();
            } catch (IllegalStateException e) {
                // Log error for debugging
                LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
                return Collections.emptyList();
            }
        }

        return items.stream()
                .sorted(Comparator.comparing(Item::getTranslationKey))
                .toList();
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(List<Optional<Ingredient>> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = ingredients.get(i);
            List<Item> items = optIngredient.map(ingredient -> {
                        List<Item> ingredientItems = getItemsFromIngredient(ingredient);
                        return ingredientItems.isEmpty() ? List.of(Items.AIR) : ingredientItems;
                    })
                    .orElse(List.of(Items.AIR));

            /*if (optIngredient.isEmpty()) {
                items = List.of(Items.AIR);
            } else {
                Ingredient ingredient = optIngredient.get();
                items = ingredient.getValues().stream()
                        .map(Holder::value)
                        .sorted(Comparator.comparing(Item::getDescriptionId))
                        .toList();
            }*/

            String key = items.stream()
                    .map(Item::getTranslationKey)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Item> finalItems = items;
            Group group = groupKeyToGroup.computeIfAbsent(key, k -> new Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        List<Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Item>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Item>> product = cartesianProduct(groupChoices);

        List<List<Item>> combinations = new ArrayList<>();

        for (List<Item> choiceList : product) {
            Item[] itemsArray = new Item[ingredients.size()];
            Arrays.fill(itemsArray, Items.AIR);

            for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
                Group group = groups.get(groupIdx);
                Item chosenItem = choiceList.get(groupIdx);
                for (int pos : group.positions) {
                    if (pos >= 0 && pos < itemsArray.length) {
                        itemsArray[pos] = chosenItem;
                    }
                }
            }

            combinations.add(Arrays.asList(itemsArray));
        }

        return combinations;
    }

    // Helper method to get all possible combinations of ingredients for shapeless recipes
    private List<List<Item>> getAllShapelessIngredientCombinations(List<Ingredient> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            List<Item> items = getItemsFromIngredient(ingredient);
            if (items.isEmpty()) items = List.of(Items.AIR);

            String key = items.stream()
                    .map(Item::getTranslationKey)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Item> finalItems = items;
            Group group = groupKeyToGroup.computeIfAbsent(key, k -> new Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        List<Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Item>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Item>> product = cartesianProduct(groupChoices);

        List<List<Item>> combinations = new ArrayList<>();

        for (List<Item> choiceList : product) {
            Item[] itemsArray = new Item[ingredients.size()];
            Arrays.fill(itemsArray, Items.AIR);

            for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
                Group group = groups.get(groupIdx);
                Item chosenItem = choiceList.get(groupIdx);
                for (int pos : group.positions) {
                    if (pos >= 0 && pos < itemsArray.length) {
                        itemsArray[pos] = chosenItem;
                    }
                }
            }

            combinations.add(Arrays.asList(itemsArray));
        }

        return combinations;
    }

    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        if (lists.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        List<T> firstList = lists.get(0);
        List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));

        for (T item : firstList) {
            for (List<T> remaining : remainingLists) {
                List<T> combination = new ArrayList<>();
                combination.add(item);
                combination.addAll(remaining);
                result.add(combination);
            }
        }

        return result;
    }

    private boolean isSameItemCombination(List<Item> combination) {
        Item firstItem = null;
        for (Item item : combination) {
            if (item != Items.AIR) {
                if (firstItem == null) {
                    firstItem = item;
                } else if (item != firstItem) {
                    return false;
                }
            }
        }
        return true;
    }

    public void handleButtonClick(String data){
        if (hasRecipe()){
            List<ItemStack> outputs = currentRecipe.getOutputs();

            for (int i = 0; i < outputs.size(); i++) {
                ItemStack output = outputs.get(i);
                if (i < outputSlots.length) {
                    ItemStack slotStack = this.getStack(outputSlots[i]);

                    if (slotStack.isEmpty()) {
                        this.setStack(outputSlots[i], output.copy());
                    } else if (ItemStack.areItemsAndComponentsEqual(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxCount()) {
                        slotStack.increment(output.getCount());
                        this.setStack(outputSlots[i], slotStack);
                    }
                }
            }

            this.removeStack(0, this.currentRecipe.getInput().getCount());
            markDirty();

            getOutputStacks();
            if (world != null && !world.isClient()) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                for (ServerPlayerEntity playerEntity : PlayerLookup.around((ServerWorld) world, new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ()), 10)){
                    ServerPlayNetworking.send(playerEntity, new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()));
                }
            }
        }
        else{
            this.player.sendMessage(Text.literal("No recipe or output slot found!"), true);
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
            if (i >= outputSlots.length) return false;

            ItemStack slotStack = this.getStack(this.outputSlots[i]);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.areItemsAndComponentsEqual(slotStack, result)) {
                return false;
            }

            if (slotStack.getCount() + result.getCount() > slotStack.getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    private static class Group {
        List<Integer> positions;
        List<Item> items;

        Group(List<Integer> positions, List<Item> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}
