package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.UETags;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.*;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

@SuppressWarnings({"unused"})
public class UncraftingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    public static final int NO_RECIPE = 0;
    public static final int NO_SUITABLE_OUTPUT_SLOT = 1;
    public static final int NO_ENOUGH_EXPERIENCE = 2;
    public static final int NO_ENOUGH_INPUT = 3;
    public static final int SHULKER_WITH_ITEM = 4;
    public static final int RESTRICTED_ITEM = 5;
    public static final int DAMAGED_ITEM = 6;
    public static final int ENCHANTED_ITEM = 7;

    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private PlayerEntity player;
    private final PropertyDelegate data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private int status = -1;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    public UncraftingTableBlockEntity() {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE);
        this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch (index){
                    case 0:
                        return experience;
                    case 1:
                        return experienceType;
                    case 2:
                        return status;
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0: experience = value;
                    case 1: experienceType = value;
                    case 2: status = value;
                }
            }

            @Override
            public int size() {
                return 3;
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
                PacketByteBuf packetByteBuf = PacketByteBufs.create();
                try{
                    packetByteBuf.encode(UncraftingTableDataPayload.CODEC, new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()));
                } catch (IOException e) {
                    System.out.println("Failed to encode UncraftingTableDataPayload: " + e.getMessage());
                }
                ServerPlayNetworking.send(playerEntity, UncraftingTableDataPayload.ID, packetByteBuf);
            }
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        getOutputStacks();
        if (slot != 0){
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
        }
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
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this.player = player;
        return new UncraftingTableMenu(syncId, playerInventory, this, data);
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        Inventories.toTag(nbt, inventory);
        ListTag listTag = new ListTag();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            CompoundTag recipeTag = new CompoundTag();
            recipeTag.put("recipe", recipe.serializeNbt());
            listTag.add(recipeTag);
        }
        nbt.put("current_recipes", listTag);
        if (currentRecipe != null) {
            nbt.put("current_recipe", currentRecipe.serializeNbt());
        }
        nbt.putInt("experience", experience);
        nbt.putInt("experienceType", experienceType);

        return super.toTag(nbt);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag nbt) {
        super.fromTag(state, nbt);

        Inventories.fromTag(nbt, inventory);
        if (nbt.contains("current_recipes", 9)){
            ListTag listTag = nbt.getList("current_recipes", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe")));
            }
        }
        if (nbt.contains("current_recipe", 10)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(nbt.getCompound("current_recipe"));
        }
        experience = nbt.getInt("experience");
        experienceType = nbt.getInt("experienceType");
    }

    @Override
    public CompoundTag toInitialChunkDataTag() {
        CompoundTag compoundTag = new CompoundTag();
        this.toTag(compoundTag);
        return compoundTag;
    }

    @Override
    public @Nullable BlockEntityUpdateS2CPacket toUpdatePacket() {
        CompoundTag tag = new CompoundTag();
        toTag(tag);
        return new BlockEntityUpdateS2CPacket(pos, 1, tag);
    }

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
    }

    public Identifier inputStackLocation() {
        return Registry.ITEM.getId(this.getStack(inputSlots[0]).getItem());
    }

    public void getOutputStacks() {
        if (!(world instanceof ServerWorld)) return;
        ServerWorld serverLevel = (ServerWorld) world;

        this.status = -1;

        ItemStack inputStack = this.getStack(inputSlots[0]);

        List<String> blacklist = UncraftEverythingConfig.restrictions;
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .collect(Collectors.toList());

        if (this.getStack(inputSlots[0]).isEmpty()
                || this.getStack(inputSlots[0]).getDamage() > 0
                || UncraftEverythingConfig.isItemBlacklisted(this.getStack(inputSlots[0]))
                || UncraftEverythingConfig.isItemWhitelisted(this.getStack(inputSlots[0]))
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim"))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag())
        ) {
            if (this.getStack(inputSlots[0]).getDamage() > 0){
                this.status = DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.isItemBlacklisted(this.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (UncraftEverythingConfig.isItemWhitelisted(this.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim")){
                this.status = ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag()){
                this.status = SHULKER_WITH_ITEM;
            }

            if (this.getStack(inputSlots[0]).isEmpty()){
                this.status = NO_RECIPE;
            }

            currentRecipes.clear();
            currentRecipe = null;
            experience = 0;
            markDirty();
            if (world != null && !world.isClient) {
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
            }
            return;
        }

        List<Recipe<?>> recipes = serverLevel.getRecipeManager().values().stream().filter(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe){
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipeHolder;
                if (shapedRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.output.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack)){
                    this.status = ENCHANTED_ITEM;
                    return false;
                }
                return shapedRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.output.getCount();
            }

            if (recipeHolder instanceof ShapelessRecipe){
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipeHolder;
                if (shapelessRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.output.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack)){
                    this.status = ENCHANTED_ITEM;
                    return false;
                }
                return shapelessRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.output.getCount();
            }

            if(recipeHolder instanceof ShulkerBoxColoringRecipe){
                return inputStack.getItem().isIn(UETags.Items.SHULKER_BOXES) && !inputStack.getItem().equals(Items.SHULKER_BOX);
            }

            if (recipeHolder instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) recipeHolder;
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                if (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(0))){
                    this.status = ENCHANTED_ITEM;
                    return false;
                }
                return inputStack.getItem().equals(smithingTransformRecipe.getOutput().getItem());
            }

            if (this.status == -1){
                this.status = NO_RECIPE;
            }
            return false;
        }).collect(Collectors.toList());

        if (!recipes.isEmpty() || inputStack.getItem().equals(Items.TIPPED_ARROW)){
            this.status = -1;
            this.experience = getExperience();
            this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.getItem().equals(Items.TIPPED_ARROW)){
            Potion potion = PotionUtil.getPotion(inputStack);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 8));
            ItemStack lingeringPotion = new ItemStack(Items.LINGERING_POTION);
            PotionUtil.setPotion(lingeringPotion, potion);

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
            if (r instanceof ShulkerBoxColoringRecipe && inputStack.getItem().isIn(UETags.Items.SHULKER_BOXES) && !inputStack.getItem().equals(Items.SHULKER_BOX)){
                List<Ingredient> ingredients = new ArrayList<>();

                Ingredient shulkerBoxIngredient = Ingredient.fromTag(UETags.Items.SHULKER_BOXES);
                ingredients.add(shulkerBoxIngredient);

                Ingredient dyeIngredient = Ingredient.ofItems(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) inputStack.getItem()).getBlock()).getColor())));
                ingredients.add(dyeIngredient);

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                CompoundTag itemContainerContents = inputStack.getTag();

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (itemStack.getItem().isIn(UETags.Items.SHULKER_BOXES)){
                                itemStack.setTag(itemContainerContents);
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r instanceof ShapedRecipe) {
                ShapedRecipe shapedRecipe = (ShapedRecipe) r;
                // Get all possible combinations of ingredients
                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(shapedRecipe.getPreviewInputs());

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.output.getItem(), shapedRecipe.output.getCount()));

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

            if (r instanceof ShapelessRecipe) {
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) r;
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.input);

                if (inputStack.hasTag() && inputStack.getSubTag("Fireworks") != null) {
                    CompoundTag compoundTag = inputStack.getSubTag("Fireworks");
                    if (compoundTag != null){
                        byte fireworks = compoundTag.getByte("Flight");
                        for(int i = 1;i < fireworks;i++){
                            ingredients.add(Ingredient.ofItems(Items.GUNPOWDER));
                        }
                    }
                }

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.output.getItem(), shapelessRecipe.output.getCount()));

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

            if (r instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) r;
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(smithingTransformRecipe.getOutput());

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
        }

        this.currentRecipes = outputs;

        if (!currentRecipes.isEmpty()) {
            this.currentRecipe = outputs.get(0);
            if(!hasRecipe()){
                this.status = NO_SUITABLE_OUTPUT_SLOT;
            }
            else{
                if (!hasEnoughExperience()) {
                    this.status = NO_ENOUGH_EXPERIENCE;
                }
            }
        }
        else{
            if (this.status == -1){
                this.status = NO_RECIPE;
            }
        }
    }

    private List<Item> getItemsFromIngredient(Ingredient ingredient) {
        List<Item> items;
        ingredient.cacheMatchingStacks();

        try {
            items = Arrays.stream(ingredient.matchingStacks)
                    .map(ItemStack::getItem)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IllegalStateException e) {
            return Collections.emptyList();
        }

        return items.stream()
                .filter(item -> {
                    if (item.getTranslationKey().contains("shulker_box")){
                        return item == Items.SHULKER_BOX;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Item::getTranslationKey))
                .collect(Collectors.toList());
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(DefaultedList<Ingredient> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = Optional.of(ingredients.get(i));
            List<Item> items = optIngredient.map(ingredient -> {
                        List<Item> ingredientItems = getItemsFromIngredient(ingredient);
                        return ingredientItems.isEmpty() ? new ArrayList<>(Collections.singleton(Items.AIR)) : ingredientItems;
                    })
                    .orElse(new ArrayList<>(Collections.singleton(Items.AIR)));

            String key = items.stream()
                    .map(Item::getTranslationKey)
                    .sorted()
                    .collect(Collectors.joining(","));

            Group group = groupKeyToGroup.computeIfAbsent(key, k -> new Group(new ArrayList<>(), items));
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
            if (items.isEmpty()) items = new ArrayList<>(Collections.singleton(Items.AIR));

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

    public void handleButtonClick(){
        if (hasRecipe() && hasEnoughExperience()){
            List<ItemStack> outputs = currentRecipe.getOutputs();

            for (int i = 0; i < outputs.size(); i++) {
                ItemStack output = outputs.get(i);
                if (i < outputSlots.length) {
                    ItemStack slotStack = this.getStack(outputSlots[i]);

                    if (slotStack.isEmpty()) {
                        this.setStack(outputSlots[i], output.copy());
                    } else if (ItemStack.areItemsEqual(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxCount()) {
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
                    PacketByteBuf packetByteBuf = PacketByteBufs.create();
                    try{
                        packetByteBuf.encode(UncraftingTableDataPayload.CODEC, new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()));
                    } catch (IOException e) {
                        System.out.println("Failed to encode UncraftingTableDataPayload: " + e.getMessage());
                    }
                    ServerPlayNetworking.send(playerEntity, UncraftingTableDataPayload.ID, packetByteBuf);
                }
            }
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (this.getStack(0).isEmpty()){
                this.status = NO_RECIPE;
            }
            else {
                this.status = NO_SUITABLE_OUTPUT_SLOT;
            }
        }
        else{
            if (hasEnoughExperience()){
                this.status = -1;
            }
            else{
                this.status = NO_ENOUGH_EXPERIENCE;
            }
        }
    }

    private int getExperience() {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation().toString(), UncraftEverythingConfig.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<Tag<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && this.getStack(inputSlots[0]).getItem().isIn(tagKey.get())) {
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
            return player.totalExperience >= getExperience() || player.isCreative();
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return player.experienceLevel >= getExperience() || player.isCreative();
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

            if (!ItemStack.areItemsEqual(slotStack, result)) {
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
