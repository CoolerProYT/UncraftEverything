package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UEConfigScreen extends AbstractScrollableScreen {
    private final Screen parent;
    private final ResponseConfigPayload config = UncraftEverythingClient.payloadFromServer;

    private UncraftEverythingConfig.ExperienceType experienceType = config.experienceType();
    private int experience = config.experience();
    private UncraftEverythingConfig.RestrictionType restrictionType = config.restrictionType();
    private List<String> restrictions = config.restrictedItems();
    private boolean allowEnchantedItems = config.allowEnchantedItem();
    private boolean allowUnsmithing = config.allowUnsmithing();
    private boolean allowDamagedItems = config.allowDamaged();
    private boolean preventModdedIngredientsFromVanillaItems = config.preventModdedIngredientsFromVanillaItems();
    private List<String> restrictedModIngredients = config.restrictedModIngredients();

    private EditBoxWidget restrictionsInput;
    private TextFieldWidget experienceInput;
    private EditBoxWidget restrictedModInput;
    private ButtonWidget saveButton;

    protected UEConfigScreen(Text title, Screen parent) {
        super(title, 338);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        // Restriction Type Config
        ButtonWidget restrictionTypeButton =  ButtonWidget.builder(Text.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton).dimensions(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new EditBoxWidget(this.textRenderer, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88,
                Text.translatable("screen.uncrafteverything.blank"), Text.translatable("screen.uncrafteverything.blank"));
        restrictionsInput.setText(joined);
        this.addDrawableChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        ButtonWidget toggleEnchantedBtn = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        }).dimensions(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantedBtn);

        // Toggle for experienceType
        ButtonWidget toggleEnchantmentTypeBtn = ButtonWidget.builder(
                Text.translatable("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Text.translatable("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
                }
        ).dimensions(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.textRenderer, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20,
                Text.translatable("screen.uncrafteverything.blank"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addDrawableChild(experienceInput);

        // Toggle for allowUnsmithing
        ButtonWidget toggleAllowUnsmithing = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        }).dimensions(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowUnsmithing);

        ButtonWidget toggleAllowDamaged = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        }).dimensions(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        ButtonWidget togglePreventModdedIngredientsFromVanillaItems = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        }).dimensions(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(togglePreventModdedIngredientsFromVanillaItems);

        // Restricted Mod input box
        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = new EditBoxWidget(textRenderer, x, (int) (baseY + 270 - scrollAmount), widgetWidth, 88, Text.translatable("screen.uncrafteverything.blank"), Text.translatable("screen.uncrafteverything.blank"));
        restrictedModInput.setText(joinedMod);
        this.addDrawableChild(restrictedModInput);

        // Save button (always at bottom)
        saveButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.save"), this::pressSaveButton).dimensions(this.width / 2 - 100, (this.height - 45) + 15, 200, 20).build();
        this.addDrawableChild(saveButton);
    }

    public void renderBackground(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDarkening(guiGraphics);
        applyBlur(partialTick);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
    }

    @Override
    public void render(@NotNull DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        for(Drawable drawable : this.drawables) {
            drawable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        // Render labels with scroll offset
        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        pGuiGraphics.drawTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFFFF);

        Text format = Text.translatable("screen.uncrafteverything.config.restricted_item_label");
        pGuiGraphics.drawTextWrapped(this.textRenderer, format, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 20), textWidth, 0xFFFFFFFF);

        // Format help text
        pGuiGraphics.getMatrices().push();
        pGuiGraphics.getMatrices().scale(0.65f, 0.65f, 0);
        pGuiGraphics.getMatrices().translate(x * 1.55f, (float) (((baseY + 25f - scrollAmount) * 1.6f) + this.textRenderer.getWrappedLinesHeight(format, textWidth) * 2f - (this.textRenderer.fontHeight * 0.65f) + 40f), 0);
        pGuiGraphics.drawTextWrapped(this.textRenderer, Text.translatable("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5), 0xFFAAAAAA);
        pGuiGraphics.getMatrices().pop();

        Text allowEnchantedItem = Text.translatable("screen.uncrafteverything.config.allow_enchanted_label");
        pGuiGraphics.drawTextWrapped(this.textRenderer, allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawTextWrapped(this.textRenderer, Text.translatable("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text expRequired = Text.translatable("screen.uncrafteverything.config.exp_required_label");
        pGuiGraphics.drawTextWrapped(this.textRenderer, expRequired, x, (int) (baseY + 170 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(expRequired, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawTextWrapped(this.textRenderer, Text.translatable("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text allowDamagedItem = Text.translatable("screen.uncrafteverything.config.allow_damaged_label");
        pGuiGraphics.drawTextWrapped(this.textRenderer, allowDamagedItem, x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text preventModded = Text.translatable("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label");
        pGuiGraphics.drawTextWrapped(this.textRenderer, preventModded, x, (int) (baseY + 245 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(preventModded, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text restrictedMod = Text.translatable("screen.uncrafteverything.config.prevent_modid");
        pGuiGraphics.drawTextWrapped(this.textRenderer, restrictedMod, x, (int) (baseY + 304 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.disableScissor();

        pGuiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);

        saveButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected int scrollBarX() {
        return this.width - 6;
    }

    @Override
    protected int scrollBarY() {
        int scrollBarHeight = Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) contentHeight));
        return (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 70 - scrollBarHeight));
    }

    @Override
    protected int scrollerHeight() {
        return Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) contentHeight));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        this.clearChildren();
        this.init();
        return true;
    }

    @Override
    protected int getMaxScroll() {
        return Math.max(0, contentHeight - (height - 100)); // 100 for header and footer space
    }


    @Override
    public void close() {
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scrollTop = 25;
        int scrollBottom = this.height - 45;

        boolean inScrollArea = mouseY >= scrollTop && mouseY <= scrollBottom;

        if (!inScrollArea) {
            if (!saveButton.isMouseOver(mouseX, mouseY)) {
                return false;
            }
            else{
                return saveButton.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void renderSeparator(DrawContext guiGraphics){
        Identifier header = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier footer = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        guiGraphics.drawTexture(header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.drawTexture(footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private String getLabel(String label, boolean enabled) {
        return label + (enabled ? "yes" : "no");
    }

    private void pressRestrictionTypeButton(ButtonWidget button){
        UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
        UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
        restrictionType = next;
        button.setMessage(Text.translatable("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
    }

    private void pressSaveButton(ButtonWidget button){
        restrictions = Arrays.stream(restrictionsInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        experience = Integer.parseInt(experienceInput.getText());
        restrictedModIngredients = Arrays.stream(restrictedModInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients);
        ClientPlayNetworking.send(configPayload);
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }
}