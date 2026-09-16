# Uncrafting Table

Uncrafting table is a block entity that provide ability to uncraft items.

## Obtaining

Uncrafting table can be crafted in Crafting Table. Any type of planks works.

<RecipeCard :grid="[null, null, null, '#minecraft:planks', 'minecraft:crafting_table', '#minecraft:planks', '#minecraft:planks', 'minecraft:diamond', '#minecraft:planks']" result="uncrafteverything:uncrafting_table" />

::: info
This recipe is not applicable to version below `v1.8.2`
:::

## Limitation

Because we cannot know what are the ingredients used to craft an item (e.g. a crafting table can be crafted with different planks), the Uncrafting Table will let player to select which type of ingredient to output instead of directly output the original used ingredients.

![](/limitation.png){.screenshot}