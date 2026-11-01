# Uncrafting Table

This page covers **20.1.0 for Minecraft 1.20.1**, on Forge and Fabric.

## Obtaining

Craft the table with four planks, one crafting table and one diamond:

```text
(empty)  (empty)         (empty)
plank    crafting table plank
plank    diamond        plank
```

Any plank accepted by the `minecraft:planks` tag can be used.

## Using the table

Place the item in the input slot. The recipe list on the left lets you choose between available recipes; use its page controls when there are more results. Click **UnCraft** once, or hold Shift while clicking to repeat while the input, experience and output space allow it.

With [autoMoveToInventory](./client-config#output-behaviour) enabled, recovered items move to your inventory; if it is full, they drop into the world. With it disabled, collect the materials from the table's nine output slots. Shift-click can move output stacks into your inventory.

Experience and item restrictions follow the [common config](./common-config), with optional [per-item costs](./per-item-exp-config) and [FTB Quests progression](./ftb-quest-progression-config). The selected recipe is remembered for later use.

## Reading blocked states

Hover the colored input slot for the reason uncrafting cannot proceed.

| Message | What to check |
| --- | --- |
| No Recipe Found | The item needs a supported reverse recipe; modpack recipes and restrictions can affect availability. |
| Not Enough Input Item | Add enough items for a complete recipe batch. A recipe can exist even when the current stack is too small. |
| No Suitable Output Slot | Clear space for the complete output batch. There are nine output slots; all returned materials must fit. |
| Not Enough Exp | Supply the experience required by the selected recipe. |
| Restricted, damaged, enchanted or progression-related messages | Check the server's common rules and quest requirements. |

For Forge TaCZ guns and ammo, see the [batch and output rules](./tacz-compatibility).

## Config commands

Hover the `?` icon for the configuration commands. `/ueconfig common`, `/ueconfig exp` and `/ueconfig progression` edit server rules and require creative mode or operator permission; progression also needs FTB Quests. `/ueconfig client` opens your local settings.
