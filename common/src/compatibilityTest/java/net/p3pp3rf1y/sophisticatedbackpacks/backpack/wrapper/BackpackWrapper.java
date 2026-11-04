package net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper;

import net.minecraft.world.item.ItemStack;

// Mimics the public read APIs; a Fabric handler exposes getSlots, a NeoForge handler size.
public class BackpackWrapper {
    public static Object inventory = new NeoHandler(54), upgrades = new NeoHandler(4);
    public static int columns;
    public static boolean broken;
    public static int refreshes;
    public static BackpackWrapper fromStack(ItemStack stack){
        return new BackpackWrapper();
    }

    public void onContentsUpdated(){
        refreshes++;
    }

    public static BackpackWrapper fromStackNoUuidCache(ItemStack stack){
        if (broken) throw new IllegalStateException("Unsupported API fixture");
        return new BackpackWrapper();
    }

    public Object getInventoryHandler(){
        return inventory;
    }

    public Object getUpgradeHandler(){
        return upgrades;
    }

    public int getColumnsTaken(){
        return columns;
    }

    public static class FabricHandler {
        public final ItemStack[] stacks;
        public FabricHandler(int size){
            stacks = new ItemStack[size];
            java.util.Arrays.fill(stacks, ItemStack.EMPTY);
        }
        public int getSlots(){
            return stacks.length;
        }
        public ItemStack getStackInSlot(int slot){
            return stacks[slot];
        }
    }

    public static class NeoHandler extends FabricHandler {
        public NeoHandler(int size){
            super(size);
        }
        public int size(){
            return stacks.length;
        }
    }
}
