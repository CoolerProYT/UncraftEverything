package com.coolerpromc.uncrafteverything.compat;

import com.coolerpromc.uncrafteverything.compat.mod.SophisticatedBackpacksCompat;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;

import java.util.*;

/** Exercises production recipe/output logic against vanilla 26.2 and isolated optional API fixtures. */
public final class SophisticatedBackpacksRegression {
    private static int checks;
    private static DataComponentType<UUID> uuid;
    private static DataComponentType<Integer> inventorySlots, upgradeSlots, energy;
    private static DataComponentType<String> linked, loot;
    private static Item leather, iron, netherite, base, basicUpgrade, advancedUpgrade;

    public static void main(String[] args) throws Exception {
        try {
            run();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        SharedConstants.tryDetectVersion();
        // Register fixture mod content at the same point loaders do, before vanilla freezes registries.
        var bootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrapped.setAccessible(true);
        bootstrapped.setBoolean(null, true);
        Items.AIR.getDescriptionId();
        uuid = component("sophisticatedcore:storage_uuid", UUIDUtil.CODEC);
        inventorySlots = component("sophisticatedcore:number_of_inventory_slots", Codec.INT);
        upgradeSlots = component("sophisticatedcore:number_of_upgrade_slots", Codec.INT);
        energy = component("sophisticatedcore:energy_stored", Codec.INT);
        linked = component("sophisticatedcore:linked_storage_endpoint", Codec.STRING);
        loot = component("sophisticatedbackpacks:loot_table", Codec.STRING);
        leather = backpack("backpack", 27, 1);
        iron = backpack("iron_backpack", 54, 4);
        netherite = backpack("netherite_backpack", 108, 5);
        base = register("upgrade_base", Item::new);
        basicUpgrade = register("pickup_upgrade", UpgradeItem::new);
        advancedUpgrade = register("advanced_pickup_upgrade", UpgradeItem::new);
        bootstrapped.setBoolean(null, false);
        Bootstrap.bootStrap();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(net.minecraft.data.registries.VanillaRegistries.createLookup())
                .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);

        var config = UncraftEverythingConfig.CONFIG;
        config.allowUnSmithing.set(true);
        config.restrictAmbiguouslyCraftedItems.set(false);
        config.outputEnchantedBook.set(false);
        config.restrictedModIngredients.set(List.of());

        ShapedRecipe basic = shaped(new ItemStack(leather), Map.of('L', Ingredient.of(Items.LEATHER),
                'S', Ingredient.of(Items.STRING), 'C', Ingredient.of(Items.CHEST)), "SLS", "SCS", "LLL");
        Recipe<?> basicWrapped = new BasicBackpackRecipe(basic);
        Recipe<?> tier = new BackpackUpgradeRecipe(shaped(new ItemStack(iron),
                Map.of('B', Ingredient.of(leather), 'I', Ingredient.of(Items.IRON_INGOT)), "III", "IBI", "III"));
        Recipe<?> smith = new SmithingBackpackUpgradeRecipe(new SmithingTransformRecipe(new Recipe.CommonInfo(true),
                Optional.of(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)), Ingredient.of(iron),
                Optional.of(Ingredient.of(Items.NETHERITE_INGOT)), ItemStackTemplate.fromNonEmptyStack(new ItemStack(netherite))));
        Recipe<?> advanced = new UpgradeNextTierRecipe(shaped(new ItemStack(advancedUpgrade),
                Map.of('B', Ingredient.of(basicUpgrade), 'G', Ingredient.of(Items.GOLD_INGOT)), "GGG", "GBG", "GGG"));
        Recipe<?> ordinary = shaped(new ItemStack(basicUpgrade), Map.of('B', Ingredient.of(base), 'I', Ingredient.of(Items.IRON_INGOT)), "IBI");

        check(SophisticatedBackpacksCompat.unwrapRecipe(basicWrapped) == basic, "wrapped basic recipe is exposed to recipe viewers");
        check(!SophisticatedBackpacksCompat.isSupportedItem(new ItemStack(Items.CHEST)), "vanilla recipes stay on the normal path");
        ItemStack fresh = new ItemStack(leather);
        var basicOutput = one(basicWrapped, fresh);
        check(count(basicOutput, Items.LEATHER) == 4 && count(basicOutput, Items.STRING) == 4 && count(basicOutput, Items.CHEST) == 1,
                "basic recipe returns exact materials without empty pattern slots");
        check(one(ordinary, new ItemStack(basicUpgrade)).getOutputs().size() == 2, "ordinary upgrade recipes are recognized");

