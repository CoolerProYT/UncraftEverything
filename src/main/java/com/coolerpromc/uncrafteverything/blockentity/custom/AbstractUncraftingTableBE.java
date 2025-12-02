package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

public abstract class AbstractUncraftingTableBE extends BlockEntity {
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
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getPage() {
        return page;
    }

    public void updatePage(int page){
        this.page = page;
        PacketDistributor.sendToPlayer(player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(page * 7, Math.min(page * 7 + 7, currentRecipes.size()))), currentRecipes.size()));
    }

    public void getOutputStacks(ItemStackHandler inputHandler, boolean isAuto) {
        if (!(level instanceof ServerLevel serverLevel) || (player == null && !isAuto)) return;
        this.status = Status.BLANK;
        ItemStack inputStack = inputHandler.getStackInSlot(0);
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
            this.experience = getExperience(inputHandler);
            this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.getRaw() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        }

        Tuple<List<UncraftingTableRecipe>, Boolean> outputs = UncraftingTableHelpers.getOutputs(inputStack, recipes, this);
        if (!outputs.getB()) return;
        this.currentRecipes = outputs.getA();

        if (!currentRecipes.isEmpty()) {
            if (!isAuto){
                PacketDistributor.sendToPlayer(player, new UncraftingRecipeSelectionRequestPayload());
            }
            else{
                if (currentRecipe == null){
                    currentRecipe = currentRecipes.getFirst();
                }
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

    protected int getExperience(ItemStackHandler inputHandler) {
        Map<String, Integer> experienceMap = PerItemExpCostConfig.getPerItemExp();
        int experience = experienceMap.getOrDefault(inputStackLocation(inputHandler).toString(), UncraftEverythingConfig.CONFIG.getExperience());

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
                if (Pattern.matches(regex, inputStackLocation(inputHandler).toString())){
                    experience = exp.getValue();
                    break;
                }
            }
        }

        return experience;
    }

    public ResourceLocation inputStackLocation(ItemStackHandler inputHandler) {
        return BuiltInRegistries.ITEM.getKey(inputHandler.getStackInSlot(0).getItem());
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
}
