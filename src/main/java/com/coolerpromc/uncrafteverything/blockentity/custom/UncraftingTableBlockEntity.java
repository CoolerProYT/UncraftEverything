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
import net.minecraft.component.type.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

@SuppressWarnings({"unused", "deprecation"})
public class UncraftingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {
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
    private final PropertyDelegate data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private int status = -1;

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
                ServerPlayNetworking.send(playerEntity, new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()));
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
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer){
            this.player = serverPlayer;
        }
        return new UncraftingTableMenu(syncId, playerInventory, this, data);
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
        nbt.putInt("experience", experience);
        nbt.putInt("experienceType", experienceType);
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
        experience = nbt.getInt("experience");
        experienceType = nbt.getInt("experienceType");
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

        this.status = -1;

        ItemStack inputStack = this.getStack(inputSlots[0]);

        List<String> blacklist = UncraftEverythingConfig.restrictions;
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .toList();

        if (this.getStack(inputSlots[0]).isEmpty()
                || (this.getStack(inputSlots[0]).getDamage() > 0 && !UncraftEverythingConfig.allowDamaged())
                || UncraftEverythingConfig.isItemBlacklisted(this.getStack(inputSlots[0]))
                || UncraftEverythingConfig.isItemWhitelisted(this.getStack(inputSlots[0]))
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && !inputStack.contains(DataComponentTypes.TRIM))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT)
        ) {
            if (this.getStack(inputSlots[0]).getDamage() > 0 && !UncraftEverythingConfig.allowDamaged()){
                this.status = DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.isItemBlacklisted(this.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (UncraftEverythingConfig.isItemWhitelisted(this.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && !inputStack.contains(DataComponentTypes.TRIM)){
                this.status = ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT){
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

        List<RecipeEntry<?>> recipes = serverLevel.getRecipeManager().values().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT){
                    return false;
                }
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                if (shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT){
                    return false;
                }
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            if(recipeHolder.value() instanceof TransmuteRecipe transmuteRecipe){
                return transmuteRecipe.result.value() == inputStack.getItem();
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT){
                    return false;
                }
                return inputStack.isOf(smithingTransformRecipe.result.getItem());
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                ArmorTrim armorTrim = inputStack.get(DataComponentTypes.TRIM);
                if (armorTrim != null){
                    Optional<Ingredient> ingredient = smithingTrimRecipe.addition();
                    Optional<Ingredient> templateIngredient = smithingTrimRecipe.template();
                    if (ingredient.isPresent() && templateIngredient.isPresent()){
                        Optional<RegistryEntry.Reference<ArmorTrimPattern>> trimPatternReference = ArmorTrimPatterns.get(this.world.getRegistryManager(), templateIngredient.get().getMatchingItems().toList().getFirst().value().getDefaultStack());
                        if (trimPatternReference.isPresent() && armorTrim.pattern().equals(trimPatternReference.get())){
                            return true;
                        }
                    }
                }
            }

            if (this.status == -1){
                this.status = NO_RECIPE;
            }
            return false;
        }).toList();

        if (!recipes.isEmpty() || inputStack.isOf(Items.TIPPED_ARROW) || (UncraftEverythingConfig.allowEnchantedItems && inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT)){
            this.status = -1;
            this.experience = getExperience();
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

        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && recipes.isEmpty()){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));
            ItemEnchantmentsComponent enchantments = inputStack.get(DataComponentTypes.ENCHANTMENTS);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.set(DataComponentTypes.STORED_ENCHANTMENTS, enchantments);

            outputStack.addOutput(new ItemStack(inputStack.getItem(), 1));
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (RecipeEntry<?> r : recipes) {
            if (r.value() instanceof TransmuteRecipe transmuteRecipe){
                List<Ingredient> ingredients = List.of(transmuteRecipe.input, transmuteRecipe.material);
                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                ContainerComponent itemContainerContents = inputStack.get(DataComponentTypes.CONTAINER);

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(transmuteRecipe.result.value(), 1));

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
                    Map<Item, Integer> allIngredients = new HashMap<>();

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()),
                                    new ItemStack(stack.getItem(), stack.getCount() + 1));
                        } else {
                            outputStack.addOutput(new ItemStack(item, 1));

                        }
                        allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                    }
                    // Check if the input stack is damaged and if so, remove the corresponding number of damaged items from the output
                    if (inputStack.isDamaged()){
                        RepairableComponent repairableComponent = inputStack.get(DataComponentTypes.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.matches(new ItemStack(x.getKey(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamage() / inputStack.getMaxDamage() * x.getValue());
                                    for (int i = 0;i < outputStack.getOutputs().size() && damagedPercentage != 0;i++){
                                        if (outputStack.getOutputs().get(i).isOf(x.getKey())){
                                            outputStack.setOutput(i, ItemStack.EMPTY);
                                            damagedPercentage--;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        else{
                            this.status = DAMAGED_ITEM;
                            outputs.clear();
                            return;
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe) {
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.contains(DataComponentTypes.FIREWORKS)){
                    FireworksComponent fireworks = inputStack.get(DataComponentTypes.FIREWORKS);
                    if (fireworks != null){
                        for(int i = 1;i < fireworks.flightDuration();i++){
                            ingredients.add(Ingredient.ofItem(Items.GUNPOWDER));
                        }
                    }
                }

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem(), shapelessRecipe.result.getCount()));
                    Map<Item, Integer> allIngredients = new HashMap<>();

                    for (Item item : ingredientCombination) {
                        if (item != Items.AIR) {
                            if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                                ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                                outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()),
                                        new ItemStack(stack.getItem(), stack.getCount() + 1));
                            } else {
                                outputStack.addOutput(new ItemStack(item, 1));
                            }
                            allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                        }
                    }
                    // Check if the input stack is damaged and if so, remove the corresponding number of damaged items from the output
                    if (inputStack.isDamaged()){
                        RepairableComponent repairableComponent = inputStack.get(DataComponentTypes.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.matches(new ItemStack(x.getKey(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamage() / inputStack.getMaxDamage() * x.getValue());
                                    for (int i = 0;i < outputStack.getOutputs().size() && damagedPercentage != 0;i++){
                                        if (outputStack.getOutputs().get(i).isOf(x.getKey())){
                                            outputStack.setOutput(i, ItemStack.EMPTY);
                                            damagedPercentage--;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        else{
                            this.status = DAMAGED_ITEM;
                            outputs.clear();
                            return;
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                List<Optional<Ingredient>> ingredients = new ArrayList<>();

                ingredients.add(smithingTransformRecipe.base());
                ingredients.add(smithingTransformRecipe.addition());
                ingredients.add(smithingTransformRecipe.template());

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.getItem(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            if (item.getDefaultStack().isDamageable()){
                                stack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                            }
                            stack.increment(1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getDefaultStack().isDamageable()){
                                itemStack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                                if (itemStack.getOrDefault(DataComponentTypes.DAMAGE, 0) >= itemStack.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0)){
                                    itemStack = ItemStack.EMPTY;
                                }
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                ArmorTrim armorTrim = inputStack.get(DataComponentTypes.TRIM);
                Optional<Ingredient> additionIngredient = smithingTrimRecipe.addition();

                List<Optional<Ingredient>> ingredients = new ArrayList<>();
                smithingTrimRecipe.base().ifPresent(ingredient -> ingredient.getMatchingItems().filter(itemHolder -> inputStack.isOf(itemHolder.value())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.ofItem(itemHolder.value())))));
                ingredients.add(smithingTrimRecipe.template());
                if (additionIngredient.isPresent() && armorTrim != null){
                    additionIngredient.get().getMatchingItems().filter(itemHolder -> {
                        RegistryKey<Item> itemResourceKey = itemHolder.getKey().orElse(null);
                        RegistryKey<ArmorTrimMaterial> armorTrimKey = armorTrim.material().getKey().orElse(null);
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.getValue().getPath().contains(armorTrimKey.getValue().getPath());
                        }
                        return false;
                    }).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.ofItem(itemHolder.value()))));
                }

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);
                ItemEnchantmentsComponent itemEnchantments = inputStack.get(DataComponentTypes.ENCHANTMENTS);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            if (item.getDefaultStack().isOf(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getMatchingItems().toList().getFirst().value() : Items.AIR)){
                                stack.set(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
                                stack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                            }
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (item.getDefaultStack().isOf(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getMatchingItems().toList().getFirst().value() : Items.AIR)){
                                itemStack.set(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
                                itemStack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
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
            this.currentRecipe = outputs.getFirst();
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
                    return item.getRecipeRemainder(item.getDefaultStack()) == ItemStack.EMPTY || item.getRecipeRemainder(item.getDefaultStack()).getItem() != item.getDefaultStack().getItem();
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

        List<T> firstList = lists.getFirst();
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
