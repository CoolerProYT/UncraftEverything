package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UncraftingTableBlock extends BlockWithEntity {
    public UncraftingTableBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(UncraftingTableBlock::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new UncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient){
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof UncraftingTableBlockEntity blockEntity){
                player.openHandledScreen(blockEntity);
                blockEntity.getOutputStacks();
                world.updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                for (ServerPlayerEntity playerEntity : PlayerLookup.around((ServerWorld) world, new Vec3d(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ()), 10)){
                    ServerPlayNetworking.send(playerEntity, new UncraftingTableDataPayload(blockEntity.getPos(), blockEntity.getCurrentRecipes()));
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }

        return ActionResult.SUCCESS;
    }
}
