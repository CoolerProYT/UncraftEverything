package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.UETags;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UncraftingTableDataPayload(getBlockPos(), getCurrentRecipes()));
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
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("input", inputHandler.serializeNBT());
        tag.put("output", outputHandler.serializeNBT());
        ListTag listTag = new ListTag();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            CompoundTag recipeTag = new CompoundTag();
            recipeTag.put("recipe", recipe.serializeNbt());
            listTag.add(recipeTag);
        }
        tag.put("current_recipes", listTag);
        if (currentRecipe != null) {
            tag.put("current_recipe", currentRecipe.serializeNbt());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        inputHandler.deserializeNBT(tag.getCompound("input"));
        outputHandler.deserializeNBT(tag.getCompound("output"));
        if (tag.contains("current_recipes", ListTag.TAG_LIST)){
            ListTag listTag = tag.getList("current_recipes", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe")));
            }
        }
        if (tag.contains("current_recipe", Tag.TAG_COMPOUND)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(tag.getCompound("current_recipe"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
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

    public ResourceLocation inputStackLocation() {
        return BuiltInRegistries.ITEM.getKey(inputHandler.getStackInSlot(0).getItem());
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<? extends String> blacklist = UncraftEverythingConfig.CONFIG.blacklist.get();
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .toList();

        if (inputHandler.getStackInSlot(0).isEmpty() || inputHandler.getStackInSlot(0).getDamageValue() > 0
                || blacklist.contains(inputStackLocation().toString())
                || wildcardBlacklist.stream().anyMatch(pattern -> pattern.matcher(inputStackLocation().toString()).matches())) {
            currentRecipes.clear();
            currentRecipe = null;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            return;
        }

        ItemStack inputStack = this.inputHandler.getStackInSlot(0);

        List<Recipe<?>> recipes = serverLevel.getRecipeManager().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.result.getItem() == Items.SHULKER_BOX && inputStack.hasTag()){
                    return false;
                }
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder instanceof ShapelessRecipe shapelessRecipe){
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            if(recipeHolder instanceof ShulkerBoxColoring transmuteRecipe){
                return true;
            }

            return false;
        }).toList();

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.is(Items.TIPPED_ARROW)){
            Potion potion = PotionUtils.getPotion(inputStack);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 8));
            ItemStack lingeringPotion = new ItemStack(Items.LINGERING_POTION);
            PotionUtils.setPotion(lingeringPotion, potion);

            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(lingeringPotion);
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));

            outputs.add(outputStack);
        }

        for (Recipe<?> r : recipes) {
            if (r instanceof ShulkerBoxColoring transmuteRecipe && inputStack.is(UETags.Items.SHULKER_BOXES) && !inputStack.is(Items.SHULKER_BOX)) {
                List<Ingredient> ingredients = new ArrayList<>();

                Ingredient shulkerBoxIngredient = Ingredient.of(UETags.Items.SHULKER_BOXES);
                ingredients.add(shulkerBoxIngredient);

                Ingredient dyeIngredient = Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) inputStack.getItem()).getBlock()).getColor())));
                ingredients.add(dyeIngredient);

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                CompoundTag itemContainerContents = inputStack.getTag();

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (itemStack.is(UETags.Items.SHULKER_BOXES)){
                                itemStack.setTag(itemContainerContents);
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r instanceof ShapedRecipe shapedRecipe) {
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

            if (r instanceof ShapelessRecipe shapelessRecipe) {
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.hasTag() && inputStack.getTagElement("Fireworks") != null){
                    byte fireworks = inputStack.getTagElement("Fireworks").getByte("Flight");
                    for(int i = 1;i < fireworks;i++){
                        ingredients.add(Ingredient.of(Items.GUNPOWDER));
                    }
                }
                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);

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
            this.currentRecipe = outputs.get(0);
        }
    }

    private List<Item> getItemsFromIngredient(Ingredient ingredient) {
        List<Item> items = new ArrayList<>();

        // Handle tag ingredients
        if (false) {
            /*for (var holder : ingredient.getCustomIngredient().getItems().toList()) {
                items.add(holder.getItem());
            }*/
        }
        // Handle regular item ingredients
        else {
            try {
                items = Arrays.stream(ingredient.getItems())
                        .map(ItemStack::getItem)
                        .distinct()
                        .toList();
            } catch (IllegalStateException e) {
                // Log error for debugging
                LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
                return Collections.emptyList();
            }
        }

        return items.stream()
                .filter(item -> {
                    if (item.getDescriptionId().contains("shulker_box")){
                        return item == Items.SHULKER_BOX;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Item::getDescriptionId))
                .toList();
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(NonNullList<Ingredient> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = Optional.of(ingredients.get(i));
            List<Item> items = optIngredient.map(ingredient -> {
                        List<Item> ingredientItems = getItemsFromIngredient(ingredient);
                        return ingredientItems.isEmpty() ? List.of(Items.AIR) : ingredientItems;
                    })
                    .orElse(List.of(Items.AIR));

            String key = items.stream()
                    .map(Item::getDescriptionId)
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
                    .map(Item::getDescriptionId)
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

    public void handleButtonClick(){
        if (hasRecipe() && hasEnoughExperience()){
            List<ItemStack> outputs = currentRecipe.getOutputs();

            for (int i = 0; i < outputs.size(); i++) {
                ItemStack output = outputs.get(i);
                if (i < outputHandler.getSlots()) {
                    ItemStack slotStack = outputHandler.getStackInSlot(i);

                    if (slotStack.isEmpty()) {
                        outputHandler.setStackInSlot(i, output.copy());
                    } else if (ItemStack.isSameItemSameTags(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                        slotStack.grow(output.getCount());
                        outputHandler.setStackInSlot(i, slotStack);
                    }
                }
            }

            inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
            player.giveExperiencePoints(-UncraftEverythingConfig.CONFIG.getExperiencePoints());
            setChanged();

            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
        else if (!hasRecipe()) {
            player.displayClientMessage(Component.literal("No recipe or suitable output slot found."), false);
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;
    }

    private boolean hasEnoughExperience(){
        if (player.totalExperience < UncraftEverythingConfig.CONFIG.getExperiencePoints() && !player.isCreative()) {
            player.displayClientMessage(Component.literal("You don't have enough experience points to uncraft this item!"), false);
            return false;
        }
        return true;
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

            if (!ItemStack.isSameItemSameTags(slotStack, result)) {
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

    private static class Group {
        List<Integer> positions;
        List<Item> items;

        Group(List<Integer> positions, List<Item> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}