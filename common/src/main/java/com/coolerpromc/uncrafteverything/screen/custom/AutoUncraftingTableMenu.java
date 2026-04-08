package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ServerBoundCloseMenuPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AutoUncraftingTableMenu extends AbstractUncraftingMenu<AutoUncraftingTableBlockEntity> {
    public AutoUncraftingTableMenu(int pContainerId, Inventory inventory, BlockPos blockPos){
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(blockPos));
    }

    public AutoUncraftingTableMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity){
        super(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU.get(), pContainerId, (AutoUncraftingTableBlockEntity) blockEntity, inventory.player.level(), inventory.player, ((AutoUncraftingTableBlockEntity) blockEntity).getData());

        this.addSlot(new Slot(this.blockEntity.getInputHandler(), 0, 26, 35));

        for (int i = 0; i < this.blockEntity.getOutputHandler().getContainerSize(); i ++){
            this.addSlot(new Slot(this.blockEntity.getOutputHandler(), i, 98 + 18 * (i % 3), 17 + (i / 3) * 18){
                @Override
                public boolean mayPlace(ItemStack stack) {
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
    public void removed(Player player) {
        super.removed(player);
        if (this.level.isClientSide()){
            Services.NETWORK.sendToServer(new ServerBoundCloseMenuPayload(this.blockEntity.getBlockPos()));
        }
    }
}