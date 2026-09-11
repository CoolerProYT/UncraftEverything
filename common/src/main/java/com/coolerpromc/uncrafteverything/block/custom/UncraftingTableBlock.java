package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.networking.ServerBoundClientConfigSyncPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class UncraftingTableBlock extends BaseEntityBlock {
    public UncraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer){
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof UncraftingTableBlockEntity blockEntity){
                Services.MENU.openMenu(serverPlayer, blockEntity, blockEntity.getBlockPos());
                blockEntity.getOutputStacks(blockEntity.getSlots(), false);
                if (!world.isClientSide()) {
                    world.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    blockEntity.updatePage(0);
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }
        else{
            Services.NETWORK.sendToServer(new ServerBoundClientConfigSyncPayload(UncraftEverythingClientConfig.CONFIG.autoMoveToInventory()));
        }

        return InteractionResult.SUCCESS;
    }
}
