package ue.test;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.compat.mod.SophisticatedBackpacksCompat;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;

import java.util.*;
import java.util.stream.IntStream;

@Mod("ue_sophisticated_test")
public class SophisticatedIntegration {
    static int checks;
    static boolean passed;

    public SophisticatedIntegration(){
        NeoForge.EVENT_BUS.addListener(this::started);
        NeoForge.EVENT_BUS.addListener(this::stopped);
    }

    static Item item(String name){
        return BuiltInRegistries.ITEM.getValue(Identifier.parse("sophisticatedbackpacks:" + name));
    }

    static void check(boolean value, String msg){
        checks++;
        if (!value){
            throw new AssertionError(msg);
        }
    }

    void stopped(ServerStoppedEvent event){
        System.exit(passed ? 0 : 1);
    }

    void started(ServerStartedEvent event){
        try {
            var level = event.getServer().overworld();
            var table = new UncraftingTableBlockEntity(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get().defaultBlockState());
            table.setLevel(level);
            UncraftEverythingConfig.CONFIG.outputEnchantedBook.set(false);
            UncraftEverythingConfig.CONFIG.allowUnSmithing.set(true);
            UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems.set(false);
            UncraftEverythingConfig.CONFIG.restrictedModIngredients.set(List.of());
            for (String name : List.of("backpack", "iron_backpack", "gold_backpack", "diamond_backpack", "netherite_backpack", "pickup_upgrade", "advanced_pickup_upgrade", "stack_upgrade_tier_1")){
                ItemStack input = new ItemStack(item(name));
                check(!input.isEmpty(), "missing item " + name);
                var recipes = UncraftingTableHelpers.findRecipe(level, input, table);
                var outputs = UncraftingTableHelpers.getOutputs(input, recipes, table).getLeft();
                check(!outputs.isEmpty(), "no recipe for " + name + ": " + table.status);
            }
            ItemStack input = new ItemStack(item("iron_backpack"));
            var wrapper = BackpackWrapper.fromStack(input);
            wrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(Items.DIAMOND, 12));
            wrapper.getUpgradeHandler().setStackInSlot(0, new ItemStack(item("pickup_upgrade")));
            var uuid = wrapper.getContentsUuid().orElseThrow();
            var recipes = UncraftingTableHelpers.findRecipe(level, input, table);
            var outputs = UncraftingTableHelpers.getOutputs(input, recipes, table).getLeft();
            check(!outputs.isEmpty(), "filled iron backpack not supported");
            var choice = outputs.stream().filter(r -> r.getOutputs().stream().anyMatch(s -> s.is(item("backpack")))).findFirst().orElseThrow();
            ItemStack lower = choice.getOutputs().stream().filter(s -> s.is(item("backpack"))).findFirst().orElseThrow();
            check(lower.get(ModCoreDataComponents.STORAGE_UUID).equals(uuid), "UUID changed");
            check(wrapper.getInventoryHandler().getStackInSlot(0).getCount()==12,"preview changed contents");
            int lowerCapacity = lower.get(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS);
            wrapper.getInventoryHandler().setStackInSlot(lowerCapacity, new ItemStack(Items.EMERALD));
            check(!SophisticatedBackpacksCompat.canStillUncraft(input, choice, table),"stale overflow not blocked");
            wrapper.getInventoryHandler().setStackInSlot(lowerCapacity, ItemStack.EMPTY);
            check(SophisticatedBackpacksCompat.canStillUncraft(input, choice, table),"valid contents blocked");
            SophisticatedBackpacksCompat.onUncrafted(choice.getOutputs());
            var lowerWrapper = BackpackWrapper.fromStack(lower);
            check(lowerWrapper.getInventoryHandler().size()==lowerCapacity,"cached capacity not updated");
            check(lowerWrapper.getInventoryHandler().getStackInSlot(0).getCount()==12,"contents lost on downgrade");
            check(lowerWrapper.getUpgradeHandler().getStackInSlot(0).is(item("pickup_upgrade")),"installed upgrade lost");
            var lowerRecipes = UncraftingTableHelpers.findRecipe(level, lower, table);
            check(UncraftingTableHelpers.getOutputs(lower, lowerRecipes, table).getLeft().isEmpty(),"filled base backpack can be dismantled");
            UncraftEverythingConfig.CONFIG.experience.set(0);
            UncraftEverything.AUTO_MOVE = false;
            for (boolean automatic : List.of(false, true)){
                ItemStack packed = new ItemStack(item("iron_backpack"));
                var packedWrapper = BackpackWrapper.fromStack(packed);
                packedWrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(Items.DIAMOND, 17));
                var packedUuid = packedWrapper.getContentsUuid().orElseThrow();
                ItemStack result;
                if (automatic){
                    var auto = new AutoUncraftingTableBlockEntity(new BlockPos(0, 90, 0), UEBlocks.AUTO_UNCRAFTING_TABLE.get().defaultBlockState());
                    auto.setLevel(level);
                    var xpPlayer = FakePlayerFactory.getMinecraft(level);
                    xpPlayer.giveExperiencePoints(1000);
                    auto.addExperiencePoints(1000, xpPlayer);
                    auto.getInputHandler().setItem(0, packed);
                    for (int i = 0;i<auto.getOutputHandler().getContainerSize();i++) auto.getOutputHandler().setItem(i, new ItemStack(Items.STONE, 64));
                    auto.setActive(true);
                    auto.tick(level, auto.getBlockPos(), auto.getBlockState());
                    check(!auto.getInputHandler().getItem(0).isEmpty(),"full automatic output consumed input");
                    for (int i = 0;i<auto.getOutputHandler().getContainerSize();i++) auto.getOutputHandler().setItem(i, ItemStack.EMPTY);
                    auto.getOutputStacks(auto.getInputHandler(), true);
                    check(auto.hasRecipe(),"automatic table lacks safe downgrade");
                    check(auto.hasEnoughExperience(),"automatic table lacks experience: " + UncraftEverythingConfig.CONFIG.experience());
                    auto.setActive(true);
                    auto.tick(level, auto.getBlockPos(), auto.getBlockState());
                    check(auto.getInputHandler().getItem(0).isEmpty(),"automatic input not consumed");
                    result = IntStream.range(0, auto.getOutputHandler().getContainerSize()).mapToObj(auto.getOutputHandler()::getItem).filter(st -> st.is(item("backpack"))).findFirst().orElseThrow();
                }
                else{
                    table.setPlayer(FakePlayerFactory.getMinecraft(level));
                    table.getSlots().setItem(0, packed);
                    var selected = UncraftingTableHelpers.getOutputs(packed, UncraftingTableHelpers.findRecipe(level, packed, table), table).getLeft().stream().filter(r->r.getOutputs().stream().anyMatch(st->st.is(item("backpack")))).findFirst().orElseThrow();
                    table.handleRecipeSelection(selected);
                    check(table.hasRecipe(),"manual table lacks safe downgrade");
                    table.handleUncraftButtonClicked(false);
                    check(table.getSlots().getItem(0).isEmpty(),"manual input not consumed");
                    result = Arrays.stream(table.getOutputSlots()).mapToObj(table.getSlots()::getItem).filter(st -> st.is(item("backpack"))).findFirst().orElseThrow();
                }
                check(result.get(ModCoreDataComponents.STORAGE_UUID).equals(packedUuid),"table replaced UUID");
                check(BackpackWrapper.fromStack(result).getInventoryHandler().getStackInSlot(0).getCount()==17,"table lost backpack contents");
            }
            passed = true;
            System.out.println("REAL SOPHISTICATED INTEGRATION PASSED " + checks + " checks");
        } catch (Throwable failure){
            failure.printStackTrace();
            System.out.println("REAL SOPHISTICATED INTEGRATION FAILED");
        }
        event.getServer().halt(false);
    }
}