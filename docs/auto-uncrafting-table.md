# Auto Uncrafting Table

<ItemSlot id="uncrafteverything:auto_uncrafting_table" size="large" />

The Auto Uncrafting Table does the same job as the [Uncrafting Table](./uncrafting-table), but uncrafts on its own while it has a redstone signal. Added in `v2.0.0`.

## Obtaining

<RecipeCard id="auto_uncrafting_table" />

## How it works

1. Put the item to take apart in the input slot, or feed it in with a hopper.
2. If the item has several recipes, pick one from the recipe list. The table remembers the choice for that item, so it keeps using it for later stacks.
3. Store experience in the table (see below).
4. Power the table with redstone. While powered, it uncrafts continuously as long as it has a valid recipe, enough stored experience and room in the output slots.

The block's texture changes when it is powered and again while it is actively uncrafting, so you can see at a glance whether it is working.

## Stored experience

The Auto Uncrafting Table pays for uncrafting from its own experience store, not from a player.

- Use the **+** and **-** buttons to move experience from you to the table and back.
- Scroll over the **amount** and **type** widgets beside them to set how much is moved each click, in levels or points.
- Hover the experience bar to see how much is stored and what the current recipe requires.

::: info Why levels are converted
Levels are turned into experience points with the vanilla formula before they are stored. One level at level 30 is worth far more than one level at level 1, so storing points prevents free experience and experience loss.
:::

## Automation

| Side | What it exposes |
| --- | --- |
| Top and sides | The input slot, insert only |
| Bottom | The output slots, extract only |

A hopper above the table and one below it make a complete uncrafting line. Remember to keep the table powered and its experience topped up.
