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

    protected UEConfigScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        // Restriction Type Config
        ButtonWidget restrictionTypeButton = ButtonWidget.builder(
                Text.literal("Restriction Type: " + restrictionType),
                btn -> {
                    UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
                    UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
                    restrictionType = next;
                    btn.setMessage(Text.literal("Restriction Type: " + next));
                }
        ).dimensions(x, 18, widgetWidth, 20).build();
        this.addDrawableChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        EditBoxWidget restrictionsInput = new EditBoxWidget(this.textRenderer, x, 42, widgetWidth, 88, Text.literal("List of item to restrict for Whitelist/Blacklist"),Text.literal("Restrictions"));
        restrictionsInput.setText(joined);
        this.addDrawableChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        ButtonWidget toggleEnchantedBtn = ButtonWidget.builder(Text.literal(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Text.literal(getLabel(allowEnchantedItems)));
        }).dimensions(x, 135, widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantedBtn);

        // Toggle for enchantmentType
        ButtonWidget toggleEnchantmentTypeBtn = ButtonWidget.builder(
                Text.literal("Experience Type: " + experienceType),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Text.literal("Experience Type: " + next));
                }
        ).dimensions(x, 158, widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantmentTypeBtn);

        // Experience input box
        TextFieldWidget experienceInput = new TextFieldWidget(this.textRenderer, x, 183, widgetWidth, 20, Text.literal("Experience Input"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addDrawableChild(experienceInput);

        // Toggle for allowUnsmithing
        ButtonWidget toggleAllowUnsmithing = ButtonWidget.builder(Text.literal(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Text.literal(getUnsmithingLabel(allowUnsmithing)));
        }).dimensions(x, 208, widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowUnsmithing);

        // Save button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
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

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing);
            ClientPlayNetworking.send(configPayload);
            ClientPlayNetworking.send(new RequestConfigPayload());
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 23, 200, 20).build());
    }

    private String getLabel(boolean enabled) {
        return "Allow Enchanted Items: " + (enabled ? "Yes" : "No");
    }

    private String getUnsmithingLabel(boolean enabled) {
        return "Allow Unsmithing: " + (enabled ? "Yes" : "No");
    }

    @Override
    public void render(@NotNull DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;

        pGuiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderline(true)), this.width / 2, 4, 0xFFFFFF);
        pGuiGraphics.drawTextWithShadow(this.textRenderer, "Restriction Type", x, 18 + (this.textRenderer.fontHeight / 2) + 2, 0xFFFFFF);
        Text format = Text.literal("Restricted Items \n(Please write each item in new line)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, format, x, 42 + (this.textRenderer.fontHeight / 2) + 20, textWidth,0xFFFFFF);
        pGuiGraphics.getMatrices().push();
        pGuiGraphics.getMatrices().scale(0.65f, 0.65f, 0f);
        pGuiGraphics.getMatrices().translate(x * 1.55, (42 * 1.6) + this.textRenderer.getWrappedLinesHeight(format, textWidth) * 2 - (this.textRenderer.fontHeight * 0.65) + 40, 0);
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0, (int) (textWidth * 1.5),11184810);
        pGuiGraphics.getMatrices().pop();
        Text allowEnchantedItem = Text.literal("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, allowEnchantedItem, x, 135 + (this.textRenderer.fontHeight / 2) + 1 - this.textRenderer.getWrappedLinesHeight(allowEnchantedItem, textWidth) / 4, textWidth, 0xFFFFFF);
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Experience Type"), x, 158 + (this.textRenderer.fontHeight / 2) + 2, textWidth,0xFFFFFF);
        Text expRequired = Text.literal("Experience Required \n(Level/Point based on previous config)");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, expRequired, x, 183 + (this.textRenderer.fontHeight / 2) + 1 - this.textRenderer.getWrappedLinesHeight(expRequired, textWidth) / 4, textWidth,0xFFFFFF);
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.literal("Allow Unsmithing (Netherite/Trimmed Armor)"), x, 208 + (this.textRenderer.fontHeight / 2) + 2, textWidth, 0xFFFFFF);
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }
}