# Client config

The client config holds settings that only affect your own screen, such as the status overlay colors and whether JEI shows the Uncrafting category. Each player sets them for themselves, and the server can't change them. Added in `v2.0.0`.

**File:** `config/uncrafteverything-client.toml`<br>
**Command:** `/ueconfig client`

## Options

### Behaviour

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `autoMoveToInventory` | Boolean | `true` | Moves uncrafted items into your inventory when you click **UnCraft**. Items that don't fit are dropped. Does not apply to the Auto Uncrafting Table. |
| `showJeiUncraftingCategory` | Boolean | `true` | Shows the Uncrafting category in JEI. Turn it off to hide the category and its recipes from JEI. Changes apply straight away, no restart needed. Only has an effect when JEI is installed. Added in `26.3.0.0`, `26.2.0.2`, `26.1.2.11` and `2.1.5` (1.21.1 NeoForge). |

### StatusColor

The overlay color on the input slot for each [reason an item can't be uncrafted](./uncrafting-table#why-an-item-can-t-be-uncrafted).

<StatusColors />

::: info Color format
Colors are stored as a signed decimal of the ARGB hex value. For example, `#FFFF615C` (opaque red) is written as `-40612`. The in-game screen has a color picker, so you rarely need to convert by hand.
:::

## Example file

```toml [uncrafteverything-client.toml]
# UncraftEverything Client Configuration

[Behaviour]
	# Auto move uncrafted items to player inventory, drops to world if inventory is full.
	autoMoveToInventory = true
	# Show the Uncrafting category in JEI. Only affects you, other players are not affected.
	showJeiUncraftingCategory = true

[StatusColor]
	# Overlay color for No Recipe Found
	noRecipeFound = -40612
	# Overlay color for No Suitable Output Slot
	noSuitableOutputSlotColor = -226487
	# Overlay color for Not Enough Exp
	notEnoughExpColor = -852102
	# Overlay color for Not Enough Input Item
	notEnoughInputItemColor = -4288
	# Overlay color for Not Empty Shulker
	notEmptyShulkerColor = -1799425
	# Overlay color for Restricted By Config
	restrictedByConfigColor = -11579569
	# Overlay color for Damaged Item
	damagedItemColor = -40612
	# Overlay color for Enchanted Item
	enchantedItemColor = -40612
	# Overlay color for Locked Item
	lockedItemColor = -40612
	# Overlay color for Progression Not Defined
	progressionNotDefinedColor = -40612
```

## In-game screen

Run `/ueconfig client` to edit these settings with a color picker.

![Client config screen](/client_config_screen.png){.screenshot}

![Color picker](/color_picker.png){.screenshot}
