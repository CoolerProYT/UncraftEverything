package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.CloseMenuPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class AutoUncraftingTableMenu extends AbstractUncraftingMenu<AutoUncraftingTableBlockEntity> {
    public AutoUncraftingTableMenu(int pContainerId, PlayerInventory inventory, BlockPos blockPos){
        this(pContainerId, inventory, inventory.player.getEntityWorld().getBlockEntity(blockPos));
    }

    public AutoUncraftingTableMenu(int pContainerId, PlayerInventory inventory, BlockEntity blockEntity){
        super(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU, pContainerId, (AutoUncraftingTableBlockEntity) blockEntity, inventory.player.getEntityWorld(), inventory.player, ((AutoUncraftingTableBlockEntity) blockEntity).getData());

        this.addSlot(new Slot(this.blockEntity.getInputHandler(), 0, 26, 35));

        for (int i = 0; i < this.blockEntity.getOutputHandler().size(); i ++){
            this.addSlot(new Slot(this.blockEntity.getOutputHandler(), i, 98 + 18 * (i % 3), 17 + (i / 3) * 18){
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }
            });
        }

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    public String getExpType(){
        return this.data.get(1) == 0 ? "Point" : "Level";
    }

    public int getExpAmount(){
        return this.data.get(0);
    }

    public int getStatus(){
        return this.data.get(2);
    }

    public int getExpPoints(){
        return this.data.get(3) - getTotalXpForLevel(getExpLevels());
    }

    public int getTotalXpForLevel(int experienceLevel) {
        if (experienceLevel <= 16) {
            return experienceLevel * experienceLevel + 6 * experienceLevel;
        } else if (experienceLevel <= 31) {
            return (int)(2.5 * experienceLevel * experienceLevel - 40.5 * experienceLevel + 360);
        } else {
            return (int)(4.5 * experienceLevel * experienceLevel - 162.5 * experienceLevel + 2220);
        }
    }

    public int getExpLevels(){
        return this.data.get(4);
    }

    public int getExpProgress(){
        return this.data.get(5);
    }

    public UncraftEverythingConfig.ExperienceType getTypeToAdd(){
        return this.data.get(6) == 0 ? UncraftEverythingConfig.ExperienceType.LEVEL : UncraftEverythingConfig.ExperienceType.POINT;
    }

    public int getAmountToAdd(){
        return this.data.get(7);
    }

    public int getPage(){
        return this.data.get(8);
    }

    public int getIndex(){
        return this.data.get(9);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (this.level.isClient()){
            ClientPlayNetworking.send(new CloseMenuPayload(this.blockEntity.getPos()));
        }
    }
}