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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
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
import org.jetbrains.annotations.Nullable;

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
    private ServerPlayerEntity player;
    private final PropertyDelegate data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private int status = -1;
    private ItemStack currentStack = ItemStack.EMPTY;

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
            ServerPlayNetworking.send(player, UncraftingTableDataPayload.ID, UncraftingTableDataPayload.encode(new UncraftingTableDataPayload(this.pos, new ArrayList<>(this.getCurrentRecipes())), PacketByteBufs.create()));

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
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        Inventories.writeNbt(nbt, inventory);
        NbtList listTag = new NbtList();
        for (UncraftingTableRecipe recipe : currentRecipes) {
            NbtCompound recipeTag = new NbtCompound();
            recipeTag.put("recipe", recipe.serializeNbt());
            listTag.add(recipeTag);
        }
        nbt.put("current_recipes", listTag);
        if (currentRecipe != null) {
            nbt.put("current_recipe", currentRecipe.serializeNbt());
        }
        nbt.putInt("experience", experience);
        nbt.putInt("experienceType", experienceType);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        Inventories.readNbt(nbt, inventory);
        if (nbt.contains("current_recipes", NbtList.LIST_TYPE)){
            NbtList listTag = nbt.getList("current_recipes", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound recipeTag = listTag.getCompound(i);
                currentRecipes.add(UncraftingTableRecipe.deserializeNbt(recipeTag.getCompound("recipe")));
            }
        }
        if (nbt.contains("current_recipe", NbtElement.COMPOUND_TYPE)){
            currentRecipe = UncraftingTableRecipe.deserializeNbt(nbt.getCompound("current_recipe"));
        }
        experience = nbt.getInt("experience");
        experienceType = nbt.getInt("experienceType");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
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
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && inputStack.getNbt() != null && !inputStack.getNbt().contains("Trim"))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasNbt())
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

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(this.getStack(inputSlots[0])) && inputStack.getNbt() != null && !inputStack.getNbt().contains("Trim")){
                this.status = ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasNbt()){
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
            if (recipeHolder instanceof ShapedRecipe shapedRecipe){
                if (shapedRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() < shapedRecipe.output.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.get(inputStack).isEmpty()){
                    return false;
                }
                return shapedRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.output.getCount();
            }

            if (recipeHolder instanceof ShapelessRecipe shapelessRecipe){
                if (shapelessRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() < shapelessRecipe.output.getCount()){
                    this.status = NO_ENOUGH_INPUT;
                }
                if (!EnchantmentHelper.get(inputStack).isEmpty()){
                    return false;
                }
                return shapelessRecipe.output.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.output.getCount();
            }

            if(recipeHolder instanceof ShulkerBoxColoringRecipe transmuteRecipe){
                return inputStack.isIn(ConventionalItemTags.SHULKER_BOXES) && !inputStack.isOf(Items.SHULKER_BOX);
            }

            if (recipeHolder instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                if (!EnchantmentHelper.get(inputStack).isEmpty()){
                    return false;
                }
                return inputStack.isOf(smithingTransformRecipe.result.getItem());
            }

            if (recipeHolder instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }

                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(this.world.getRegistryManager(), inputStack);
                if (armorTrim.isPresent()){
                    Ingredient ingredient = smithingTrimRecipe.addition;
                    Optional<RegistryEntry.Reference<ArmorTrimPattern>> trimPatternReference = ArmorTrimPatterns.get(this.world.getRegistryManager(), smithingTrimRecipe.template.getMatchingStacks()[0]);
                    if (ingredient != Ingredient.EMPTY && trimPatternReference.isPresent() && armorTrim.get().getPattern().equals(trimPatternReference.get())){
                        return true;
                    }
                }
            }

            if (this.status == -1){
                this.status = NO_RECIPE;
            }
            return false;
        }).toList();

        if (!recipes.isEmpty() || inputStack.isOf(Items.TIPPED_ARROW) || (UncraftEverythingConfig.allowEnchantedItems && !EnchantmentHelper.get(inputStack).isEmpty() && !inputStack.getItem().equals(Items.ENCHANTED_BOOK))){
            this.status = -1;
            this.experience = getExperience();
            this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.isOf(Items.TIPPED_ARROW)){
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

        if (!EnchantmentHelper.get(inputStack).isEmpty() && recipes.isEmpty() && !inputStack.getItem().equals(Items.ENCHANTED_BOOK)){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(inputStack);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.set(enchantments, book);
            ItemStack output = new ItemStack(inputStack.getItem(), 1);
            output.setDamage(inputStack.getDamage());

            outputStack.addOutput(output);
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (Recipe<?> r : recipes) {
            if (r instanceof ShulkerBoxColoringRecipe transmuteRecipe && inputStack.isIn(ConventionalItemTags.SHULKER_BOXES) && !inputStack.isOf(Items.SHULKER_BOX)){
                List<Ingredient> ingredients = new ArrayList<>();

                Ingredient shulkerBoxIngredient = Ingredient.fromTag(ConventionalItemTags.SHULKER_BOXES);
                ingredients.add(shulkerBoxIngredient);

                Ingredient dyeIngredient = Ingredient.ofItems(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) inputStack.getItem()).getBlock()).getColor())));
                ingredients.add(dyeIngredient);

                List<List<Item>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients);
                NbtCompound itemContainerContents = inputStack.getNbt();

                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (itemStack.isIn(ConventionalItemTags.SHULKER_BOXES)){
                                itemStack.setNbt(itemContainerContents);
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
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.output.getItem(), shapedRecipe.output.getCount()));
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
                        for (var x : allIngredients.entrySet()){
                            if (inputStack.getItem().canRepair(inputStack, new ItemStack(x.getKey(), x.getValue()))){
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
                    outputs.add(outputStack);
                }
            }

            if (r instanceof ShapelessRecipe shapelessRecipe) {
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.input);

                if (inputStack.hasNbt() && inputStack.getSubNbt("Fireworks") != null) {
                    NbtCompound compoundTag = inputStack.getSubNbt("Fireworks");
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
                        for (var x : allIngredients.entrySet()){
                            if (inputStack.getItem().canRepair(inputStack, new ItemStack(x.getKey(), x.getValue()))){
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
                    outputs.add(outputStack);
                }
            }

            if (r instanceof SmithingTransformRecipe smithingTransformRecipe){
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                ingredients.add(smithingTransformRecipe.template);

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(smithingTransformRecipe.result);

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            if (item.getDefaultStack().isDamageable()){
                                stack.setDamage(inputStack.getDamage());
                            }
                            stack.increment(1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getDefaultStack().isDamageable()){
                                itemStack.setDamage(inputStack.getDamage());
                                if (itemStack.getDamage() >= itemStack.getMaxDamage()){
                                    itemStack = ItemStack.EMPTY;
                                }
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r instanceof SmithingTrimRecipe smithingTrimRecipe){
                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(this.world.getRegistryManager(), inputStack);
                Ingredient additionIngredient = smithingTrimRecipe.addition;

                DefaultedList<Ingredient> ingredients = DefaultedList.of();
                Arrays.stream(smithingTrimRecipe.base.getMatchingStacks()).filter(itemStack -> itemStack.isOf(inputStack.getItem())).forEach(itemStack -> ingredients.add(Ingredient.ofStacks(itemStack)));
                ingredients.add(smithingTrimRecipe.template);
                Arrays.stream(smithingTrimRecipe.addition.getMatchingStacks()).filter(itemStack -> {
                    if (armorTrim.isPresent()){
                        RegistryKey<ArmorTrimMaterial> armorTrimKey = armorTrim.get().getMaterial().getKey().orElse(null);
                        RegistryKey<Item> itemResourceKey =  itemStack.getRegistryEntry().getKey().orElse(null);
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.getValue().getPath().contains(armorTrimKey.getValue().getPath());
                        }
                    }
                    return false;
                }).forEach(itemStack -> ingredients.add(Ingredient.ofStacks(itemStack)));

                List<List<Item>> allIngredientCombinations = getAllIngredientCombinations(ingredients);
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.get(inputStack);

                // Create a recipe for each combination
                for (List<Item> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Item item : ingredientCombination) {
                        if (outputStack.getOutputs().contains(item.getDefaultStack())) {
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultStack()));
                            if (item.getDefaultStack().isOf(ingredients.get(0).getMatchingStacks()[0].getItem())){
                                EnchantmentHelper.set(itemEnchantments, stack);
                                stack.setDamage(inputStack.getDamage());
                            }
                            stack.setCount(stack.getCount() + 1);
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultStack()), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item, 1);
                            if (item.getDefaultStack().isOf(ingredients.get(0).getMatchingStacks()[0].getItem())){
                                EnchantmentHelper.set(itemEnchantments, itemStack);
                                itemStack.setDamage(inputStack.getDamage());
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
            ServerPlayNetworking.send(player, UncraftingRecipeSelectionRequestPayload.TYPE, PacketByteBufs.create());
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
        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().getMatchingStacks().isEmpty()) {
            for (var holder : ingredient.getCustomIngredient().getMatchingStacks()) {
                items.add(holder.getItem());
            }
        }
        // Handle regular item ingredients
        else {
            try {
                items = Arrays.stream(ingredient.getMatchingStacks())
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
                    if (item.getTranslationKey().contains("shulker_box")){
                        return item == Items.SHULKER_BOX;
                    }
                    return item.getRecipeRemainder(item.getDefaultStack()) == ItemStack.EMPTY || item.getRecipeRemainder(item.getDefaultStack()).getItem() != item.getDefaultStack().getItem();
                })
                .sorted(Comparator.comparing(Item::getTranslationKey))
                .toList();
    }

    // Helper method to get all possible combinations of ingredients for shaped recipes
    private List<List<Item>> getAllIngredientCombinations(DefaultedList<Ingredient> ingredients) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = Optional.of(ingredients.get(i));
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

    public void handleUncraftButtonClicked(boolean hasShiftDown){
        if (hasShiftDown){
            while (hasRecipe() && hasEnoughExperience()) {
                processUncraft();
            }
        }
        else{
            if (hasRecipe() && hasEnoughExperience()) {
                processUncraft();
            }
        }
    }

    private void processUncraft(){
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
            ServerPlayNetworking.send(player, UncraftingTableDataPayload.ID, UncraftingTableDataPayload.encode(new UncraftingTableDataPayload(this.getPos(), this.getCurrentRecipes()), PacketByteBufs.create()));
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
