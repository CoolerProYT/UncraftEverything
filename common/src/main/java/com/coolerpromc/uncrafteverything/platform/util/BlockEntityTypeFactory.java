package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockEntityTypeFactory<T extends BlockEntity> {
    T create(BlockPos pos, BlockState state);
}
