package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

public class UncraftingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {
    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private PlayerEntity player;
    private PropertyDelegate data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    public UncraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE, pos, state);
        this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0 -> experience;
                    case 1 -> experienceType;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0 -> experience = value;
                    case 1 -> experienceType = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
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
        return new UncraftingTableMenu(syncId, playerInventory, this, data);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, inventory);
        WriteView.ListAppender<UncraftingTableRecipe> recipeListAppender = view.getListAppender("current_recipes", UncraftingTableRecipe.CODEC);
        for (UncraftingTableRecipe recipe : currentRecipes) {
            recipeListAppender.add(recipe);
        }

        if (currentRecipe != null) {
            view.put("current_recipe", UncraftingTableRecipe.CODEC, currentRecipe);
        }
        view.putInt("experience", experience);
        view.putInt("experienceType", experienceType);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        Inventories.readData(view, inventory);
        for (UncraftingTableRecipe recipe : view.getTypedListView("current_recipes", UncraftingTableRecipe.CODEC)) {
            currentRecipes.add(recipe);
        }

        currentRecipe = view.read("current_recipe", UncraftingTableRecipe.CODEC).orElse(null);
        experience = view.getInt("experience", UncraftEverythingConfig.experience);
        experienceType = view.getInt("experienceType", UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0);
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

    public Identifier inputStackLocation() {
        return Registries.ITEM.getId(this.getStack(inputSlots[0]).getItem());
    }


    public void getOutputStacks() {
        if (!(world instanceof ServerWorld serverLevel)) return;

        List<String> blacklist = UncraftEverythingConfig.restrictions;
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .toList();

        if (this.getStack(inputSlots[0]).isEmpty()
                || this.getStack(inputSlots[0]).getDamage() > 0
                || UncraftEverythingConfig.isItemBlacklisted(this.getStack(inputSlots[0]))
                || UncraftEverythingConfig.isItemWhitelisted(this.getStack(inputSlots[0]))
                || !UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0]))
        ) {
            currentRecipes.clear();
            currentRecipe = null;
            experience = 0;
            markDirty();
            if (world != null && !world.isClient) {
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
            }
            return;
        }

        ItemStack inputStack = this.getStack(inputSlots[0]);

        List<RecipeEntry<?>> recipes = serverLevel.getRecipeManager().values().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.result.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT){
                    return false;
                }
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            if(recipeHolder.value() instanceof TransmuteRecipe transmuteRecipe){
                return transmuteRecipe.result.itemEntry().value() == inputStack.getItem();
            }

            return false;
        }).toList();

        if (!recipes.isEmpty() || inputStack.isOf(Items.TIPPED_ARROW)){
            experience = getExperience();
            this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.isOf(Items.TIPPED_ARROW)){
            PotionContentsComponent potionContents = inputStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 8));
            ItemStack potion = new ItemStack(Items.LINGERING_POTION);
            potion.set(DataComponentTypes.POTION_CONTENTS, potionContents);

            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(potion);
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));
            outputStack.addOutput(new ItemStack(Items.ARROW, 1));

            outputs.add(outputStack);
        }

        for (RecipeEntry<?> r : recipes) {
            if (r.value() instanceof TransmuteRecipe transmuteRecipe){
                List<Ingredient> ingredients = List.of(transmuteRecipe.input, transmuteRecipe.material);
                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                ContainerComponent itemContainerContents = inputStack.get(DataComponentTypes.CONTAINER);

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(transmuteRecipe.result.itemEntry().value(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            if (stack.contains(DataComponentTypes.CONTAINER)){
                                stack.set(DataComponentTypes.CONTAINER, itemContainerContents);
                            }
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (itemStack.contains(DataComponentTypes.CONTAINER)){
                                itemStack.set(DataComponentTypes.CONTAINER, itemContainerContents);
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

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
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.contains(DataComponentTypes.FIREWORKS)){
                    FireworksComponent fireworks = inputStack.get(DataComponentTypes.FIREWORKS);
                    for(int i = 1;i < fireworks.flightDuration();i++){
                        ingredients.add(Ingredient.ofItem(Items.GUNPOWDER));
                    }
                }

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);

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
                .filter(item -> {
                    if (item.getTranslationKey().contains("shulker_box")){
                        return item == Items.SHULKER_BOX;
                    }
                    return true;
                })
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
        if (hasRecipe() && hasEnoughExperience()){
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

            if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
                player.addExperience(-getExperience());
            }
            else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
                player.addExperienceLevels(-getExperience());
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
        else if (!hasRecipe()){
            player.sendMessage(Text.literal("No recipe or suitable output slot found."), false);
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;
    }

    private int getExperience() {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation().toString(), UncraftEverythingConfig.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && this.getStack(inputSlots[0]).isIn(tagKey.get())) {
                    experience = exp.getValue();
                    break;
                }
            }

            if (exp.getKey().contains("*")){
                String regex = exp.getKey().replace("*", ".*");
                if (Pattern.matches(regex, inputStackLocation().toString())){
                    experience = exp.getValue();
                    break;
                }
            }
        }

        return experience;
    }

    private boolean hasEnoughExperience(){
        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            if (player.totalExperience < getExperience() && !player.isCreative()) {
                player.sendMessage(Text.literal("You don't have enough experience points to uncraft this item!"), false);
                return false;
            }
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            if (player.experienceLevel < getExperience() && !player.isCreative()) {
                player.sendMessage(Text.literal("You don't have enough experience levels to uncraft this item!"), false);
                return false;
            }
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

    public PropertyDelegate getData() {
        return data;
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
