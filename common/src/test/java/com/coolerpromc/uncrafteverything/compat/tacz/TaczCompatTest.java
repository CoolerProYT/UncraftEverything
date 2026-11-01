package com.coolerpromc.uncrafteverything.compat.tacz;

import com.coolerpromc.uncrafteverything.util.UncraftingOutputPlan;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.coolerpromc.uncrafteverything.platform.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaczCompatTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static ItemStack gun(String id) {
        ItemStack stack = new ItemStack(Items.IRON_HOE);
        stack.getOrCreateTag().putString("GunId", id);
        return stack;
    }

    @Test
    void bridgeLoadsWithoutTaczInstalled() throws Exception {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.tacz.guns.api.TimelessAPI"));
        assertNotNull(Class.forName(TaczCompat.class.getName()));
    }

    @Test
    void droppedGunMatchesDespiteMutableData() {
        ItemStack input = gun("tacz:ak47");
        input.getOrCreateTag().putInt("GunCurrentAmmoCount", 17);
        input.getOrCreateTag().putBoolean("HasBulletInBarrel", true);
        input.getOrCreateTag().putString("GunFireMode", "AUTO");
        input.getOrCreateTag().putInt("GunLevelExp", 900);
        assertTrue(TaczCompat.matches(gun("tacz:ak47"), input));
    }

    @Test
    void modelsSharingAnItemNeverMatchEachOther() {
        assertFalse(TaczCompat.matches(gun("tacz:ak47"), gun("tacz:m4a1")));
        assertFalse(TaczCompat.matches(gun("tacz:ak47"), new ItemStack(Items.IRON_HOE)));
        assertFalse(TaczCompat.matches(gun("bad id"), gun("bad id")));
    }

    @Test
    void ammunitionRequiresCorrectCaliberAndCompleteCraftingBatch() {
        ItemStack recipe = new ItemStack(Items.IRON_NUGGET, 35);
        recipe.getOrCreateTag().putString("AmmoId", "tacz:762x39");
        ItemStack input = recipe.copy();
        input.setHoverName(net.minecraft.network.chat.Component.literal("Recovered rounds"));
        assertTrue(TaczCompat.matches(recipe, input));
        input.setCount(34);
        assertFalse(TaczCompat.matches(recipe, input));
        input.setCount(70);
        input.getOrCreateTag().putString("AmmoId", "tacz:556x45");
        assertFalse(TaczCompat.matches(recipe, input));
        input.removeTagKey("AmmoId");
        assertFalse(TaczCompat.matches(recipe, input));
    }

    @Test
    void attachmentsRequireCorrectModelAndIgnoreMutableState() {
        ItemStack recipe = new ItemStack(Items.SPYGLASS);
        recipe.getOrCreateTag().putString("AttachmentId", "tacz:scope_acog_ta31");
        ItemStack input = recipe.copy();
        input.getOrCreateTag().putInt("AttachmentDurability", 17);
        assertTrue(TaczCompat.matches(recipe, input));
        input.getOrCreateTag().putString("AttachmentId", "tacz:scope_sro_dot");
        assertFalse(TaczCompat.matches(recipe, input));
        input.getOrCreateTag().putString("AttachmentId", "invalid id");
        assertFalse(TaczCompat.matches(recipe, input));
    }

    @Test
    void otherSmithingOutputsHonorRecipeNbt() {
        ItemStack recipe = new ItemStack(Items.IRON_INGOT);
        recipe.getOrCreateTag().putString("Variant", "special");
        ItemStack input = recipe.copy();
        input.setHoverName(net.minecraft.network.chat.Component.literal("Recovered part"));
        assertTrue(TaczCompat.matches(recipe, input));
        input.getOrCreateTag().putString("Variant", "other");
        assertFalse(TaczCompat.matches(recipe, input));
    }

    @Test
    void honorsRecipeOutputBatchSize() {
        ItemStack result = gun("tacz:ak47").copyWithCount(2);
        assertFalse(TaczCompat.matches(result, gun("tacz:ak47")));
        assertTrue(TaczCompat.matches(result, gun("tacz:ak47").copyWithCount(2)));
    }

    @Test
    void countedMaterialsMergeAndSplitAtStackLimit() {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        List<UncraftingTableRecipe> recipes = TaczCompat.expand(gun("tacz:ak47"), List.of(
                new TaczCompat.Material(List.of(iron), 70),
                new TaczCompat.Material(List.of(iron), 10)), List.of());
        assertEquals(1, recipes.size());
        assertEquals(List.of(64, 16), recipes.get(0).getOutputs().stream().map(ItemStack::getCount).toList());
        assertEquals(1, iron.getCount(), "Never mutate ingredient examples");
    }

    @Test
    void alternativesKeepTheirFullMaterialCountsAndNbt() {
        ItemStack tagged = new ItemStack(Items.GOLD_INGOT);
        tagged.getOrCreateTag().putString("Variant", "special");
        List<UncraftingTableRecipe> recipes = TaczCompat.expand(gun("tacz:ak47"), List.of(
                new TaczCompat.Material(List.of(new ItemStack(Items.IRON_INGOT), tagged), 12)), List.of());
        assertEquals(2, recipes.size());
        assertEquals(12, recipes.get(0).getOutputs().get(0).getCount());
        assertEquals(12, recipes.get(1).getOutputs().get(0).getCount());
        assertEquals(tagged.getTag(), recipes.get(1).getOutputs().get(0).getTag());
    }

    @Test
    void preservesInputDataAndReturnedContents() {
        ItemStack input = gun("tacz:ak47");
        input.getOrCreateTag().putInt("GunCurrentAmmoCount", 10);
        ItemStack attachment = new ItemStack(Items.SPYGLASS);
        attachment.getOrCreateTag().putString("AttachmentId", "tacz:scope");
        var recipes = TaczCompat.expand(input, List.of(
                new TaczCompat.Material(List.of(new ItemStack(Items.IRON_INGOT)), 5)), List.of(attachment));
        assertTrue(ItemStack.isSameItemSameTags(input, recipes.get(0).getInput()));
        assertTrue(ItemStack.isSameItemSameTags(attachment, recipes.get(0).getOutputs().get(1)));
    }

    @Test
    void oversizedRecipeIsRejectedRatherThanTruncated() {
        assertTrue(TaczCompat.expand(gun("tacz:ak47"), List.of(
                new TaczCompat.Material(List.of(new ItemStack(Items.IRON_INGOT)), 64 * 9 + 1)), List.of()).isEmpty());
    }

    @Test
    void manualPlanChecksActualDestinationAndDoesNotMutateInventory() {
        ItemStackHandler handler = new ItemStackHandler(9);
        handler.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT));
        assertNull(UncraftingOutputPlan.create(handler, List.of(new ItemStack(Items.IRON_INGOT, 64)), true));
        assertTrue(handler.getStackInSlot(1).isEmpty());
        assertEquals(1, handler.getStackInSlot(0).getCount());
    }

    @Test
    void automaticPlanReservesSpaceForEntireBatch() {
        ItemStackHandler handler = new ItemStackHandler(2);
        handler.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 60));
        handler.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 64));
        assertNull(UncraftingOutputPlan.create(handler, List.of(
                new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.IRON_INGOT, 3)), false));
        assertEquals(60, handler.getStackInSlot(0).getCount());
    }

    @Test
    void automaticPlanPacksAndAppliesAllCountedMaterials() {
        ItemStackHandler handler = new ItemStackHandler(3);
        handler.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 60));
        var plan = UncraftingOutputPlan.create(handler, List.of(new ItemStack(Items.IRON_INGOT, 70)), false);
        assertNotNull(plan);
        UncraftingOutputPlan.apply(handler, plan);
        assertEquals(64, handler.getStackInSlot(0).getCount());
        assertEquals(64, handler.getStackInSlot(1).getCount());
        assertEquals(2, handler.getStackInSlot(2).getCount());
    }

    @Test
    void fullOutputAndNonStackableItemsCannotOverflow() {
        ItemStackHandler handler = new ItemStackHandler(1);
        assertNull(UncraftingOutputPlan.create(handler, List.of(new ItemStack(Items.SPYGLASS, 2)), true));
        handler.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
        assertNull(UncraftingOutputPlan.create(handler, List.of(new ItemStack(Items.IRON_INGOT)), false));
    }

    @Test
    void loadedMagazineAndChamberAreNotRefunded() {
        ItemStack input = gun("tacz:ak47");
        input.getOrCreateTag().putInt("GunCurrentAmmoCount", 17);
        input.getOrCreateTag().putBoolean("HasBulletInBarrel", true);
        assertTrue(TaczCompat.contents(input).isEmpty());
    }

    @Test
    void npcDummyAmmoNeverBecomesRealAmmoButAttachmentsAreReturned() throws Exception {
        ItemStack input = gun("tacz:ak47");
        input.getOrCreateTag().putInt("DummyAmmo", 9999);
        input.getOrCreateTag().putInt("GunCurrentAmmoCount", 30);
        input.getOrCreateTag().putBoolean("HasBulletInBarrel", true);
        input.getOrCreateTag().putBoolean("AttachmentLock", true);
        ItemStack scope = new ItemStack(Items.SPYGLASS);
        scope.getOrCreateTag().putString("AttachmentId", "tacz:scope");
        input.getOrCreateTag().put("AttachmentSCOPE", scope.save(new net.minecraft.nbt.CompoundTag()));
        var contents = TaczCompat.contents(input);
        assertEquals(1, contents.size());
        assertTrue(ItemStack.isSameItemSameTags(scope, contents.get(0)));
    }

    @Test
    void loadedAmmoDoesNotRequireAmmoResolution() {
        ItemStack input = gun("tacz:ak47");
        input.getOrCreateTag().putInt("GunCurrentAmmoCount", 10);
        assertTrue(TaczCompat.contents(input).isEmpty());
    }

    @Test
    void contentsThatExceedNineSlotsRejectTheWholeRecipe() {
        var recipes = TaczCompat.expand(gun("tacz:ak47"), List.of(
                new TaczCompat.Material(List.of(new ItemStack(Items.IRON_INGOT)), 64 * 9)),
                List.of(new ItemStack(Items.SPYGLASS)));
        assertTrue(recipes.isEmpty());
    }

    @Test
    void batchRecipeReturnsContentsForEveryConsumedGun() {
        var recipes = TaczCompat.expand(gun("tacz:ak47").copyWithCount(2), List.of(
                new TaczCompat.Material(List.of(new ItemStack(Items.IRON_INGOT)), 10)),
                List.of(new ItemStack(Items.IRON_NUGGET, 18)));
        assertEquals(2, recipes.get(0).getInput().getCount());
        assertEquals(10, recipes.get(0).getOutputs().get(0).getCount());
        assertEquals(36, recipes.get(0).getOutputs().get(1).getCount());
    }
}
