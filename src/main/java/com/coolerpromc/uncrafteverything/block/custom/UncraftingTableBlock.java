package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.networking.ClientConfigSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class UncraftingTableBlock extends BaseEntityBlock {
    public UncraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UncraftingTableBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!level.isClientSide && pPlayer instanceof ServerPlayer serverPlayer) {
            BlockEntity entity = level.getBlockEntity(pPos);
            if (entity instanceof UncraftingTableBlockEntity blockEntity){
                NetworkHooks.openScreen(serverPlayer, blockEntity, pPos);
                blockEntity.getOutputStacks(blockEntity.getInputHandler(), false);
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    blockEntity.updatePage(0);
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }
        else{
            ClientConfigSyncPayload.INSTANCE.sendToServer(new ClientConfigSyncPayload(UncraftEverythingClientConfig.CONFIG.autoMoveToInventory.get()));
        }

        return InteractionResult.SUCCESS;
    }
}
