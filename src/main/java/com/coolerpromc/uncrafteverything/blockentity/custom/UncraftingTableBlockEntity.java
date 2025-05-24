package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.UETags;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

@SuppressWarnings({"unused", "deprecation"})
public class UncraftingTableBlockEntity extends BlockEntity implements MenuProvider {
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
    private ServerPlayer player;
    private final ContainerData data;
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
                UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UncraftingTableDataPayload(getBlockPos(), getCurrentRecipes()));
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

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0 -> experience;
                    case 1 -> experienceType;
                    case 2 -> status;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0 -> experience = value;
                    case 1 -> experienceType = value;
                    case 2 -> status = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            this.player = serverPlayer;
        }
        return new UncraftingTableMenu(containerId, playerInventory, this, data);
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
        tag.putInt("experience", experience);
        tag.putInt("experienceType", experienceType);
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
        experience = tag.getInt("experience");
        experienceType = tag.getInt("experienceType");
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
        if (player == null) return;

        this.status = -1;

        ItemStack inputStack = this.inputHandler.getStackInSlot(0);

        List<? extends String> blacklist = UncraftEverythingConfig.CONFIG.restrictions.get();
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .toList();

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

        List<Recipe<?>> recipes = serverLevel.getRecipeManager().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder instanceof ShapelessRecipe shapelessRecipe){
                if (shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            if(recipeHolder instanceof ShulkerBoxColoring transmuteRecipe){
                return inputStack.is(UETags.Items.SHULKER_BOXES) && !inputStack.is(Items.SHULKER_BOX);
            }

            if (recipeHolder instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing()){
                    return false;
                }
                if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty()){
                    return false;
                }
                return inputStack.is(smithingTransformRecipe.result.getItem());
            }

            if (recipeHolder instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing()){
                    return false;
                }

                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(this.level.registryAccess(), inputStack);
                if (armorTrim.isPresent()){
                    Ingredient ingredient = smithingTrimRecipe.addition;
                    Optional<Holder.Reference<TrimPattern>> trimPatternReference = TrimPatterns.getFromTemplate(this.level.registryAccess(), smithingTrimRecipe.template.getItems()[0]);
                    if (ingredient != Ingredient.EMPTY && trimPatternReference.isPresent() && armorTrim.get().pattern().equals(trimPatternReference.get())){
                        return true;
                    }
                }
            }

            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");
                    Class<?> gameStageHelperClass = Class.forName("net.darkhax.gamestages.GameStageHelper");

                    if (shapedRecipeStageClass.isInstance(recipeHolder)) {
                        String stage = (String) shapedRecipeStageClass.getMethod("getStage").invoke(recipeHolder);
                        Boolean hasStage = (Boolean) gameStageHelperClass.getMethod("hasAnyOf", Player.class, Collection.class).invoke(null, player, Collections.singleton(stage));

                        if (hasStage) {
                            Object recipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipeHolder);

                            if (recipe instanceof ShapedRecipe shapedRecipe) {
                                if (shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.result.getCount()) {
                                    this.status = NO_ENOUGH_INPUT;
                                }
                                if (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputHandler.getStackInSlot(0))) {
                                    this.status = ENCHANTED_ITEM;
                                    return false;
                                }
                                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
                            }
                        }
                    }

                    if (recipeStageClass.isInstance(recipeHolder)) {
                        String stage = (String) recipeStageClass.getMethod("getStage").invoke(recipeHolder);

                        Boolean hasStage = (Boolean) gameStageHelperClass.getMethod("hasAnyOf", Player.class, Collection.class).invoke(null, player, Collections.singleton(stage));

                        if (hasStage) {
                            Object recipe = recipeStageClass.getMethod("getRecipe").invoke(recipeHolder);

                            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                                if (shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.result.getCount()) {
                                    this.status = NO_ENOUGH_INPUT;
                                }
                                if (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputHandler.getStackInSlot(0))) {
                                    this.status = ENCHANTED_ITEM;
                                    return false;
                                }
                                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }

            if (this.status == -1){
                this.status = NO_RECIPE;
            }
            return false;
        }).map(recipe -> {
            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

                    if (shapedRecipeStageClass.isInstance(recipe)) {
                        Object innerRecipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipe);
                        if (innerRecipe instanceof ShapedRecipe shapedRecipe) {
                            return shapedRecipe;
                        }
                    }

                    if (recipeStageClass.isInstance(recipe)) {
                        Object innerRecipe = recipeStageClass.getMethod("getRecipe").invoke(recipe);
                        if (innerRecipe instanceof ShapelessRecipe shapelessRecipe) {
                            return shapelessRecipe;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }

            return recipe instanceof Recipe<?> ? recipe : null;
        }).toList();

        if (!recipes.isEmpty() || inputStack.is(Items.TIPPED_ARROW) || (UncraftEverythingConfig.CONFIG.allowEnchantedItems.get() && !EnchantmentHelper.getEnchantments(inputStack).isEmpty())){
            this.status = -1;
            this.experience = getExperience();
            this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

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

        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && recipes.isEmpty()){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(inputStack);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(enchantments, book);

            outputStack.addOutput(new ItemStack(inputStack.getItem(), 1));
            outputStack.addOutput(book);

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
                    CompoundTag compoundTag = inputStack.getTagElement("Fireworks");
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

            if (r instanceof SmithingTransformRecipe smithingTransformRecipe){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                ingredients.add(smithingTransformRecipe.template);

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(smithingTransformRecipe.result);

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

            if (r instanceof SmithingTrimRecipe smithingTrimRecipe){
                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(this.level.registryAccess(), inputStack);
                Ingredient additionIngredient = smithingTrimRecipe.addition;

                NonNullList<Ingredient> ingredients = NonNullList.create();
                Arrays.stream(smithingTrimRecipe.base.getItems()).filter(itemStack -> itemStack.is(inputStack.getItem())).forEach(itemStack -> ingredients.add(Ingredient.of(itemStack)));
                ingredients.add(smithingTrimRecipe.template);
                Arrays.stream(smithingTrimRecipe.addition.getItems()).filter(itemStack -> {
                    if (armorTrim.isPresent()){
                        ResourceKey<TrimMaterial> armorTrimKey = armorTrim.get().material().unwrapKey().orElse(null);
                        ResourceKey<Item> itemResourceKey =  itemStack.getItemHolder().unwrapKey().orElse(null);
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.location().getPath().contains(armorTrimKey.location().getPath());
                        }
                    }
                    return false;
                }).forEach(itemStack -> ingredients.add(Ingredient.of(itemStack)));

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(inputStack);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            if (item.getDefaultInstance().is(ingredients.get(0).getItems()[0].getItem())){
                                EnchantmentHelper.setEnchantments(itemEnchantments, stack);
                            }
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (item.getDefaultInstance().is(ingredients.get(0).getItems()[0].getItem())){
                                EnchantmentHelper.setEnchantments(itemEnchantments, itemStack);
                            }
                            outputStack.addOutput(itemStack);
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
                    .toList();
        } catch (IllegalStateException e) {
            // Log error for debugging
            LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
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
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && inputHandler.getStackInSlot(0).is(tagKey.get())) {
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

    public ContainerData getData() {
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