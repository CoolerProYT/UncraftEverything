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
    private boolean allowDamagedItems = config.allowDamaged();

    // Scroll variablesAdd commentMore actions
    private double scrollAmount = 0.0;
    private final int CONTENT_HEIGHT = 240;
    private MultiLineEditBox restrictionsInput;
    private EditBox experienceInput;
    private Button saveButton;

    protected UEConfigScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    private double getMaxScroll() {
        return Math.max(0, CONTENT_HEIGHT - (height - 100)); // 100 for header and footer space
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 40; // Start position, accounting for title and scroll

        // Restriction Type Config
        Button restrictionTypeButton = Button.builder(
                Component.literal("Restriction Type: " + restrictionType),
                btn -> {
                    UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
                    UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
                    restrictionType = next;
                    btn.setMessage(Component.literal("Restriction Type: " + next));
                }
        ).bounds(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        MultiLineEditBox restrictionsInput = new MultiLineEditBox(this.font, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Component.literal("List of item to restrict for Whitelist/Blacklist"),Component.literal("Restrictions"));
        restrictionsInput.setValue(joined);
        this.addRenderableWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        Button toggleEnchantedBtn = Button.builder(Component.literal(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Component.literal(getLabel(allowEnchantedItems)));
        }).bounds(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantedBtn);

        // Toggle for experienceType
        Button toggleEnchantmentTypeBtn = Button.builder(
                Component.literal("Experience Type: " + experienceType),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Component.literal("Experience Type: " + next));
                }
        ).bounds(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new EditBox(this.font, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, Component.literal("Experience Input"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addRenderableWidget(experienceInput);

        // Toggle for allowUnsmithing
        Button toggleAllowUnsmithing = Button.builder(Component.literal(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.literal(getUnsmithingLabel(allowUnsmithing)));
        }).bounds(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowUnsmithing);

        Button toggleAllowDamaged = Button.builder(Component.literal(getDamagedLabel(allowDamagedItems)), btn -> {
                allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Component.literal(getDamagedLabel(allowDamagedItems)));
        }).bounds(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowDamaged);

        // Save button
        saveButton = Button.builder(Component.literal("Done"), btn -> {
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

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing, allowDamagedItems);
            PacketDistributor.sendToServer(configPayload);
            PacketDistributor.sendToServer(new RequestConfigPayload());
            this.getMinecraft().setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 23, 200, 20).build();
        this.addRenderableWidget(saveButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double newScroll = scrollAmount - verticalAmount * 10; // 10 is scroll speed
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));

        this.clearWidgets();
        this.init();

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264) { // Down arrow
            mouseScrolled(0, 0, 0, -1);
            return true;
        } else if (keyCode == 265) { // Up arrow
            mouseScrolled(0, 0, 0, 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String getLabel(boolean enabled) {
        return "Allow Enchanted Items: " + (enabled ? "Yes" : "No");
    }

    private String getUnsmithingLabel(boolean enabled) {
        return "Allow Unsmithing: " + (enabled ? "Yes" : "No");
    }

    private String getDamagedLabel(boolean enabled) {
        return "Allow Damaged Items: " + (enabled ? "Yes" : "No");
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 40;

        pGuiGraphics.drawString(this.font, "Restriction Type", x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        Component format = Component.literal("Restricted Items \n(Please write each item in new line)");
        pGuiGraphics.drawWordWrap(this.font, format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.65f, 0.65f, 0f);
        pGuiGraphics.pose().translate(x * 1.55, ((baseY + 25 - scrollAmount) * 1.6) + this.font.wordWrapHeight(format, textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 0);
        pGuiGraphics.drawWordWrap(this.font, Component.literal("Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0, (int) (textWidth * 1.5),0xFFAAAAAA);
        pGuiGraphics.pose().popPose();

        Component allowEnchantedItem = Component.literal("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        pGuiGraphics.drawWordWrap(this.font, allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWordWrap(this.font, Component.literal("Experience Type"), x, (int) (baseY + 145 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth,0xFFFFFFFF);

        Component expRequired = Component.literal("Experience Required \n(Level/Point based on previous config)");
        pGuiGraphics.drawWordWrap(this.font, expRequired, x, (int) (baseY + 170 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired, textWidth) / 4d), textWidth,0xFFFFFFFF);

        pGuiGraphics.drawWordWrap(this.font, Component.literal("Allow Unsmithing (Netherite/Trimmed Armor)"), x, (int) (baseY + 195 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWordWrap(this.font, Component.literal("Allow Damaged Items to be Uncrafted (Reapair Item will be deducted)"), x, (int) (baseY + 220 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(Component.literal("Allow Damaged Items to be Uncrafted (Reapair Item will be deducted)"), textWidth) / 4d), textWidth, 0xFFFFFF);

        pGuiGraphics.disableScissor();

        // Draw title and scroll indicator outside scissor area
        pGuiGraphics.drawCenteredString(this.font, Component.literal("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFF);

        // Draw scroll indicator if content overflows
        if (getMaxScroll() > 0) {
            int scrollBarHeight = Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) CONTENT_HEIGHT));
            int scrollBarY = (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 70 - scrollBarHeight));
            pGuiGraphics.fill(this.width - 6, scrollBarY, this.width, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
            pGuiGraphics.fill(this.width - 6, 25, this.width, this.height - 45, 0x44000000);
        }

        saveButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }
}
