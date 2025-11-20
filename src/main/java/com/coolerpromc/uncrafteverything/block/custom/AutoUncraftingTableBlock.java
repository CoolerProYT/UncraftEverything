package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AutoUncraftingTableBlock extends BaseEntityBlock {
    public AutoUncraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AutoUncraftingTableBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoUncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()){
            boolean powered = level.hasNeighborSignal(pos);
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                blockEntity.setActive(powered);
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                player.openMenu(blockEntity, pos);
                blockEntity.getOutputStacks(blockEntity.getInputHandler(), false);
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    int fromIndex = blockEntity.getPage() * 7;
                    if (fromIndex >= blockEntity.getCurrentRecipes().size()) {
                        fromIndex = blockEntity.getCurrentRecipes().size();
                    }
                    int toIndex = Math.min(fromIndex + 7, blockEntity.getCurrentRecipes().size());
                    PacketDistributor.sendToPlayer(serverPlayer, new UncraftingTableDataPayload(blockEntity.getBlockPos(), new ArrayList<>(blockEntity.getCurrentRecipes().subList(fromIndex, toIndex)), blockEntity.getCurrentRecipes().size()));
                    blockEntity.setChanged();
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get(), (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1));
    }
}
