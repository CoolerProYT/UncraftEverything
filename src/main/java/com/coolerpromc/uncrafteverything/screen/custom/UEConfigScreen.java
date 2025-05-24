package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UEConfigScreen extends Screen {
    private final Screen parent;
    private final ResponseConfigPayload config = ClientPayloadHandler.payloadFromServer;

    private UncraftEverythingConfig.ExperienceType experienceType = config.experienceType();
    private final int experience = config.experience();
    private UncraftEverythingConfig.RestrictionType restrictionType = config.restrictionType();
    private final List<? extends String> restrictions = config.restrictedItems();
    private boolean allowEnchantedItems = config.allowEnchantedItem();
    private boolean allowUnsmithing = config.allowUnsmithing();

    protected UEConfigScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        // Restriction Type Config
        Button restrictionTypeButton = Button.builder(
                Component.literal("Restriction Type: " + restrictionType),
                btn -> {
                    UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
                    UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
                    restrictionType = next;
                    btn.setMessage(Component.literal("Restriction Type: " + next));
                }
        ).bounds(x, 18, widgetWidth, 20).build();
        this.addRenderableWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        MultiLineEditBox restrictionsInput = new MultiLineEditBox(this.font, x, 42, widgetWidth, 88, Component.literal("List of item to restrict for Whitelist/Blacklist"),Component.literal("Restrictions"));
        restrictionsInput.setValue(joined);
        this.addRenderableWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        Button toggleEnchantedBtn = Button.builder(Component.literal(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Component.literal(getLabel(allowEnchantedItems)));
        }).bounds(x, 135, widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantedBtn);

        // Toggle for enchantmentType
        Button toggleEnchantmentTypeBtn = Button.builder(
                Component.literal("Experience Type: " + experienceType),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Component.literal("Experience Type: " + next));
                }
        ).bounds(x, 158, widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        EditBox experienceInput = new EditBox(this.font, x, 183, widgetWidth, 20, Component.literal("Experience Input"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addRenderableWidget(experienceInput);

        // Toggle for allowUnsmithing
        Button toggleAllowUnsmithing = Button.builder(Component.literal(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.literal(getUnsmithingLabel(allowUnsmithing)));
        }).bounds(x, 208, widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowUnsmithing);

        // Save button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
            String[] entries = restrictionsInput.getValue().split("\n");
            List<String> restrictedItems = Arrays.stream(entries)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            int expValue = this.experience;
            try {
                expValue = Integer.parseInt(experienceInput.getValue());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid experience value, using default: " + this.experience);
            }

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing);
            PacketDistributor.sendToServer(configPayload);
            PacketDistributor.sendToServer(new RequestConfigPayload());
            this.getMinecraft().setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 23, 200, 20).build());
    }

    private String getLabel(boolean enabled) {
        return "Allow Enchanted Items: " + (enabled ? "Yes" : "No");
    }

    private String getUnsmithingLabel(boolean enabled) {
        return "Allow Unsmithing: " + (enabled ? "Yes" : "No");
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;

        pGuiGraphics.drawCenteredString(this.font, Component.literal("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFF);
        pGuiGraphics.drawString(this.font, "Restriction Type", x, 18 + (this.font.lineHeight / 2) + 2, 0xFFFFFF);
        Component format = Component.literal("Restricted Items \n(Please write each item in new line)");
        pGuiGraphics.drawWordWrap(this.font, format, x, 42 + (this.font.lineHeight / 2) + 20, textWidth,0xFFFFFF);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.65f, 0.65f, 0f);
        pGuiGraphics.pose().translate(x * 1.55, (42 * 1.6) + this.font.wordWrapHeight(format, textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 0);
        pGuiGraphics.drawWordWrap(this.font, Component.literal("Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0, (int) (textWidth * 1.5),11184810);
        pGuiGraphics.pose().popPose();
        Component allowEnchantedItem = Component.literal("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        pGuiGraphics.drawWordWrap(this.font, allowEnchantedItem, x, 135 + (this.font.lineHeight / 2) + 1 - this.font.wordWrapHeight(allowEnchantedItem, textWidth) / 4, textWidth, 0xFFFFFF);
        pGuiGraphics.drawWordWrap(this.font, Component.literal("Experience Type"), x, 158 + (this.font.lineHeight / 2) + 2, textWidth,0xFFFFFF);
        Component expRequired = Component.literal("Experience Required \n(Level/Point based on previous config)");
        pGuiGraphics.drawWordWrap(this.font, expRequired, x, 183 + (this.font.lineHeight / 2) + 1 - this.font.wordWrapHeight(expRequired, textWidth) / 4, textWidth,0xFFFFFF);
        pGuiGraphics.drawWordWrap(this.font, Component.literal("Allow Unsmithing (Netherite/Trimmed Armor)"), x, 208 + (this.font.lineHeight / 2) + 2, textWidth, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }
}