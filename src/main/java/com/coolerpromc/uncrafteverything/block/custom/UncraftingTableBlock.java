package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class UncraftingTableBlock extends BaseEntityBlock {
    public UncraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(UncraftingTableBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer){
            BlockEntity entity = level.getBlockEntity(blockPos);
            if (entity instanceof UncraftingTableBlockEntity blockEntity){
                player.openMenu(blockEntity, blockPos);
                blockEntity.getOutputStacks();
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    PacketDistributor.sendToPlayer(serverPlayer, new UncraftingTableDataPayload(blockEntity.getBlockPos(), blockEntity.getCurrentRecipes()));
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }
        else{
            PacketDistributor.sendToServer(new RequestConfigPayload());
        }

        return ItemInteractionResult.SUCCESS;
    }
}
