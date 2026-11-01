# Auto Uncrafting Table

Available on **both Forge and Fabric** in 20.1.0 for Minecraft 1.20.1. This is a new feature for players upgrading from Fabric 1.9.2.

## Obtaining

Craft it with five iron ingots, an Uncrafting Table, two redstone dust and a dropper:

```text
iron      iron             iron
iron      Uncrafting Table iron
redstone  dropper          redstone
```

## Power and experience

The automatic table processes items while powered by redstone. It pays the configured uncrafting cost from its **stored experience**, rather than directly from a nearby player.

Open the table and use the experience controls to deposit or withdraw experience. The controls let you adjust the transfer amount and choose levels or points. Deposit enough experience for the selected recipe before turning on your setup. A zero-cost recipe does not require stored experience.

## Item automation

Insert items from the **top or sides**, and extract the recovered materials from the **bottom**, using hoppers or compatible item transport. The table has nine output slots and checks the complete batch before consuming input or experience.

Choose the recipe in the table's screen when several recipes are available. Recipe-selection history keeps that choice for subsequent input, including automated processing without the screen open, and survives saving and reloading the world.

## Troubleshooting

Check redstone power, stored experience, input batch size and output space. The same [recipe restrictions](./common-config) apply as for the manual table. Hover the input status to read why processing is blocked.

Optional [TaCZ compatibility](./tacz-compatibility) works in both tables on Forge. Ammunition still needs a complete material batch, and all gun materials and recovered attachments must fit in the output.
