package com.coolerpromc.uncrafteverything.test;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.*;
import com.coolerpromc.uncrafteverything.platform.*;
import com.coolerpromc.uncrafteverything.util.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

public class FabricParityGameTests implements FabricGameTest {
  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void automaticTableUncraftsAndPersists(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    table.getInputHandler().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
    helper.assertTrue(
        !table.getCurrentRecipes().isEmpty(), "Automatic table discovers plank recipe");
    helper.assertTrue(table.hasRecipe(), "Complete output fits");
    table.removeExperiencePoints(100);
    int previousXp = table.getTotalExperience();
    table.setActive(true);
    table.tick(helper.getLevel(), table.getBlockPos(), table.getBlockState());
    helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "Four planks consumed");
    helper.assertTrue(
        table.getOutputHandler().getStackInSlot(0).is(Items.OAK_LOG), "Recipe returns one oak log");
    helper.assertTrue(table.getTotalExperience() < previousXp, "Uncrafting spends stored XP");
    CompoundTag saved = table.saveWithFullMetadata();
    var restored = new AutoUncraftingTableBlockEntity(table.getBlockPos(), table.getBlockState());
    restored.load(saved);
    helper.assertTrue(
        restored.getOutputHandler().getStackInSlot(0).is(Items.OAK_LOG),
        "Output survives save/reload");
    helper.assertTrue(
        restored.getTotalExperience() == table.getTotalExperience(),
        "Stored XP survives save/reload");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void migratesFabric192Inventory(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
    var table = (UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    NonNullList<ItemStack> old = NonNullList.withSize(10, ItemStack.EMPTY);
    ItemStack input = new ItemStack(Items.IRON_SWORD);
    input.getOrCreateTag().putString("custom", "keep-me");
    input.setDamageValue(17);
    old.set(0, input);
    old.set(1, new ItemStack(Items.IRON_INGOT, 37));
    old.set(9, new ItemStack(Items.GOLD_INGOT, 9));
    CompoundTag legacy = new CompoundTag();
    ContainerHelper.saveAllItems(legacy, old);
    legacy.putInt("experience", 13);
    legacy.putInt("experienceType", 0);
    legacy.putString("id", "minecraft:uncrafting_table_be");
    var loaded = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(table.getBlockPos(), table.getBlockState(), legacy);
    helper.assertTrue(loaded instanceof UncraftingTableBlockEntity, "Fabric 1.9.2's legacy block entity ID resolves");
    table = (UncraftingTableBlockEntity) loaded;
    helper.assertTrue(
        ItemStack.isSameItemSameTags(table.getInputHandler().getStackInSlot(0), input),
        "Input NBT and damage preserved");
    helper.assertTrue(
        table.getOutputHandler().getStackInSlot(0).getCount() == 37,
        "First legacy output preserved");
    helper.assertTrue(
        table.getOutputHandler().getStackInSlot(8).is(Items.GOLD_INGOT),
        "Last legacy output preserved");
    var restored = new UncraftingTableBlockEntity(table.getBlockPos(), table.getBlockState());
    restored.load(table.saveWithFullMetadata());
    helper.assertTrue(
        restored.getOutputHandler().getStackInSlot(8).getCount() == 9,
        "Migrated inventory round-trips");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void sidedAutomationAndOutputProtection(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    var input = ItemStorage.SIDED.find(helper.getLevel(), table.getBlockPos(), Direction.UP);
    var output = ItemStorage.SIDED.find(helper.getLevel(), table.getBlockPos(), Direction.DOWN);
    helper.assertTrue(input != null && output != null, "Fabric exposes both sides");
    try (var transaction = Transaction.openOuter()) {
      helper.assertTrue(
          input.insert(ItemVariant.of(Items.OAK_PLANKS), 4, transaction) == 4,
          "Automation inserts input");
      transaction.commit();
    }
    helper.assertTrue(
        table.getInputHandler().getStackInSlot(0).getCount() == 4, "Input insertion commits");
    try (var transaction = Transaction.openOuter()) {
      helper.assertTrue(
          output.insert(ItemVariant.of(Items.DIAMOND), 1, transaction) == 0,
          "Output rejects external insertion");
    }
    table.getOutputHandler().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
    try (var transaction = Transaction.openOuter()) {
      helper.assertTrue(
          output.extract(ItemVariant.of(Items.IRON_INGOT), 5, transaction) == 5,
          "Automation extracts below");
      transaction.commit();
    }
    helper.assertTrue(
        table.getOutputHandler().getStackInSlot(0).getCount() == 7, "Extraction updates output");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void redstoneControlsAutomaticTable(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    helper.setBlock(new BlockPos(2, 1, 1), Blocks.REDSTONE_BLOCK);
    helper.runAfterDelay(
        3,
        () -> {
          var state = helper.getBlockState(new BlockPos(1, 1, 1));
          helper.assertTrue(
              state.getValue(
                  com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock.POWERED),
              "Redstone powers table");
          helper.succeed();
        });
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void countedOutputsCannotBePartiallyInserted(GameTestHelper helper) {
    ItemStackHandler handler = new ItemStackHandler(2);
    handler.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 60));
    handler.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 64));
    var plan =
        UncraftingOutputPlan.create(
            handler,
            java.util.List.of(
                new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.IRON_INGOT, 3)),
            false);
    helper.assertTrue(plan == null, "Whole batch rejected when only four spaces exist");
    helper.assertTrue(handler.getStackInSlot(0).getCount() == 60, "Inventory unchanged");
    helper.succeed();
  }

  private net.minecraft.server.level.ServerPlayer player(GameTestHelper helper) {
    var player =
        new net.minecraft.server.level.ServerPlayer(
            helper.getLevel().getServer(),
            helper.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "UncraftTest"));
    player.totalExperience = 1000;
    player.experienceLevel = 30;
    return player;
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void manualTableAndMenuMatchForge(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
    var table = (UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    var player = player(helper);
    var menu = table.createMenu(7, player.getInventory(), player);
    helper.assertTrue(
        menu.slots.size() == 46, "Manual menu has 36 player slots plus ten table slots");
    helper.assertTrue(
        !menu.getSlot(37).mayPlace(new ItemStack(Items.DIAMOND)),
        "Players cannot place into output");
    table.getInputHandler().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
    helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Manual table discovers recipe");
    table.handleRecipeSelection(table.getCurrentRecipes().get(0));
    helper.assertTrue(table.hasRecipe(), "Manual table can uncraft");
    table.handleUncraftButtonClicked(false);
    helper.assertTrue(
        table.getInputHandler().getStackInSlot(0).isEmpty(), "Manual operation consumes input");
    helper.assertTrue(
        table.getOutputHandler().getStackInSlot(0).is(Items.OAK_LOG),
        "Manual operation returns material");
    helper.assertTrue(player.totalExperience < 1000, "Manual operation charges player XP");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void menuMergeRefreshesRecipes(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
    helper.setBlock(new BlockPos(1, 0, 0), UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var manual = (UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    var automatic = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(new BlockPos(1, 0, 0));
    var player = player(helper);
    for (var table : java.util.List.of(manual, automatic)) {
      player.getInventory().clearContent();
      var menu = ((net.minecraft.world.MenuProvider) table).createMenu(7, player.getInventory(), player);
      var input = table == manual ? manual.getInputHandler() : automatic.getInputHandler();
      input.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 3));
      helper.assertTrue((table == manual ? manual.getCurrentRecipes() : automatic.getCurrentRecipes()).isEmpty(), "Partial input has no usable recipe");
      player.getInventory().setItem(0, new ItemStack(Items.OAK_PLANKS));
      helper.getLevel().getChunkAt(table.getBlockPos()).setUnsaved(false);
      menu.quickMoveStack(player, 27);
      helper.assertTrue(helper.getLevel().getChunkAt(table.getBlockPos()).isUnsaved(), "Menu merge marks table data for saving");
      helper.assertTrue(input.getStackInSlot(0).getCount() == 4, "Shift-click merges into existing input");
      var recipes = table == manual ? manual.getCurrentRecipes() : automatic.getCurrentRecipes();
      helper.assertTrue(!recipes.isEmpty(), "In-place menu merge refreshes recipes");
      helper.assertTrue(table.status != Status.NOT_ENOUGH_INPUT_ITEM, "Complete batch clears shortage");
      table.handleRecipeSelection(recipes.get(0));
      helper.assertTrue(table.hasRecipe(), "Merged input can be uncrafted");
    }
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void automaticXpDepositWithdrawalAndMenu(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    var player = player(helper);
    var menu = table.createMenu(7, player.getInventory(), player);
    helper.assertTrue(menu.slots.size() == 46, "Automatic menu retains ten table slots");
    helper.assertTrue(
        table.getData().getCount() == 10, "Automatic XP/history control data synchronized");
    table.addExperiencePoints(100, player);
    helper.assertTrue(
        table.getTotalExperience() == 100 && player.totalExperience == 900,
        "Deposit transfers exact points");
    table.addExperiencePoints(-40, player);
    helper.assertTrue(
        table.getTotalExperience() == 60 && player.totalExperience == 940,
        "Withdrawal transfers exact points");
    table.addExperiencePoints(-100, player);
    helper.assertTrue(
        table.getTotalExperience() == 60 && player.totalExperience == 940,
        "Insufficient balance cannot duplicate XP");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void tippedArrowsPreservePotion(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    ItemStack arrows =
        net.minecraft.world.item.alchemy.PotionUtils.setPotion(
            new ItemStack(Items.TIPPED_ARROW, 8), net.minecraft.world.item.alchemy.Potions.POISON);
    table.getInputHandler().setStackInSlot(0, arrows);
    helper.assertTrue(
        !table.getCurrentRecipes().isEmpty(), "Tipped arrows have an uncrafting result");
    var result = table.getCurrentRecipes().get(0);
    helper.assertTrue(
        result.getOutputs().stream()
            .anyMatch(stack -> stack.is(Items.ARROW) && stack.getCount() == 8),
        "Eight arrows recovered");
    helper.assertTrue(
        result.getOutputs().stream()
            .anyMatch(
                stack ->
                    stack.is(Items.LINGERING_POTION)
                        && net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack)
                            == net.minecraft.world.item.alchemy.Potions.POISON),
        "Potion NBT preserved");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void coloredShulkerPreservesContents(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    ItemStack shulker = new ItemStack(Items.RED_SHULKER_BOX);
    CompoundTag contents = new CompoundTag();
    NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    items.set(0, new ItemStack(Items.DIAMOND, 3));
    ContainerHelper.saveAllItems(contents, items);
    shulker.getOrCreateTag().put("BlockEntityTag", contents);
    table.getInputHandler().setStackInSlot(0, shulker);
    helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Colored shulker can be reversed");
    var outputs = table.getCurrentRecipes().get(0).getOutputs();
    helper.assertTrue(
        outputs.stream()
            .anyMatch(
                stack ->
                    stack.is(Items.SHULKER_BOX)
                        && stack.hasTag()
                        && contents.equals(stack.getTagElement("BlockEntityTag"))),
        "Shulker inventory retained");
    helper.assertTrue(outputs.stream().anyMatch(stack -> stack.is(Items.RED_DYE)), "Dye recovered");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void smithingReturnsTemplateBaseAndAddition(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    table.getInputHandler().setStackInSlot(0, new ItemStack(Items.NETHERITE_SWORD));
    helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Netherite upgrade can be reversed");
    helper.assertTrue(
        table.getCurrentRecipes().stream()
            .anyMatch(
                recipe ->
                    recipe.getOutputs().stream().anyMatch(stack -> stack.is(Items.DIAMOND_SWORD))
                        && recipe.getOutputs().stream()
                            .anyMatch(stack -> stack.is(Items.NETHERITE_INGOT))
                        && recipe.getOutputs().stream()
                            .anyMatch(
                                stack -> stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))),
        "Smithing returns all three inputs");
    helper.succeed();
  }

  @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
  public void namesAndDamageFollowForgeRules(GameTestHelper helper) {
    helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
    var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
    ItemStack sword = new ItemStack(Items.IRON_SWORD);
    sword.setHoverName(net.minecraft.network.chat.Component.literal("My Sword"));
    sword.setDamageValue(50);
    table.getInputHandler().setStackInSlot(0, sword);
    helper.assertTrue(
        !table.getCurrentRecipes().isEmpty(), "Renamed damaged sword remains reversible");
    helper.assertTrue(
        table.getCurrentRecipes().stream()
            .anyMatch(
                recipe -> recipe.getOutputs().stream().anyMatch(stack -> stack.is(Items.STICK))),
        "Original crafting inputs recovered");
    helper.succeed();
  }
}
