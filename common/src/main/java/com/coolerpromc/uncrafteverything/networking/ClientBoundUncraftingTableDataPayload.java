package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingMenu;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;

import java.util.List;

public record ClientBoundUncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes, int size, boolean shouldSendPacket) implements CustomPacketPayload {
    public ClientBoundUncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes, int size){
        this(blockPos, recipes, size, true);
    }
    public static final Type<ClientBoundUncraftingTableDataPayload> TYPE = new Type<>(Constants.id("uncrafting_table_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundUncraftingTableDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientBoundUncraftingTableDataPayload::blockPos,
                    UncraftingTableRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ClientBoundUncraftingTableDataPayload::recipes,
                    ByteBufCodecs.INT,
                    ClientBoundUncraftingTableDataPayload::size,
                    ByteBufCodecs.BOOL,
                    ClientBoundUncraftingTableDataPayload::shouldSendPacket,
                    ClientBoundUncraftingTableDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level world = minecraft.level;
            Screen screen = minecraft.screen;

            if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen){
                if (world.getBlockEntity(this.blockPos()) instanceof AbstractUncraftingTableBE){
                    uncraftingTableScreen.updateFromBlockEntity(this.recipes(), this.size(), this.shouldSendPacket());
                }
            }
        });
    }
}