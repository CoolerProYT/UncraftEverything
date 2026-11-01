package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;

import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public final class ForgeUncraftingTableBlockEntity extends UncraftingTableBlockEntity {
    private LazyOptional<IItemHandler> input =
            LazyOptional.of(() -> new ForgeInventoryAdapter(getInputHandler()));
    private LazyOptional<IItemHandler> output =
            LazyOptional.of(() -> new ForgeInventoryAdapter(getOutputHandler()));

    public ForgeUncraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER)
            return (side == Direction.DOWN ? output : input).cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        input.invalidate();
        output.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        input = LazyOptional.of(() -> new ForgeInventoryAdapter(getInputHandler()));
        output = LazyOptional.of(() -> new ForgeInventoryAdapter(getOutputHandler()));
    }
}
