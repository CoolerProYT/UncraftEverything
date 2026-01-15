package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

public abstract class AbstractUncraftingTableBE extends BlockEntity implements SidedInventory {
    protected List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    public UncraftingTableRecipe currentRecipe = null;
    protected ServerPlayerEntity player;
    protected int experience = 0;
    protected int experienceType; // 0 = POINT, 1 = LEVEL
    protected ItemStack currentStack = ItemStack.EMPTY;
    protected int page = 0;
    public Status status = Status.BLANK;

    public AbstractUncraftingTableBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public @org.jspecify.annotations.Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public int getPage() {
        return page;
    }

    public void updatePage(int page){
        this.page = page;
        ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getPos(), new ArrayList<>(currentRecipes.subList(page * 7, Math.min(page * 7 + 7, currentRecipes.size()))), currentRecipes.size()));
    }

    public void getOutputStacks(ImplementedInventory inputHandler, boolean isAuto) {
        if (!(world instanceof ServerWorld serverLevel) || (player == null && !isAuto)) return;
        this.status = Status.BLANK;
        ItemStack inputStack = inputHandler.getStack(0);
        if (!UncraftingTableHelpers.validateInput(inputStack, player, this)){
            currentRecipes.clear();
            currentRecipe = null;
            experience = 0;
            markDirty();
            if (world != null && !world.isClient()) {
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
            }
            return;
        }

        List<RecipeEntry<?>> recipes = UncraftingTableHelpers.findRecipe(serverLevel, inputStack, this);

        if (!recipes.isEmpty() || inputStack.isOf(Items.TIPPED_ARROW) || (UncraftEverythingConfig.allowEnchantedItems && inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT)) {
            this.status = Status.BLANK;
            this.experience = getExperience(inputHandler);
            this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        Pair<List<UncraftingTableRecipe>, Boolean> outputs = UncraftingTableHelpers.getOutputs(inputStack, recipes, this);
        if (!outputs.getRight()) return;
        if (UncraftEverythingConfig.prioritizeVanillaIngredientRecipe){
            this.currentRecipes = new ArrayList<>(outputs.getLeft().stream().sorted(Comparator.comparingInt(this::countVanillaIngredients).reversed()).toList());
        }
        else{
            this.currentRecipes = outputs.getLeft();
        }

        if (!currentRecipes.isEmpty()) {
            if (!isAuto){
                ServerPlayNetworking.send(player, new UncraftingRecipeSelectionRequestPayload());
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
            if (stack.getRegistryEntry().getKey().get().getValue().getNamespace().equals("minecraft")) count++;
        }
        return count;
    }

    protected int getExperience(ImplementedInventory inputHandler) {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation(inputHandler).toString(), UncraftEverythingConfig.getExperience());

        for (Map.Entry<String, Integer> exp : experienceMap.entrySet()){
            if (exp.getKey().startsWith("#")){
                String tagName = exp.getKey().substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && inputHandler.getStack(0).isIn(tagKey.get())) {
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
        return Registries.ITEM.getId(inputHandler.getStack(0).getItem());
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

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public abstract boolean hasRecipe();
    public abstract boolean hasEnoughExperience();
    public abstract void handleRecipeSelection(UncraftingTableRecipe recipe);

    public void setCurrentRecipe(List<UncraftingTableRecipe> currentRecipes) {
    }
}