        for (boolean fabric : List.of(false, true)) {
            BackpackWrapper.inventory = fabric ? new BackpackWrapper.FabricHandler(54) : new BackpackWrapper.NeoHandler(54);
            BackpackWrapper.upgrades = fabric ? new BackpackWrapper.FabricHandler(4) : new BackpackWrapper.NeoHandler(4);
            var inventory = (BackpackWrapper.FabricHandler) BackpackWrapper.inventory;
            var upgrades = (BackpackWrapper.FabricHandler) BackpackWrapper.upgrades;
            ItemStack input = stored(iron);
            input.set(DataComponents.CUSTOM_NAME, Component.literal("My supplies"));
            input.set(energy, 500);
            inventory.stacks[0] = new ItemStack(Items.DIAMOND, 12);
            upgrades.stacks[0] = new ItemStack(basicUpgrade);
            ItemStack before = input.copy();
            var output = one(tier, input);
            ItemStack lower = output.getOutputs().stream().filter(stack -> stack.is(leather)).findFirst().orElseThrow();
            check(lower.get(uuid).equals(input.get(uuid)), "storage identity is retained on " + fabric);
            check(lower.get(energy) == 500 && lower.getHoverName().getString().equals("My supplies"), "all state and name are retained");
            check(lower.get(inventorySlots) == 27 && lower.get(upgradeSlots) == 1, "lower tier receives its own capacity");
            check(count(output, Items.IRON_INGOT) == 8, "tier upgrade materials are returned");
            check(output.getOutputs().stream().filter(stack -> stack.has(uuid)).count() == 1, "storage UUID is never copied to materials");
            check(ItemStack.matches(input, before), "preview leaves the input unchanged");
            check(inventory.stacks[0].getCount() == 12 && upgrades.stacks[0].is(basicUpgrade), "preview leaves contents and installed upgrades unchanged");
            check(SophisticatedBackpacksCompat.canStillUncraft(input, output, null), "safe recipe passes consumption check");
            check(!SophisticatedBackpacksCompat.canFitOutputs(List.of(stored(leather), new ItemStack(Items.STONE)), output.getOutputs()),
                    "another UUID cannot share the output backpack slot");
            check(SophisticatedBackpacksCompat.canFitOutputs(List.of(ItemStack.EMPTY, ItemStack.EMPTY), output.getOutputs()),
                    "empty output slots accept every output");
            inventory.stacks[27] = new ItemStack(Items.EMERALD);
            check(outputs(tier, input).isEmpty(), "occupied inventory slot beyond the lower capacity blocks downgrade");
            check(!SophisticatedBackpacksCompat.canStillUncraft(input, output, null), "contents changed after preview are checked again");
            inventory.stacks[27] = ItemStack.EMPTY;
            upgrades.stacks[1] = new ItemStack(basicUpgrade);
            check(outputs(tier, input).isEmpty(), "installed upgrade outside lower-tier capacity blocks downgrade");
            upgrades.stacks[1] = ItemStack.EMPTY;
            BackpackWrapper.columns = 1;
            inventory.stacks[24] = new ItemStack(Items.DIAMOND);
            check(outputs(tier, input).isEmpty(), "columns occupied by upgrades reduce available inventory");
            inventory.stacks[24] = ItemStack.EMPTY;
            BackpackWrapper.columns = 0;

            ItemStack initializedLeather = stored(leather);
            check(outputs(basicWrapped, initializedLeather).isEmpty(), "filled base backpack cannot be dismantled");
            inventory.stacks[0] = ItemStack.EMPTY;
            check(outputs(basicWrapped, initializedLeather).isEmpty(), "installed upgrades protect a base backpack too");
            upgrades.stacks[0] = ItemStack.EMPTY;
            check(!outputs(basicWrapped, initializedLeather).isEmpty(), "initialized empty base backpack can be dismantled");
            check(!outputs(tier, input).isEmpty(), "downgrade is possible again after removing overflow");
            input.set(linked, "endpoint");
            check(outputs(tier, input).isEmpty(), "linked storage is protected");
            input.remove(linked);
            input.set(loot, "unopened_loot");
            check(outputs(tier, input).isEmpty(), "unresolved loot is protected");
            input.remove(loot);
            BackpackWrapper.broken = true;
            check(outputs(tier, input).isEmpty(), "unknown APIs fail closed");
            BackpackWrapper.broken = false;
        }

        var smithOutput = one(smith, stored(netherite));
        check(BackpackWrapper.refreshes == 0, "previews never refresh cached handlers");
        SophisticatedBackpacksCompat.onUncrafted(smithOutput.getOutputs());
        check(BackpackWrapper.refreshes == 1, "committing refreshes the predecessor's cached handlers");
        var uncached = SophisticatedBackpacksCompat.class.getDeclaredMethod("getUncachedWrapper", Class.class, ItemStack.class);
        uncached.setAccessible(true);
        ItemStack legacyInput = stored(iron);
        LegacyWrapper legacy = (LegacyWrapper) uncached.invoke(null, LegacyWrapper.class, legacyInput);
        check(legacy.stack != legacyInput && ItemStack.matches(legacy.stack, legacyInput), "Fabric constructor fallback reads a copy of the input");
        check(count(smithOutput, iron) == 1 && count(smithOutput, Items.NETHERITE_INGOT) == 1
                && count(smithOutput, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) == 1, "smithing returns base, material and template");
        config.allowUnSmithing.set(false);
        check(outputs(smith, stored(netherite)).isEmpty(), "smithing restriction is honored");
        config.allowUnSmithing.set(true);
        config.restrictAmbiguouslyCraftedItems.set(true);
        Recipe<?> ambiguous = new BasicBackpackRecipe(shaped(new ItemStack(leather),
                Map.of('C', Ingredient.of(Items.CHEST, Items.BARREL)), "C"));
        check(!SophisticatedBackpacksCompat.hasRecipe(ambiguous, fresh, null), "ambiguous ingredient restriction is honored");
        config.restrictAmbiguouslyCraftedItems.set(false);

