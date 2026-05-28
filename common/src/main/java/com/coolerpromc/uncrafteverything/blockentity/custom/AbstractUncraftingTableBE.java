package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractUncraftingTableBE extends BlockEntity implements WorldlyContainer {
    protected List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    public UncraftingTableRecipe currentRecipe = null;
    protected ServerPlayer player;
    protected int experience = 0;
    protected int experienceType; // 0 = POINT, 1 = LEVEL
    protected ItemStack currentStack = ItemStack.EMPTY;
    protected int page = 0;
    public Status status = Status.BLANK;

    public AbstractUncraftingTableBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @org.jspecify.annotations.Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getPage() {
        return page;
    }

    public void updatePage(int page){
        this.page = page;
        Services.NETWORK.sendToPlayer(player, new ClientBoundUncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(page * 7, Math.min(page * 7 + 7, currentRecipes.size()))), currentRecipes.size()));
    }

    public void getOutputStacks(ImplementedInventory inputHandler, boolean isAuto) {
        if (!(level instanceof ServerLevel serverLevel) || (player == null && !isAuto)) return;
        this.status = Status.BLANK;
        ItemStack inputStack = inputHandler.getItem(0);
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

        if (!recipes.isEmpty() || inputStack.is(Items.TIPPED_ARROW) || (UncraftEverythingConfig.CONFIG.allowEnchantedItems() && inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY)) {
            this.status = Status.BLANK;
            this.experience = getExperience(inputHandler);
            this.experienceType = UncraftEverythingConfig.CONFIG.experienceType() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        Pair<List<UncraftingTableRecipe>, Boolean> outputs = UncraftingTableHelpers.getOutputs(inputStack, recipes, this);
        if (!outputs.getRight()) return;
        if (UncraftEverythingConfig.CONFIG.prioritizeVanillaIngredientRecipe()){
            this.currentRecipes = new ArrayList<>(outputs.getLeft().stream().sorted(Comparator.comparingInt(this::countVanillaIngredients).reversed()).toList());
        }
        else{
            this.currentRecipes = outputs.getLeft();
        }

        if (!currentRecipes.isEmpty()) {
            if (!isAuto){
                Services.NETWORK.sendToPlayer(player, new ClientBoundUncraftingRecipeSelectionRequestPayload());
            }
            else{
                setCurrentRecipe(currentRecipes);
            }
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

    public int countVanillaIngredients(UncraftingTableRecipe recipe){
        int count = 0;
        for (ItemStack stack : recipe.getOutputs()){
            if (stack.typeHolder().unwrapKey().get().identifier().getNamespace().equals("minecraft")) count++;
        }
        return count;
    }

    protected int getExperience(ImplementedInventory inputHandler) {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.CONFIG.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation(inputHandler).toString(), UncraftEverythingConfig.CONFIG.experience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<TagKey<Item>> tagKey = UncraftEverythingConfig.CONFIG.tryParseTagKey(tagName);
                if (tagKey.isPresent() && inputHandler.getItem(0).is(tagKey.get())) {
                    experience = exp.getValue();
                    break;
                }
            }

            if (exp.getKey().contains("*")){
                String regex = exp.getKey().replace("*", ".*");
                if (Pattern.matches(regex, inputStackLocation(inputHandler).toString())){
                    experience = exp.getValue();
                    break;
                }
            }
        }

        return experience;
    }

    public Identifier inputStackLocation(ImplementedInventory inputHandler) {
        return BuiltInRegistries.ITEM.getKey(inputHandler.getItem(0).getItem());
    }

    public int calculateBaseXpFromLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int)(4.5 * level * level - 162.5 * level + 2220);
        }
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public abstract boolean hasRecipe();
    public abstract boolean hasEnoughExperience();
    public abstract void handleRecipeSelection(UncraftingTableRecipe recipe);

    public void setCurrentRecipe(List<UncraftingTableRecipe> currentRecipes) {
    }
}