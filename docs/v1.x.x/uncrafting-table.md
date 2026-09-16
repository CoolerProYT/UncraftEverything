# Uncrafting Table

Uncrafting table is a block entity that provide ability to uncraft items.

## Obtaining

Uncrafting table can be crafted in Crafting Table.

<RecipeCard :grid="['minecraft:crafting_table', 'minecraft:crafting_table', null, 'minecraft:crafting_table', 'minecraft:crafting_table', null, null, null, null]" result="uncrafteverything:uncrafting_table" />

::: info
This recipe is not applicable to version above `v1.8.2`
:::

## Limitation

Because we cannot know what are the ingredients used to craft an item (e.g. a crafting table can be crafted with different planks), the Uncrafting Table will let player to select which type of ingredient to output instead of directly output the original used ingredients.

![](/limitation.png){.screenshot}