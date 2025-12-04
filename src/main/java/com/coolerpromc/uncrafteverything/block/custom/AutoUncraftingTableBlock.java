package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AutoUncraftingTableBlock extends BlockWithEntity {
    public static BooleanProperty ACTIVE = BooleanProperty.of("active");
    public static BooleanProperty POWERED = BooleanProperty.of("powered");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public AutoUncraftingTableBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(AutoUncraftingTableBlock::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AutoUncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (!world.isClient()){
            boolean powered = world.isReceivingRedstonePower(pos);
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                blockEntity.setActive(powered);
                world.setBlockState(pos, state.with(POWERED, powered), 3);
            }
        }
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer){
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                player.openHandledScreen(blockEntity);
                blockEntity.getOutputStacks(blockEntity.getInputHandler(), true);
                if (!world.isClient()) {
                    world.updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                    int fromIndex = blockEntity.getPage() * 7;
                    if (fromIndex >= blockEntity.getCurrentRecipes().size()) {
                        fromIndex = blockEntity.getCurrentRecipes().size();
                    }
                    int toIndex = Math.min(fromIndex + 7, blockEntity.getCurrentRecipes().size());
                    ServerPlayNetworking.send(serverPlayer, new UncraftingTableDataPayload(blockEntity.getPos(), new ArrayList<>(blockEntity.getCurrentRecipes().subList(fromIndex, toIndex)), blockEntity.getCurrentRecipes().size()));
                    blockEntity.markDirty();
                }
            }
            else {
                throw new IllegalStateException("Container provider is missing");
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(ACTIVE, false).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(POWERED, false);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVE, FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING))).with(ACTIVE, state.get(ACTIVE)).with(POWERED, state.get(POWERED));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING))).with(ACTIVE, state.get(ACTIVE)).with(POWERED, state.get(POWERED));
    }
}
