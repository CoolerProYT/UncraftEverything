package com.coolerpromc.uncrafteverything.block.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AutoUncraftingTableBlock extends BaseEntityBlock {
    public static BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

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

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoUncraftingTableBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @org.jspecify.annotations.Nullable Orientation wireOrientation, boolean notify) {
        if (!world.isClientSide()){
            boolean powered = world.hasNeighborSignal(pos);
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                blockEntity.setActive(powered);
                world.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
        }
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer){
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AutoUncraftingTableBlockEntity blockEntity){
                player.openMenu(blockEntity);
                blockEntity.getOutputStacks(blockEntity.getInputHandler(), true);
                if (!world.isClientSide()) {
                    world.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                    int fromIndex = blockEntity.getPage() * 7;
                    if (fromIndex >= blockEntity.getCurrentRecipes().size()) {
                        fromIndex = blockEntity.getCurrentRecipes().size();
                    }
                    int toIndex = Math.min(fromIndex + 7, blockEntity.getCurrentRecipes().size());
                    ServerPlayNetworking.send(serverPlayer, new UncraftingTableDataPayload(blockEntity.getBlockPos(), new ArrayList<>(blockEntity.getCurrentRecipes().subList(fromIndex, toIndex)), blockEntity.getCurrentRecipes().size()));
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
    public @org.jspecify.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    public @org.jspecify.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(ACTIVE, false).setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(POWERED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING))).setValue(ACTIVE, state.getValue(ACTIVE)).setValue(POWERED, state.getValue(POWERED));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(ACTIVE, state.getValue(ACTIVE)).setValue(POWERED, state.getValue(POWERED));
    }
}
