package com.coolerpromc.uncrafteverything.compat.tacz;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@GameTestHolder("uncrafteverything")
@PrefixGameTestTemplate(false)
public class TaczGameTests {
    private static ItemStack gun(boolean dummy) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:modern_kinetic_gun")));
        stack.getOrCreateTag().putString("GunId", "tacz:ak47");
        stack.getOrCreateTag().putString("GunFireMode", "AUTO");
        stack.getOrCreateTag().putInt("GunCurrentAmmoCount", 17);
        stack.getOrCreateTag().putBoolean("HasBulletInBarrel", true);
        stack.getOrCreateTag().putInt("GunLevelExp", 900);
        if (dummy) stack.getOrCreateTag().putInt("DummyAmmo", 9999);
        return stack;
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void dropped_gun_returns_only_recipe_materials(GameTestHelper helper) throws Exception {
        ItemStack input = gun(false);
        var recipes = TaczCompat.findRecipes(helper.getLevel(), input);
        helper.assertTrue(!recipes.isEmpty(), "Find actual TaCZ AK47 recipe");
        var outputs = TaczCompat.getOutputs(recipes.get(0), input);
        helper.assertTrue(!outputs.isEmpty(), "Produce actual gun outputs");
        int expected = 0;
        for (Object ingredient : (List<?>) recipes.get(0).getClass().getMethod("getInputs").invoke(recipes.get(0))) {
            expected += (int) ingredient.getClass().getMethod("getCount").invoke(ingredient);
        }
        int ammo = outputs.get(0).getOutputs().stream().filter(s -> s.getTag() != null && s.getTag().contains("AmmoId")).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(ammo == 0, "Loaded magazine and chamber must not be refunded, got " + ammo);
        int all = outputs.get(0).getOutputs().stream().mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(all == expected, "Correct actual counted recipe materials: expected " + expected + ", actual " + all);
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void npc_dummy_ammo_is_not_refunded(GameTestHelper helper) {
        ItemStack input = gun(true);
        var recipes = TaczCompat.findRecipes(helper.getLevel(), input);
        helper.assertTrue(!recipes.isEmpty(), "Find recipe for NPC drop");
        var outputs = TaczCompat.getOutputs(recipes.get(0), input);
        helper.assertTrue(!outputs.isEmpty(), "NPC gun has outputs");
        helper.assertTrue(outputs.get(0).getOutputs().stream().noneMatch(s -> s.getTag() != null && s.getTag().contains("AmmoId")), "No infinite NPC ammo refund");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void automatic_table_discovers_and_uncrafts_gun(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        table.getInputHandler().setStackInSlot(0, gun(true));
        helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Actual automatic table discovers gun recipes");
        helper.assertTrue(table.hasRecipe(), "Actual table can fit complete gun material outputs");
        table.removeExperiencePoints(10000);
        // Drive the real uncraft operation after capacity and experience checks.
        try {
            var process = AutoUncraftingTableBlockEntity.class.getDeclaredMethod("processUncraft");
            process.setAccessible(true);
            process.invoke(table);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "One gun consumed");
        int count = 0;
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) count += table.getOutputHandler().getStackInSlot(slot).getCount();
        helper.assertTrue(count > 1, "Counted materials inserted into actual output inventory");
        helper.succeed();
    }
    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void manual_table_uncrafts_dropped_gun(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
        var table = (com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        player.giveExperiencePoints(10000);
        table.setPlayer(player);
        table.getInputHandler().setStackInSlot(0, gun(false));
        helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Manual table discovers dropped gun");
        var data = new com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload(
                table.getBlockPos(), table.getCurrentRecipes(), table.getCurrentRecipes().size());
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload.encode(data, buffer);
            var received = com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload.decode(buffer);
            buffer.clear();
            var selection = new com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload(
                    table.getBlockPos(), received.recipes().get(0));
            com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload.encode(selection, buffer);
            var returned = com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload.decode(buffer);
            helper.assertTrue(ItemStack.isSameItemSameTags(table.getInputHandler().getStackInSlot(0), returned.recipe().getInput()),
                    "Gun metadata survives recipe packet round trip: input=" + table.getInputHandler().getStackInSlot(0).getTag()
                            + ", received=" + returned.recipe().getInput().getTag());
            table.handleRecipeSelection(returned.recipe());
        } finally { buffer.release(); }
        helper.assertTrue(table.hasRecipe(), "Manual table has space for complete result");
        table.handleUncraftButtonClicked(false);
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "Manual table consumes gun");
        int count = 0;
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) count += table.getOutputHandler().getStackInSlot(slot).getCount();
        helper.assertTrue(count > 1, "Manual table inserts counted recipe materials");
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) {
            ItemStack output = table.getOutputHandler().getStackInSlot(slot);
            helper.assertTrue(output.getTag() == null || !output.getTag().contains("AmmoId"), "Manual table does not refund loaded ammo");
        }
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void ammunition_and_attachment_recipe_material_counts(GameTestHelper helper) throws Exception {
        var candidates = new java.util.LinkedHashMap<ResourceLocation, Recipe<?>>();
        for (Recipe<?> recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (TaczCompat.isRecipe(recipe)) candidates.put(recipe.getId(), recipe);
        }
        try {
            var api = Class.forName("com.tacz.guns.api.TimelessAPI");
            for (Object value : ((java.util.Map<?, ?>) api.getMethod("getAllRecipes").invoke(null)).values()) {
                if (value instanceof Recipe<?> recipe && TaczCompat.isRecipe(recipe)) candidates.putIfAbsent(recipe.getId(), recipe);
            }
        } catch (NoSuchMethodException ignored) { }
        int ammunition = 0;
        int attachments = 0;
        for (Recipe<?> recipe : candidates.values()) {
            ItemStack result = (ItemStack) recipe.getClass().getMethod("getOutput").invoke(recipe);
            if (result.getTag() == null) continue;
            boolean ammo = result.getTag().contains("AmmoId");
            boolean attachment = result.getTag().contains("AttachmentId");
            if (!ammo && !attachment) continue;
            helper.assertTrue(TaczCompat.findRecipes(helper.getLevel(), result).stream().anyMatch(r -> r.getId().equals(recipe.getId())),
                    "Discover real recipe " + recipe.getId());
            var outputs = TaczCompat.getOutputs(recipe, result);
            helper.assertTrue(!outputs.isEmpty(), "Produce materials for " + recipe.getId());
            int expected = 0;
            for (Object ingredient : (List<?>) recipe.getClass().getMethod("getInputs").invoke(recipe)) {
                expected += (int) ingredient.getClass().getMethod("getCount").invoke(ingredient);
            }
            for (var output : outputs) {
                int factor = result.getCount() / output.getInput().getCount();
                helper.assertTrue(output.getInput().getCount() * factor == result.getCount(), "Consume exact integral batch for " + recipe.getId());
                helper.assertTrue(output.getOutputs().stream().mapToInt(ItemStack::getCount).sum() * factor == expected,
                        "Exact material quantities for " + recipe.getId());
            }
            int batch = outputs.get(0).getInput().getCount();
            if (batch > 1) {
                helper.assertTrue(TaczCompat.getOutputs(recipe, result.copyWithCount(batch - 1)).isEmpty(),
                        "Reject incomplete batch for " + recipe.getId());
            }
            if (ammo) ammunition++;
            if (attachment) attachments++;
        }
        helper.assertTrue(ammunition > 0 && attachments > 0, "Exercise real ammunition and attachment recipes");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void manual_table_consumes_one_ammunition_batch(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
        var table = (com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        player.giveExperiencePoints(10000);
        table.setPlayer(player);
        ItemStack ammo = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:ammo")), 70);
        ammo.getOrCreateTag().putString("AmmoId", "tacz:762x39");
        table.getInputHandler().setStackInSlot(0, ammo);
        helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Manual table discovers ammunition recipe");
        var recipe = table.getCurrentRecipes().get(0);
        int batch = recipe.getInput().getCount();
        int expected = recipe.getOutputs().stream().mapToInt(ItemStack::getCount).sum();
        table.handleRecipeSelection(recipe);
        helper.assertTrue(table.hasRecipe(), "Complete ammunition batch fits");
        table.handleUncraftButtonClicked(false);
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).getCount() == 70 - batch, "Consume one complete batch only");
        int count = 0;
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) count += table.getOutputHandler().getStackInSlot(slot).getCount();
        helper.assertTrue(count == expected, "Return exact ammunition materials");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void incomplete_ammunition_batch_reports_insufficient_input(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
        helper.setBlock(new BlockPos(1, 0, 0), UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var manual = (com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        var automatic = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(new BlockPos(1, 0, 0));
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        manual.setPlayer(player);
        ItemStack ammo = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:ammo")), 64);
        ammo.getOrCreateTag().putString("AmmoId", "tacz:762x39");
        manual.getInputHandler().setStackInSlot(0, ammo.copy());
        helper.assertTrue(!manual.getCurrentRecipes().isEmpty(), "Full ammunition batch has a recipe");
        var previous = manual.getCurrentRecipes().get(0);
        int batch = previous.getInput().getCount();
        helper.assertTrue(batch > 1, "Ammunition requires multiple input items");
        for (var table : List.of(manual, automatic)) {
            var input = table == manual ? manual.getInputHandler() : automatic.getInputHandler();
            input.setStackInSlot(0, ammo.copyWithCount(batch - 1));
            helper.assertTrue(table.status == com.coolerpromc.uncrafteverything.util.Status.NOT_ENOUGH_INPUT_ITEM,
                    "Incomplete batch reports insufficient input, got " + table.status);
            helper.assertTrue((table == manual ? manual.getCurrentRecipes() : automatic.getCurrentRecipes()).isEmpty(), "Incomplete batch cannot be uncrafted");
            table.handleRecipeSelection(previous);
            helper.assertTrue(table.status == com.coolerpromc.uncrafteverything.util.Status.NOT_ENOUGH_INPUT_ITEM,
                    "Stale selection must not overwrite shortage status");
            input.setStackInSlot(0, ammo.copyWithCount(batch));
            helper.assertTrue(!(table == manual ? manual.getCurrentRecipes() : automatic.getCurrentRecipes()).isEmpty(), "Adding missing rounds restores recipe");
            helper.assertTrue(table.status != com.coolerpromc.uncrafteverything.util.Status.NOT_ENOUGH_INPUT_ITEM,
                    "Adding missing rounds clears shortage status");
            ItemStack unknown = ammo.copy();
            unknown.getOrCreateTag().putString("AmmoId", "uncrafteverything:nonexistent_ammunition");
            input.setStackInSlot(0, unknown);
            helper.assertTrue(table.status == com.coolerpromc.uncrafteverything.util.Status.NO_RECIPE_FOUND,
                    "Unknown model still reports no recipe");
        }
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void menu_merge_completes_ammunition_batch(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.UNCRAFTING_TABLE.get());
        helper.setBlock(new BlockPos(1, 0, 0), UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var manual = (com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        var automatic = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(new BlockPos(1, 0, 0));
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        for (var table : List.of(manual, automatic)) {
            player.getInventory().clearContent();
            var menu = ((net.minecraft.world.MenuProvider) table).createMenu(7, player.getInventory(), player);
            var input = table == manual ? manual.getInputHandler() : automatic.getInputHandler();
            ItemStack ammo = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:ammo")), 34);
            ammo.getOrCreateTag().putString("AmmoId", "tacz:762x39");
            input.setStackInSlot(0, ammo);
            helper.assertTrue(table.status == com.coolerpromc.uncrafteverything.util.Status.NOT_ENOUGH_INPUT_ITEM, "Initial partial batch reports shortage");
            player.getInventory().setItem(0, ammo.copyWithCount(1));
            helper.getLevel().getChunkAt(table.getBlockPos()).setUnsaved(false);
            menu.quickMoveStack(player, 27);
            helper.assertTrue(helper.getLevel().getChunkAt(table.getBlockPos()).isUnsaved(), "Menu merge marks the table chunk dirty for persistence");
            helper.assertTrue(input.getStackInSlot(0).getCount() == 35, "Shift-click merges the final round");
            var recipes = table == manual ? manual.getCurrentRecipes() : automatic.getCurrentRecipes();
            helper.assertTrue(!recipes.isEmpty(), "In-place menu merge refreshes recipe discovery");
            helper.assertTrue(table.status != com.coolerpromc.uncrafteverything.util.Status.NOT_ENOUGH_INPUT_ITEM, "Merge clears shortage status");
            table.handleRecipeSelection(recipes.get(0));
            helper.assertTrue(table.hasRecipe(), "Merged batch can be uncrafted");
        }
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void large_ammunition_recipe_uses_exact_smaller_batch(GameTestHelper helper) throws Exception {
        // The .22 WMR recipe was added after TaCZ 1.1.4.
        var index = (java.util.Optional<?>) Class.forName("com.tacz.guns.api.TimelessAPI")
                .getMethod("getCommonAmmoIndex", ResourceLocation.class).invoke(null, new ResourceLocation("tacz:22wmr"));
        if (index.isEmpty()) {
            helper.succeed();
            return;
        }
        helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        ItemStack ammo = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:ammo")), 50);
        ammo.getOrCreateTag().putString("AmmoId", "tacz:22wmr");
        ItemStack remainder = table.getInputHandler().insertItem(0, ammo, false);
        helper.assertTrue(remainder.isEmpty(), "Accept exact 50-round batch; native max=" + ammo.getMaxStackSize()
                + ", remainder=" + remainder.getCount());
        helper.assertTrue(table.hasRecipe(), "Large ammunition recipe can be uncrafted as an exact smaller batch");
        helper.assertTrue(table.getCurrentRecipes().get(0).getInput().getCount() == 50, "Consume exactly 50 rounds");
        table.removeExperiencePoints(10000);
        var process = AutoUncraftingTableBlockEntity.class.getDeclaredMethod("processUncraft");
        process.setAccessible(true);
        process.invoke(table);
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "All 50 rounds consumed");
        int copper = 0;
        int powder = 0;
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) {
            ItemStack output = table.getOutputHandler().getStackInSlot(slot);
            if (output.is(net.minecraft.world.item.Items.COPPER_INGOT)) copper += output.getCount();
            else if (output.is(net.minecraft.world.item.Items.GUNPOWDER)) powder += output.getCount();
            else helper.assertTrue(output.isEmpty(), "No additional output");
        }
        helper.assertTrue(copper == 5 && powder == 1, "Exact half of 100-round recipe: 5 copper and 1 gunpowder");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void automatic_table_uncrafts_scope(GameTestHelper helper) throws Exception {
        helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        ItemStack scope = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("tacz:attachment")));
        scope.getOrCreateTag().putString("AttachmentId", "tacz:scope_acog_ta31");
        table.getInputHandler().setStackInSlot(0, scope);
        helper.assertTrue(!table.getCurrentRecipes().isEmpty(), "Automatic table discovers scope recipe");
        int expected = table.getCurrentRecipes().get(0).getOutputs().stream().mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(table.hasRecipe(), "Scope materials fit");
        table.removeExperiencePoints(10000);
        var process = AutoUncraftingTableBlockEntity.class.getDeclaredMethod("processUncraft");
        process.setAccessible(true);
        process.invoke(table);
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "Scope consumed");
        int count = 0;
        for (int slot = 0; slot < table.getOutputHandler().getSlots(); slot++) count += table.getOutputHandler().getStackInSlot(slot).getCount();
        helper.assertTrue(count == expected, "Return exact scope materials");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void forge_capabilities_delegate_shared_inventory(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, UEBlocks.AUTO_UNCRAFTING_TABLE.get());
        var table = (AutoUncraftingTableBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        var inputCapability = table.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP);
        var outputCapability = table.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.DOWN);
        var input = inputCapability.orElseThrow(() -> new AssertionError("Input capability missing"));
        var output = outputCapability.orElseThrow(() -> new AssertionError("Output capability missing"));
        ItemStack planks = new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS, 4);
        helper.assertTrue(input.insertItem(0, planks, true).isEmpty(), "Simulated insertion accepts stack");
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).isEmpty(), "Simulation does not change shared inventory");
        helper.assertTrue(input.insertItem(0, planks, false).isEmpty(), "Actual insertion accepts stack");
        helper.assertTrue(table.getInputHandler().getStackInSlot(0).getCount() == 4, "Capability writes to common inventory");
        helper.assertTrue(!output.insertItem(0, planks, false).isEmpty(), "Output capability rejects insertion");
        table.getOutputHandler().setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.OAK_LOG, 2));
        helper.assertTrue(output.extractItem(0, 1, false).is(net.minecraft.world.item.Items.OAK_LOG), "Output capability extracts result");
        helper.assertTrue(table.getOutputHandler().getStackInSlot(0).getCount() == 1, "Extraction updates common inventory");
        var saved = table.saveWithFullMetadata();
        var restored = (AutoUncraftingTableBlockEntity) net.minecraft.world.level.block.entity.BlockEntity.loadStatic(BlockPos.ZERO, table.getBlockState(), saved);
        helper.assertTrue(restored != null && restored.getInputHandler().getStackInSlot(0).getCount() == 4, "Registered native factory preserves common input on reload");
        helper.assertTrue(restored.getOutputHandler().getStackInSlot(0).getCount() == 1, "Common output survives reload");
        table.invalidateCaps();
        helper.assertTrue(!inputCapability.isPresent() && !outputCapability.isPresent(), "Native capabilities invalidate when the table unloads");
        helper.succeed();
    }

    @GameTest(template = "empty3x3", timeoutTicks = 100)
    public static void recipe_manager_and_legacy_registry_are_deduplicated(GameTestHelper helper) {
        ItemStack input = gun(true);
        var manager = helper.getLevel().getRecipeManager();
        var saved = List.copyOf(manager.getRecipes());
        var recipe = TaczCompat.findRecipes(helper.getLevel(), input).get(0);
        var injected = new java.util.ArrayList<Recipe<?>>();
        saved.stream().filter(r -> !r.getId().equals(recipe.getId())).forEach(injected::add);
        injected.add(recipe);
        try {
            manager.replaceRecipes(injected);
            var matches = TaczCompat.findRecipes(helper.getLevel(), input);
            helper.assertTrue(matches.stream().filter(r -> r.getId().equals(recipe.getId())).count() == 1,
                    "Same recipe in manager and legacy API offered once");
        } finally { manager.replaceRecipes(saved); }
        helper.succeed();
    }

}
