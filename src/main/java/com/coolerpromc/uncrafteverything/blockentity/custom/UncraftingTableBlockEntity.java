package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

@SuppressWarnings({"unused", "NullableProblems"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private ServerPlayer player;
    private final ContainerData data;
    private int experience = 0;
    private int experienceType; // 0 = POINT, 1 = LEVEL
    private ItemStack currentStack = ItemStack.EMPTY;
    private int page = 0;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            getOutputStacks();
            if (level != null && !level.isClientSide() && player != null) {
                if (currentStack.getItem() != getResource(0).getItem() && !getResource(0).isEmpty()){
                    for (int i = 0; i < getOutputHandler().size(); i++) {
                        ItemStack outputStack = getOutputHandler().copyToList().get(i);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(outputStack);
                            getOutputHandler().set(i, ItemResource.EMPTY, 0);
                            setChanged();
                        }
                    }
                }
                currentStack = getResource(0).toStack(getAmountAsInt(0));
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                PacketDistributor.sendToPlayer(player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getCapacity(index, resource);
            }
            return toReturn;
        }
    };

    private final ItemStacksResourceHandler outputHandler = new ItemStacksResourceHandler(9){
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getCapacity(index, resource);
            }
            return toReturn;
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.getRaw() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0 -> experience;
                    case 1 -> experienceType;
                    case 2 -> status.getIndex();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0 -> experience = value;
                    case 1 -> experienceType = value;
                    case 2 -> status = Status.byIndex(value);
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
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        inputHandler.serialize(valueOutput.child("input"));
        outputHandler.serialize(valueOutput.child("output"));

        valueOutput.putInt("experience", experience);
        valueOutput.putInt("experienceType", experienceType);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        inputHandler.deserialize(valueInput.childOrEmpty("input"));
        outputHandler.deserialize(valueInput.childOrEmpty("output"));

        experience = valueInput.getIntOr("experience", 0);
        experienceType = valueInput.getIntOr("experienceType", 0);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStacksResourceHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStacksResourceHandler getOutputHandler() {
        return outputHandler;
    }

    public ResourceLocation inputStackLocation() {
        return BuiltInRegistries.ITEM.getKey(inputHandler.getResource(0).getItem());
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerLevel serverLevel) || player == null) return;
        this.status = Status.BLANK;
        ItemStack inputStack = this.inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0));
        if (!UncraftingTableHelpers.validateInput(inputStack, player, this)){
            currentRecipes.clear();
            currentRecipe = null;
            experience = 0;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            return;
        }

        List<RecipeHolder<?>> recipes = UncraftingTableHelpers.findRecipe(serverLevel, inputStack, this);

        if (!recipes.isEmpty() || inputStack.is(Items.TIPPED_ARROW) || (UncraftEverythingConfig.CONFIG.allowEnchantedItems.getAsBoolean() && inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY)) {
            this.status = Status.BLANK;
            this.experience = getExperience();
            this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.getRaw() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        Tuple<List<UncraftingTableRecipe>, Boolean> outputs = UncraftingTableHelpers.getOutputs(inputStack, recipes, this);
        if (!outputs.getB()) return;
        this.currentRecipes = outputs.getA();

        if (!currentRecipes.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new UncraftingRecipeSelectionRequestPayload());
            if(!hasRecipe()){
                this.status = Status.NO_SUITABLE_OUTPUT_SLOT;
            }
            else{
                if (!hasEnoughExperience()) {
                    this.status = Status.NOT_ENOUGH_EXP;
                }
            }
        }
        else{
            if (this.status == Status.BLANK && !inputStack.isEmpty()){
                this.status = Status.NO_RECIPE_FOUND;
            }
        }
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

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (inputHandler.getResource(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, this.inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0))).getLeft()){
                    this.status = Status.LOCKED_ITEM;
                }
                else{
                    this.status = Status.NO_SUITABLE_OUTPUT_SLOT;
                }
            }
        }
        else{
            if (hasEnoughExperience()){
                this.status = Status.BLANK;
            }
            else{
                this.status = Status.NOT_ENOUGH_EXP;
            }
        }
    }

    public void updatePage(int page){
        this.page = page;
        PacketDistributor.sendToPlayer(player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(page * 7, Math.min(page * 7 + 7, currentRecipes.size()))), currentRecipes.size()));
    }

    private int getExperience() {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation().toString(), UncraftEverythingConfig.CONFIG.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && inputHandler.getResource(0).is(tagKey.get())) {
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

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return i;
            }
        }
        return -1;
    }

    private void processUncraft(boolean hasNext){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (ItemStack output : outputs) {
            int slot = this.findSuitableOutputSlot(output);
            if (slot != -1) {
                ItemStack slotStack = outputHandler.getResource(slot).toStack(outputHandler.getAmountAsInt(slot));

                if (slotStack.isEmpty()) {
                    outputHandler.set(slot, ItemResource.of(output.copy()), output.getCount());
                } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                    outputHandler.set(slot, ItemResource.of(slotStack), slotStack.getCount());
                }

                if (UncraftEverythingClientConfig.CONFIG.autoMoveToInventory.getAsBoolean()){
                    player.getInventory().placeItemBackInInventory(outputHandler.copyToList().get(slot));
                    outputHandler.set(slot, ItemResource.EMPTY, 0);
                }
            }
        }

        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            player.giveExperiencePoints(-getExperience());
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            player.giveExperienceLevels(-getExperience());
        }

        if (hasNext){
            inputHandler.extractItemWithoutTriggerChanges(0, this.currentRecipe.getInput().getCount(), false);
        }
        else{
            try(Transaction tx = Transaction.open(null)){
                int count = inputHandler.extract(0,ItemResource.of(this.currentRecipe.getInput().getItem(), this.currentRecipe.getInput().getComponentsPatch()), this.currentRecipe.getInput().getCount(), tx);
                if (count == this.currentRecipe.getInput().getCount()){
                    tx.commit();
                }
            }
        }
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private boolean hasRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        ItemStack inputStack = inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0));
        if (inputStack.getCount() < currentRecipe.getInput().getCount()) {
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (ItemStack result : results) {
            if (cannotInsertAmountIntoOutputSlot(result) || cannotInsertItemIntoOutputSlot(result)) {
                return false;
            }
        }

        return checkSlot(results);
    }

    private boolean checkSlot(List<ItemStack> results){
        int count = results.size();
        int emptyCount = 0;

        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
            if(!stackInSlot.isEmpty()){
                for (ItemStack result : results){
                    if(stackInSlot.getItem() == result.getItem()){
                        if(stackInSlot.getCount() + result.getCount() <= 64){
                            emptyCount++;
                        }
                    }
                }
            }
            else {
                emptyCount++;
            }
        }

        return emptyCount >= count;
    }

    private boolean cannotInsertAmountIntoOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
            if (stackInSlot.isEmpty() || ItemStack.isSameItemSameComponents(stackInSlot, item)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0)).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (ItemStack result : results) {
            if (cannotInsertAmountIntoOutputSlot(result) || cannotInsertItemIntoOutputSlot(result)) {
                return false;
            }
        }

        return checkSlot(results);
    }
    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    public ContainerData getData() {
        return data;
    }
}