        ItemStack upgrade = new ItemStack(advancedUpgrade);
        upgrade.set(energy, 1000);
        upgrade.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND))));
        var upgradeOutput = one(advanced, upgrade);
        ItemStack lowerUpgrade = upgradeOutput.getOutputs().stream().filter(stack -> stack.is(basicUpgrade)).findFirst().orElseThrow();
        check(lowerUpgrade.get(energy) == 1000 && lowerUpgrade.get(DataComponents.CONTAINER).equals(upgrade.get(DataComponents.CONTAINER)),
                "standalone upgrade carries energy, inventory and settings to its predecessor");
        check(outputs(ordinary, lowerUpgrade).isEmpty(), "stored resources cannot be dismantled into an inert upgrade base");
        Recipe<?> duplicate = new BackpackUpgradeRecipe(shaped(new ItemStack(iron), Map.of('B', Ingredient.of(leather)), "BB"));
        check(outputs(duplicate, stored(iron)).isEmpty(), "multiple predecessor ingredients cannot duplicate storage");

        // The enchantment-only path must retain the original storage UUID and all other data.
        var enchantment = net.minecraft.core.Holder.direct(net.minecraft.world.item.enchantment.Enchantment.enchantment(
                net.minecraft.world.item.enchantment.Enchantment.definition(net.minecraft.core.HolderSet.direct(leather.builtInRegistryHolder()), 1, 1,
                        net.minecraft.world.item.enchantment.Enchantment.constantCost(1), net.minecraft.world.item.enchantment.Enchantment.constantCost(1),
                        1, net.minecraft.world.entity.EquipmentSlotGroup.CHEST)).build(Identifier.parse("test:enchantment")));
        ItemStack enchanted = stored(leather);
        enchanted.enchant(enchantment, 1);
        config.outputEnchantedBook.set(true);
        config.allowEnchantedItems.set(true);
        var bookOutput = UncraftingTableHelpers.getOutputs(enchanted, List.of(), null).getLeft().getFirst();
        check(bookOutput.getOutputs().getFirst().get(uuid).equals(enchanted.get(uuid)), "enchantment extraction preserves contents identity");
        check(!bookOutput.getOutputs().getFirst().isEnchanted(), "enchantment extraction removes enchantments from the retained backpack");
        check(count(bookOutput, Items.ENCHANTED_BOOK) == 1, "enchantment extraction still returns a book");

        System.out.println("PASSED " + checks + " Sophisticated Backpacks regression checks");
        System.exit(0); // CoolerConfig's file watcher otherwise keeps the standalone JVM alive.
    }

    private static ItemStack stored(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(uuid, UUID.randomUUID());
        stack.set(inventorySlots, 108);
        stack.set(upgradeSlots, 5);
        return stack;
    }

    private static List<UncraftingTableRecipe> outputs(Recipe<?> recipe, ItemStack input) {
        var holder = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.parse("test:recipe")), recipe);
        return UncraftingTableHelpers.getOutputs(input, List.of(holder), null).getLeft();
    }

    private static UncraftingTableRecipe one(Recipe<?> recipe, ItemStack input) {
        List<UncraftingTableRecipe> outputs = outputs(recipe, input);
        check(outputs.size() == 1, "expected one output for " + input + ", got " + outputs.size());
        return outputs.getFirst();
    }

    private static int count(UncraftingTableRecipe output, Item item) {
        return output.getOutputs().stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static ShapedRecipe shaped(ItemStack result, Map<Character, Ingredient> ingredients, String... pattern) {
        return new ShapedRecipe(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                ShapedRecipePattern.of(ingredients, pattern), ItemStackTemplate.fromNonEmptyStack(result));
    }

    private static Item backpack(String name, int slots, int upgrades) {
        return register(name, properties -> new BackpackItem(properties, slots, upgrades));
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Identifier id = Identifier.fromNamespaceAndPath("sophisticatedbackpacks", name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    private static <T> DataComponentType<T> component(String name, Codec<T> codec) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.parse(name), DataComponentType.<T>builder().persistent(codec).build());
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition){
            throw new AssertionError(message);
        }
    }

    private static final class UpgradeItem extends Item implements IUpgradeItem {
        private UpgradeItem(Properties properties) { super(properties); }
    }

    public static final class LegacyWrapper {
        public final ItemStack stack;
        public LegacyWrapper(ItemStack stack) { this.stack = stack; }
    }
}
