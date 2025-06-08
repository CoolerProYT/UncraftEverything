package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UEConfigScreen extends Screen {
    private final Screen parent;
    private final ResponseConfigPayload config = UncraftEverythingClient.payloadFromServer;

    private UncraftEverythingConfig.ExperienceType experienceType = config.experienceType();
    private final int experience = config.experience();
    private UncraftEverythingConfig.RestrictionType restrictionType = config.restrictionType();
    private final List<? extends String> restrictions = config.restrictedItems();
    private boolean allowEnchantedItems = config.allowEnchantedItem();
    private boolean allowUnsmithing = config.allowUnsmithing();
    private boolean allowDamagedItems = config.allowDamaged();

    // Scroll variables
    private double scrollAmount = 0.0;
    private final int CONTENT_HEIGHT = 240;
    private EditBoxWidget restrictionsInput;
    private TextFieldWidget experienceInput;
    private ButtonWidget saveButton;

    protected UEConfigScreen(Text title, Screen parent) {
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
        ButtonWidget restrictionTypeButton = ButtonWidget.builder(
                Text.literal("Restriction Type: " + restrictionType),
                btn -> {
                    UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
                    UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
                    restrictionType = next;
                    btn.setMessage(Text.literal("Restriction Type: " + next));
                }
        ).dimensions(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new EditBoxWidget(this.textRenderer, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88,
                Text.literal("List of item to restrict for Whitelist/Blacklist"), Text.literal("Restrictions"));
        restrictionsInput.setText(joined);
        this.addDrawableChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        ButtonWidget toggleEnchantedBtn = ButtonWidget.builder(Text.literal(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Text.literal(getLabel(allowEnchantedItems)));
        }).dimensions(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantedBtn);

        // Toggle for experienceType
        ButtonWidget toggleEnchantmentTypeBtn = ButtonWidget.builder(
                Text.literal("Experience Type: " + experienceType),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Text.literal("Experience Type: " + next));
                }
        ).dimensions(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.textRenderer, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20,
                Text.literal("Experience Input"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addDrawableChild(experienceInput);

        // Toggle for allowUnsmithing
        ButtonWidget toggleAllowUnsmithing = ButtonWidget.builder(Text.literal(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Text.literal(getUnsmithingLabel(allowUnsmithing)));
        }).dimensions(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowUnsmithing);

        ButtonWidget toggleAllowDamaged = ButtonWidget.builder(Text.literal(getDamagedLabel(allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Text.literal(getDamagedLabel(allowDamagedItems)));
        }).dimensions(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowDamaged);

        // Save button (always at bottom)
        saveButton = ButtonWidget.builder(Text.literal("Done"), btn -> {
            String[] entries = restrictionsInput.getText().split("\n");
            List<String> restrictedItems = Arrays.stream(entries)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            int expValue = this.experience;
            try {
                expValue = Integer.parseInt(experienceInput.getText());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid experience value, using default: " + this.experience);
            }

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing, allowDamagedItems);
            ClientPlayNetworking.send(configPayload);
            ClientPlayNetworking.send(new RequestConfigPayload());
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 23, 200, 20).build();
        this.addDrawableChild(saveButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double newScroll = scrollAmount - verticalAmount * 10; // 10 is scroll speed
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));

        this.clearChildren();
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
    public void render(@NotNull DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Render labels with scroll offset
        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 40;

        pGuiGraphics.drawTextWithShadow(this.textRenderer, "Restriction Type", x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFFFF);

        Text format = Text.literal("Restricted Items \n(Please write each item in new line)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, format, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 20), textWidth, 0xFFFFFFFF);

        // Format help text
        pGuiGraphics.getMatrices().push();
        pGuiGraphics.getMatrices().scale(0.65f, 0.65f, 0f);
        pGuiGraphics.getMatrices().translate(x * 1.55, ((baseY + 25 - scrollAmount) * 1.6) + this.textRenderer.getWrappedLinesHeight(format, textWidth) * 2 - (this.textRenderer.fontHeight * 0.65) + 40, 0);
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0, (int) (textWidth * 1.5), 0xFFAAAAAA);
        pGuiGraphics.getMatrices().pop();

        Text allowEnchantedItem = Text.literal("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Experience Type"), x, (int) (baseY + 145 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text expRequired = Text.literal("Experience Required \n(Level/Point based on previous config)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, expRequired, x, (int) (baseY + 170 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(expRequired, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Allow Unsmithing (Netherite/Trimmed Armor)"), x, (int) (baseY + 195 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Allow Damaged Items to be Uncrafted (Reapair Item will be deducted)"), x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(Text.literal("Allow Damaged Items to be Uncrafted (Enchanted Book will be given)"), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.disableScissor();

        // Draw title and scroll indicator outside scissor area
        pGuiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderline(true)), this.width / 2, 4, 0xFFFFFF);

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
    public void close() {
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }
}