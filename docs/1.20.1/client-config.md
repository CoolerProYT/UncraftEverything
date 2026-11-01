# Client config

**File:** `config/uncrafteverything-client.toml` on Forge and Fabric.<br>
**Command:** `/ueconfig client`

20.1.0 brings the current client settings screen and color picker to Fabric, alongside Forge.

## Output behaviour

Under `[AutoMove]`, `autoMoveToInventory` defaults to `true`. The manual table sends recovered items to your inventory and drops them into the world if it is full. Set it to `false` to leave materials in the table's output slots for collection.

## Status colors

The settings screen lets you change the colored input-slot overlays. Hover the input slot to read the message; changing its color does not change the server's rules.

The `[StatusColor]` keys are `noRecipeFound`, `noSuitableOutputSlotColor`, `notEnoughExpColor`, `notEnoughInputItemColor`, `notEmptyShulkerColor`, `restrictedByConfigColor`, `damagedItemColor`, `enchantedItemColor`, `lockedItemColor` and `progressionNotDefinedColor`. The TOML stores colors as signed 32-bit ARGB integers; use the in-game color picker to edit them conveniently.

The `showJeiUncraftingCategory` setting documented for newer Minecraft versions is not available in 1.20.1.
