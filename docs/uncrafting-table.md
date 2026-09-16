# Uncrafting Table

<ItemSlot id="uncrafteverything:uncrafting_table" size="large" />

The Uncrafting Table takes a crafted item apart and gives you back the ingredients of one of its recipes.

## Obtaining

<RecipeCard id="uncrafting_table" />

Any type of planks works. This recipe is used from `v1.8.2`; older versions use a [different recipe](/v1.x.x/uncrafting-table#obtaining).

## The screen

![Uncrafting Table screen with the recipe list open for a Crafting Table](/limitation.png){.screenshot}

- **Input slot.** The item to take apart. A colored overlay means it can't be uncrafted right now; hover it to see the reason.
- **Output slots.** Where the ingredients go. With [`autoMoveToInventory`](./client-config#behaviour) on (the default), they are moved straight to your inventory.
- **UnCraft button.** Uncrafts once. <kbd>Shift</kbd> + click repeats until the input or your experience runs out.
- **Experience cost.** The bottle beside the button shows what one uncraft costs. Hover it to see whether that is levels or points.
- **Recipe list.** Shown on the left when the item has recipes. Pick the one to reverse and use the `<` and `>` buttons to page through them.
- **`?` icon.** Hover it to see the [config commands](#config-commands).

Output slots take stacks of the same item, so ingredients are stacked together rather than laid out in the crafting shape.

## Why an item can't be uncrafted

Each reason has its own overlay color, which you can change in the [client config](./client-config#statuscolor).

| Tooltip | What to do |
| --- | --- |
| No Recipe Found! | The item has no crafting or smithing recipe the table can reverse, or every recipe is excluded by the config. |
| No Suitable Output Slot! | Empty the output slots. |
| Not Enough Experience! | Gain more levels or points. See the [experience settings](./common-config#experience). |
| Not Enough Input Item! | The recipe makes more than one item, so put at least that many in the slot. |
| Shulker Box is Not Empty! | Empty the shulker box first. |
| Item Restricted by Config! | The item is on the server's [restriction list](./common-config#restrictions). |
| Item is Damaged! | Repair the item, or see the [damaged item settings](./common-config#damaged). |
| Enchanted Item Not Allowed! | Disenchant the item, or see [`allowEnchantedItems`](./common-config#enchanted). |
| Quest Not Completed! | Finish the FTB Quests quest or chapter the item is [locked behind](./ftb-quest-progression-config). |
| Item Not Defined in Progression! | Only items listed in the [progression config](./ftb-quest-progression-config) can be uncrafted on this server. |

## Damaged and enchanted items

- **Damaged items** give back fewer of their repair material, in proportion to the durability lost. Items with no repair material (bows, shears, fishing rods) are refused unless [`allowDamagedNonRepairable`](./common-config#damaged) is on.
- **Enchanted items** can be uncrafted while [`allowEnchantedItems`](./common-config#enchanted) is on. With `outputEnchantedBook` on, you also get an Enchanted Book with the item's enchantments.
- **Smithing upgrades** such as netherite gear and trimmed armor can be uncrafted while [`allowUnSmithing`](./common-config#unsmithing) is on.

## Automation

Hoppers and pipes can feed the table and empty it.

| Side | What it exposes |
| --- | --- |
| Top and sides | The input slot, insert only |
| Bottom | The output slots, extract only |

The Uncrafting Table still needs a player to click **UnCraft**. For hands-free uncrafting, use the [Auto Uncrafting Table](./auto-uncrafting-table).

## Config commands

| Command | Opens | Who can use it |
| --- | --- | --- |
| `/ueconfig client` | [Client config](./client-config) | Everyone |
| `/ueconfig common` | [Common config](./common-config) | Creative mode or operators |
| `/ueconfig exp` | [Per item exp config](./per-item-exp-config) | Creative mode or operators |
| `/ueconfig progression` | [FTB Quests progression config](./ftb-quest-progression-config) | Creative mode or operators, with FTB Quests installed |

## Limitation

An item doesn't remember what it was made from. A Crafting Table made from birch planks is the same item as one made from oak, so the table can't know which planks were used. Instead, the recipe list offers one entry per plank type (as in the screenshot above) and you choose what you get back.

Turn on [`restrictAmbiguouslyCraftedItems`](./common-config#restrictions) to block these recipes entirely.
