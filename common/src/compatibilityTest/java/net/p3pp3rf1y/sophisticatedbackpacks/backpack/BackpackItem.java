package net.p3pp3rf1y.sophisticatedbackpacks.backpack;

import net.minecraft.world.item.Item;

public class BackpackItem extends Item {
    private final int slots, upgrades;
    public BackpackItem(Properties properties, int slots, int upgrades){
        super(properties.stacksTo(1));
        this.slots = slots;
        this.upgrades = upgrades;
    }

    public int getNumberOfSlots(){
        return slots;
    }

    public int getNumberOfUpgradeSlots(){
        return upgrades;
    }
}
