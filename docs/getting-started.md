# Getting started

Uncraft Everything adds two blocks that reverse crafting recipes: the [Uncrafting Table](./uncrafting-table) you operate by hand, and the redstone powered [Auto Uncrafting Table](./auto-uncrafting-table).

<div class="ue-blocks">
  <RecipeCard id="uncrafting_table" />
  <RecipeCard id="auto_uncrafting_table" />
</div>

## Installation

Download the mod for your loader and Minecraft version, then put it in your `mods` folder.

| Platform | Link |
| --- | --- |
| CurseForge | [Download from CurseForge](https://www.curseforge.com/minecraft/mc-mods/uncraft-everything) |
| Modrinth | [Download from Modrinth](https://modrinth.com/mod/uncraft-everything) |

The mod is needed on both the client and the server.

## Your first uncraft

1. Craft an [Uncrafting Table](./uncrafting-table#obtaining) and place it.
2. Put the item you want to take apart in the input slot. The slot turns a color if something is stopping the uncraft; hover it to read why.
3. If the item has more than one recipe, pick one from the **recipe list** on the left.
4. Click **UnCraft**. The ingredients go to your inventory and the experience cost is taken from you.

Hold <kbd>Shift</kbd> while clicking **UnCraft** to keep going until the input stack or your experience runs out.

::: tip Changing the rules
Hover the `?` icon in the table's screen to see the [config commands](./uncrafting-table#config-commands). Server owners can change the experience cost, block items and more in the [common config](./common-config).
:::

## Supported versions

Pages for v2.0.0 and later. Pick an older mod version from the version menu in the top bar.

| Mod loader | Minecraft version | Maintained |
| --- | :---: | :---: |
| NeoForge, Fabric | 26.3 | :white_check_mark: |
| NeoForge, Fabric | 26.2 | :white_check_mark: |
| NeoForge, Fabric | 26.1 | :white_check_mark: |
| NeoForge, Fabric, Forge | 1.21.11 | :x: |
| NeoForge, Fabric, Forge | 1.21.10 | :x: |
| NeoForge | 1.21.1 | :x: |
| Forge | 1.20.1 | :x: |

::: warning Older versions are no longer updated
Versions before Minecraft 26.1 only receive fixes for critical bugs, because the code differs too much between `1.20.1`, `1.21.1` and `26.1`.
:::

From the 26.x ports onwards, mod versions start with the Minecraft version they are for, for example `26.3.0.0`. See the [changelog](./changelog) for what changed in each release.

