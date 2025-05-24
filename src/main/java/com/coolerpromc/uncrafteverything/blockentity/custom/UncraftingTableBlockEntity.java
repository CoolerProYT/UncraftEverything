package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.UETags;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

@SuppressWarnings({"unused", "deprecation"})
public class UncraftingTableBlockEntity extends TileEntity implements INamedContainerProvider {
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
    private ServerPlayerEntity player;
    private final IIntArray data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private int status = -1;

    private final ItemStackHandler inputHandler = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            getOutputStacks();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new UncraftingTableDataPayload(getBlockPos(), getCurrentRecipes()));
            }
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };

    public UncraftingTableBlockEntity() {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get());
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new IIntArray() {
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
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public @NotNull ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.uncrafteverything.uncrafting_table");
    }

    @Nullable
    @Override
    public Container createMenu(int containerId, @NotNull PlayerInventory playerInventory, @NotNull PlayerEntity player) {
        if (player instanceof ServerPlayerEntity){
            this.player = (ServerPlayerEntity) player;
        }
        return new UncraftingTableMenu(containerId, playerInventory, this, data);
    }

    @Override
    public CompoundNBT save(@NotNull CompoundNBT tag) {
        tag.put("input", inputHandler.serializeNBT());
        tag.put("output", outputHandler.serializeNBT());
        ListNBT listTag = new ListNBT();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            CompoundNBT recipeTag = new CompoundNBT();
            recipeTag.put("recipe", recipe.serializeNbt());
            listTag.add(recipeTag);
        }
        tag.put("current_recipes", listTag);
        if (currentRecipe != null) {
            tag.put("current_recipe", currentRecipe.serializeNbt());
        }
        tag.putInt("experience", experience);
        tag.putInt("experienceType", experienceType);

        return super.save(tag);
    }

    @Override
    public void load(BlockState blockState, @NotNull CompoundNBT tag) {
        super.load(blockState, tag);

        inputHandler.deserializeNBT(tag.getCompound("input"));
        outputHandler.deserializeNBT(tag.getCompound("output"));
        if (tag.contains("current_recipes", Constants.NBT.TAG_LIST)){
            ListNBT listTag = tag.getList("current_recipes", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundNBT recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe")));
            }
        }
        if (tag.contains("current_recipe", Constants.NBT.TAG_COMPOUND)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(tag.getCompound("current_recipe"));
        }
        experience = tag.getInt("experience");
        experienceType = tag.getInt("experienceType");
    }

    @Override
    public @NotNull CompoundNBT getUpdateTag() {
        CompoundNBT compoundTag = new CompoundNBT();
        this.save(compoundTag);
        return compoundTag;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = new CompoundNBT();
        save(tag);
        return new SUpdateTileEntityPacket(this.getBlockPos(), 1, tag);
    }

    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public ResourceLocation inputStackLocation() {
        return ForgeRegistries.ITEMS.getKey(inputHandler.getStackInSlot(0).getItem());
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerWorld)) return;
        ServerWorld serverLevel = (ServerWorld) level;

        this.status = -1;

        ItemStack inputStack = this.inputHandler.getStackInSlot(0);

        List<? extends String> blacklist = UncraftEverythingConfig.CONFIG.restrictions.get();
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .collect(Collectors.toList());

        if (inputHandler.getStackInSlot(0).isEmpty()
                || inputHandler.getStackInSlot(0).getDamageValue() > 0
                || UncraftEverythingConfig.CONFIG.isItemBlacklisted(inputHandler.getStackInSlot(0))
                || UncraftEverythingConfig.CONFIG.isItemWhitelisted(inputHandler.getStackInSlot(0))
                || (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputHandler.getStackInSlot(0)) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim"))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag())
        ) {
            if (inputHandler.getStackInSlot(0).getDamageValue() > 0){
                this.status = DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.CONFIG.isItemBlacklisted(inputHandler.getStackInSlot(0))){
                this.status = RESTRICTED_ITEM;
            }

            if (UncraftEverythingConfig.CONFIG.isItemWhitelisted(inputHandler.getStackInSlot(0))){
                this.status = RESTRICTED_ITEM;
            }

            if (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputHandler.getStackInSlot(0)) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim")){
                this.status = ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag()){
                this.status = SHULKER_WITH_ITEM;
            }

            if (inputHandler.getStackInSlot(0).isEmpty()){
                this.status = NO_RECIPE;
            }

            currentRecipes.clear();
            currentRecipe = null;
            experience = 0;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            return;
        }

        List<IRecipe<?>> recipes = serverLevel.getRecipeManager().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe){
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipeHolder;
                if (shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder instanceof ShapelessRecipe){
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipeHolder;
                if (shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            if(recipeHolder instanceof ShulkerBoxColoringRecipe){
                return inputStack.getItem().is(UETags.Items.SHULKER_BOXES) && !inputStack.getItem().equals(Items.SHULKER_BOX);
            }

            if (recipeHolder instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) recipeHolder;
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing()){
                    return false;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return inputStack.getItem().equals(smithingTransformRecipe.getResultItem().getItem());
            }

            if (this.status == -1){
                this.status = NO_RECIPE;
            }
            return false;
        }).collect(Collectors.toList());

        if (!recipes.isEmpty() || inputStack.getItem().equals(Items.TIPPED_ARROW) || (UncraftEverythingConfig.CONFIG.allowEnchantedItems.get() && !EnchantmentHelper.getEnchantments(inputStack).isEmpty() && !inputStack.getItem().equals(Items.ENCHANTED_BOOK))) {
            this.status = -1;
            this.experience = getExperience();
            this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.getItem().equals(Items.TIPPED_ARROW)){
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
        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && recipes.isEmpty() && !inputStack.getItem().equals(Items.ENCHANTED_BOOK)){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(inputStack);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(enchantments, book);

            outputStack.addOutput(new ItemStack(inputStack.getItem(), 1));
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (IRecipe<?> r : recipes) {
            if (r instanceof ShulkerBoxColoringRecipe && inputStack.getItem().is(UETags.Items.SHULKER_BOXES) && !inputStack.getItem().equals(Items.SHULKER_BOX)) {
                List<Ingredient> ingredients = new ArrayList<>();

                Ingredient shulkerBoxIngredient = Ingredient.of(UETags.Items.SHULKER_BOXES);
                ingredients.add(shulkerBoxIngredient);

                Ingredient dyeIngredient = Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) inputStack.getItem()).getBlock()).getColor())));
                ingredients.add(dyeIngredient);

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                CompoundNBT itemContainerContents = inputStack.getTag();

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (itemStack.getItem().is(UETags.Items.SHULKER_BOXES)){
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

            if (r instanceof ShapelessRecipe) {
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) r;
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.hasTag() && inputStack.getTagElement("Fireworks") != null){
                    CompoundNBT compoundTag = inputStack.getTagElement("Fireworks");
                    if (compoundTag != null){
                        byte fireworks = compoundTag.getByte("Flight");
                        for(int i = 1;i < fireworks;i++){
                            ingredients.add(Ingredient.of(Items.GUNPOWDER));
                        }
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

            if (r instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) r;
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(smithingTransformRecipe.getResultItem());

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

        try {
            items = Arrays.stream(ingredient.getItems())
                    .map(ItemStack::getItem)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IllegalStateException e) {
            return Collections.emptyList();
        }

        return items.stream()
                .filter(item -> {
                    if (item.getDescriptionId().contains("shulker_box")){
                        return item == Items.SHULKER_BOX;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Item::getDescriptionId))
                .collect(Collectors.toList());
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(NonNullList<Ingredient> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = Optional.of(ingredients.get(i));
            List<Item> items = optIngredient.map(ingredient -> {
                        List<Item> ingredientItems = getItemsFromIngredient(ingredient);
                        return ingredientItems.isEmpty() ? new ArrayList<>(Collections.singleton(Items.AIR)) : ingredientItems;
                    })
                    .orElse(new ArrayList<>(Collections.singleton(Items.AIR)));

            String key = items.stream()
                    .map(Item::getDescriptionId)
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
                    } else if (ItemStack.isSame(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                        slotStack.grow(output.getCount());
                        outputHandler.setStackInSlot(i, slotStack);
                    }
                }
            }

            if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
                player.giveExperiencePoints(-getExperience());
            }
            else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
                player.giveExperienceLevels(-getExperience());
            }
            inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
            setChanged();

            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (inputHandler.getStackInSlot(0).isEmpty()){
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
        int experience = experienceMap.getOrDefault(inputStackLocation().toString(), UncraftEverythingConfig.CONFIG.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<Tags.IOptionalNamedTag<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && inputHandler.getStackInSlot(0).getItem().is(tagKey.get())) {
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
        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return player.totalExperience >= getExperience() || player.isCreative();
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
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
            if (i >= outputHandler.getSlots()) return false;

            ItemStack slotStack = outputHandler.getStackInSlot(i);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSame(slotStack, result)) {
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

    public IIntArray getData() {
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