package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class UncraftingTableBlock extends ContainerBlock {
    public UncraftingTableBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState p_149645_1_) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader iBlockReader) {
        return new UncraftingTableBlockEntity();
    }

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        if (!pLevel.isClientSide){
            TileEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof UncraftingTableBlockEntity){
                UncraftingTableBlockEntity blockEntity = (UncraftingTableBlockEntity) entity;
                NetworkHooks.openGui((ServerPlayerEntity) pPlayer, blockEntity, pPos);
                if (!pLevel.isClientSide()) {
                    pLevel.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) pPlayer), new UncraftingTableDataPayload(blockEntity.getBlockPos(), blockEntity.getCurrentRecipes()));
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }

        return ActionResultType.SUCCESS;
    }
}
