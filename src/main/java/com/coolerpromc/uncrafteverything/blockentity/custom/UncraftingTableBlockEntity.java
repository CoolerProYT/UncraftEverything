package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.nbt.NbtCompound;
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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

@SuppressWarnings({"unused"})
public class UncraftingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
    public static final int NO_RECIPE = 0;
    public static final int NO_SUITABLE_OUTPUT_SLOT = 1;
    public static final int NO_ENOUGH_EXPERIENCE = 2;
    public static final int NO_ENOUGH_INPUT = 3;
    public static final int SHULKER_WITH_ITEM = 4;
    public static final int RESTRICTED_ITEM = 5;
    public static final int DAMAGED_ITEM = 6;
    public static final int ENCHANTED_ITEM = 7;
    public static final int LOCKED_ITEM = 8;
    public static final int PROGRESSION_NOT_DEFINED = 9;

    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private ServerPlayerEntity player;
    private final PropertyDelegate data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private int status = -1;
    private ItemStack currentStack = ItemStack.EMPTY;
    private int page = 0;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private final ImplementedInventory slots = new ImplementedInventory(10){
        @Override
        public void setStack(int slot, ItemStack stack) {
            super.setStack(slot, stack);
            getOutputStacks();
            if (world != null && !world.isClient() && slot == inputSlots[0] && player != null) {
                if (currentStack.getItem() != this.getStack(0).getItem() && !this.getStack(0).isEmpty()){
                    for (int outputSlot : outputSlots) {
                        ItemStack outputStack = this.getStack(outputSlot);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().offerOrDrop(outputStack);
                            this.setStack(outputSlot, ItemStack.EMPTY);
                            markDirty();
                        }
                    }
                }
                currentStack = this.getStack(0);
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
                player.currentScreenHandler.sendContentUpdates();
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
            return super.removeStack(slot);
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
            return Arrays.asList(inputSlots).contains(slot) && side != Direction.DOWN;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction side) {
            return Arrays.asList(outputSlots).contains(slot) && side == Direction.DOWN;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return Arrays.stream(outputSlots).noneMatch(value -> value == slot);
        }
    };

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
    protected void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, inventory);

        view.putInt("experience", experience);
        view.putInt("experienceType", experienceType);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        Inventories.readData(view, inventory);

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
        return Registries.ITEM.getId(slots.getStack(inputSlots[0]).getItem());
    }


    public void getOutputStacks() {
        if (!(world instanceof ServerWorld serverLevel) || player == null) return;

        this.status = -1;

        ItemStack inputStack = slots.getStack(inputSlots[0]);

        List<String> blacklist = UncraftEverythingConfig.restrictions;
        List<Pattern> wildcardBlacklist = blacklist.stream()
                .filter(s -> s.contains("*"))
                .map(s -> Pattern.compile(s.replace("*", ".*")))
                .toList();

        if (slots.getStack(inputSlots[0]).isEmpty()
                || UncraftEverythingConfig.isItemLocked(player, slots.getStack(inputSlots[0])).getLeft()
                || (slots.getStack(inputSlots[0]).getDamage() > 0 && !UncraftEverythingConfig.allowDamaged())
                || UncraftEverythingConfig.isItemBlacklisted(slots.getStack(inputSlots[0]))
                || UncraftEverythingConfig.isItemWhitelisted(slots.getStack(inputSlots[0]))
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(slots.getStack(inputSlots[0])) && !inputStack.contains(DataComponentTypes.TRIM))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT)
                || (inputStack.getItem() == Items.ENCHANTED_BOOK)
        ) {
            if (slots.getStack(inputSlots[0]).getDamage() > 0 && !UncraftEverythingConfig.allowDamaged()){
                this.status = DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.isItemBlacklisted(slots.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (UncraftEverythingConfig.isItemWhitelisted(slots.getStack(inputSlots[0]))){
                this.status = RESTRICTED_ITEM;
            }

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(slots.getStack(inputSlots[0])) && !inputStack.contains(DataComponentTypes.TRIM)){
                this.status = ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT){
                this.status = SHULKER_WITH_ITEM;
            }

            if (slots.getStack(inputSlots[0]).isEmpty() || (inputStack.getItem() == Items.ENCHANTED_BOOK)){
                this.status = NO_RECIPE;
            }

            Pair<Boolean, Integer> isItemLocked = UncraftEverythingConfig.isItemLocked(player, slots.getStack(inputSlots[0]));
            if (isItemLocked.getLeft() && !slots.getStack(inputSlots[0]).isEmpty()){
                this.status = isItemLocked.getRight();
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
            if (!recipeHolder.id().getValue().getNamespace().equals("minecraft") && Registries.ITEM.getId(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes()){
                return false;
            }

            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.result.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT){
                    return false;
                }
                EquippableComponent component = inputStack.get(DataComponentTypes.EQUIPPABLE);
                if (component != null && component.slot() == EquipmentSlot.CHEST){
                    if (component.assetId().isPresent()){
                        Identifier assetId = component.assetId().get().getValue();
                        if (assetId.getNamespace().equals("elytra_chestplate")){
                            return false;
                        }
                    }
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
                return transmuteRecipe.result.itemEntry().value() == inputStack.getItem();
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT){
                    return false;
                }
                EquippableComponent component = inputStack.get(DataComponentTypes.EQUIPPABLE);
                if (component != null && component.slot() == EquipmentSlot.CHEST){
                    if (component.assetId().isPresent()){
                        Identifier assetId = component.assetId().get().getValue();
                        if (!assetId.getNamespace().equals("elytra_chestplate") && smithingTransformRecipe.template().isEmpty()){
                            return false;
                        }
                        if (inputStack.isOf(Items.NETHERITE_CHESTPLATE) && smithingTransformRecipe.template().isPresent() && assetId.getNamespace().equals("elytra_chestplate")){
                            return false;
                        }
                    }
                }
                return inputStack.isOf(smithingTransformRecipe.result.itemEntry().value());
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                ArmorTrim armorTrim = inputStack.get(DataComponentTypes.TRIM);
                if (armorTrim != null){
                    Optional<Ingredient> ingredient = smithingTrimRecipe.addition();
                    if (ingredient.isPresent() && armorTrim.pattern().equals(smithingTrimRecipe.pattern)){
                        return true;
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
            ItemStack output = new ItemStack(inputStack.getItem(), 1);
            output.setDamage(inputStack.getDamage());

            outputStack.addOutput(output);
            outputStack.addOutput(book);

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

                ingredients.add(Optional.of(smithingTransformRecipe.base()));
                ingredients.add(smithingTransformRecipe.addition());
                ingredients.add(smithingTransformRecipe.template());

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.itemEntry().value(), 1));

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
                smithingTrimRecipe.base().getMatchingItems().filter(itemHolder -> inputStack.isOf(itemHolder.value())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.ofItem(itemHolder.value()))));
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
            ServerPlayNetworking.send(player, new UncraftingRecipeSelectionRequestPayload());
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
                    if (item.getTranslationKey().contains("shulker_box") && item.getTranslationKey().contains("minecraft")){
                        if (this.slots.getStack(getInputSlots()[0]).getItem().getRegistryEntry().getKey().get().getValue().getNamespace().equals("reinfshulker")){
                            return true;
                        }
                        return item == Items.SHULKER_BOX;
                    } else if (item.getTranslationKey().contains("shulker_box") && item.getTranslationKey().contains("reinfshulker")) {
                        List<String> colors = new ArrayList<>();

                        for (DyeColor color : DyeColor.values()){
                            colors.add(color.getId());
                        }
                        String id = item.getRegistryEntry().getKey().get().getValue().getPath();
                        String color = id.substring(0, id.indexOf("_"));
                        if (!colors.contains(color)){
                            color += id.substring(id.indexOf("_"), id.indexOf("_", id.indexOf("_") + 1));
                        }
                        return (!colors.contains(color) || this.slots.getStack(getInputSlots()[0]).getItem().getRegistryEntry().getKey().get().getValue().getPath().contains(color)) && !item.equals(this.slots.getStack(getInputSlots()[0]).getItem());
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
            List<Item> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = Registries.ITEM.getId(slots.getStack(0).getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return Registries.ITEM.getId(item).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = Registries.ITEM.getId(item);
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

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

            List<Item> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = Registries.ITEM.getId(slots.getStack(0).getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return Registries.ITEM.getId(item).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = Registries.ITEM.getId(item);
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

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

    public static boolean isVanillaIngredientRecipe(Recipe<?> recipe) {
        List<Optional<Ingredient>> ingredients;

        if (recipe instanceof ShapedRecipe shaped) {
            ingredients = shaped.getIngredients();
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            ingredients = shapeless.ingredients.stream().map(Optional::of).toList();
        } else if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe){
            ingredients = List.of(
                    Optional.of(smithingTransformRecipe.base()),
                    smithingTransformRecipe.addition(),
                    smithingTransformRecipe.template()
            );
        } else {
            return true; // skip filtering for other types
        }

        for (Optional<Ingredient> ingredient : ingredients) {
            if (ingredient.isPresent()){
                if (ingredient.get().getCustomIngredient() != null && !ingredient.get().getCustomIngredient().getMatchingItems().toList().isEmpty()){
                    if (!ingredient.get().getCustomIngredient().getMatchingItems().map(RegistryEntry::value).map(Registries.ITEM::getId).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
                else{
                    if (!ingredient.get().getMatchingItems().map(RegistryEntry::value).map(Registries.ITEM::getId).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void handleUncraftButtonClicked(boolean hasShiftDown){
        if (hasShiftDown){
            while (hasRecipe() && hasEnoughExperience()) {
                processUncraft(hasNextRecipe());
            }
        }
        else{
            if (hasRecipe() && hasEnoughExperience()) {
                processUncraft(false);
            }
        }
    }

    private void processUncraft(boolean hasNext){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (int i = 0; i < outputs.size(); i++) {
            ItemStack output = outputs.get(i);
            if (i < outputSlots.length) {
                ItemStack slotStack = slots.getStack(outputSlots[i]);

                if (slotStack.isEmpty()) {
                    slots.setStack(outputSlots[i], output.copy());
                } else if (ItemStack.areItemsAndComponentsEqual(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxCount()) {
                    slotStack.increment(output.getCount());
                    slots.setStack(outputSlots[i], slotStack);
                }
            }
        }

        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            player.addExperience(-getExperience());
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            player.addExperienceLevels(-getExperience());
        }

        slots.removeStack(0, this.currentRecipe.getInput().getCount());
        markDirty();

        getOutputStacks();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            if (!hasNext){
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (slots.getStack(0).isEmpty()){
                this.status = NO_RECIPE;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, slots.getStack(inputSlots[0])).getLeft()){
                    this.status = LOCKED_ITEM;
                }
                else{
                    this.status = NO_SUITABLE_OUTPUT_SLOT;
                }
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

    public void updatePage(int page){
        this.page = page;
        ServerPlayNetworking.send(player, new UncraftingTableDataPayload(this.getPos(), new ArrayList<>(currentRecipes.subList(page * 7, Math.min(page * 7 + 7, currentRecipes.size()))), currentRecipes.size()));
    }


    private int getExperience() {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation().toString(), UncraftEverythingConfig.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && slots.getStack(inputSlots[0]).isIn(tagKey.get())) {
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

        ItemStack inputStack = slots.getStack(inputSlots[0]);
        if (inputStack.getCount() < currentRecipe.getInput().getCount()) {
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();
        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (i >= outputSlots.length) return false;

            ItemStack slotStack = slots.getStack(this.outputSlots[i]);

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

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (slots.getStack(inputSlots[0]).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (i >= outputSlots.length) return false;

            ItemStack slotStack = slots.getStack(this.outputSlots[i]);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.areItemsAndComponentsEqual(slotStack, result)) {
                return false;
            }

            if (slotStack.getCount() + result.getCount() * 2 > slotStack.getMaxCount()) {
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

    public ImplementedInventory getSlots() {
        return slots;
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
