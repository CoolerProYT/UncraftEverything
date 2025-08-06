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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UEConfigScreen extends AbstractScrollableScreen {
    private final Screen parent;
    private final ResponseConfigPayload config = ClientPayloadHandler.payloadFromServer;

    private UncraftEverythingConfig.ExperienceType experienceType = config.experienceType();
    private int experience = config.experience();
    private UncraftEverythingConfig.RestrictionType restrictionType = config.restrictionType();
    private List<String> restrictions = config.restrictedItems();
    private boolean allowEnchantedItems = config.allowEnchantedItem();
    private boolean allowUnsmithing = config.allowUnsmithing();
    private boolean allowDamagedItems = config.allowDamaged();
    private boolean preventModdedIngredientsFromVanillaItems = config.preventModdedIngredientsFromVanillaItems();
    private List<String> restrictedModIngredients = config.restrictedModIngredients();

    private MultiLineEditBox restrictionsInput;
    private EditBox experienceInput;
    private MultiLineEditBox restrictedModInput;
    private Button saveButton;

    protected UEConfigScreen(Component title, Screen parent) {
        super(title, 338);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        Button restrictionTypeButton = Button.builder(Component.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton).bounds(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = MultiLineEditBox.builder().setX(x).setY((int) (baseY + 25 - scrollAmount)).build(this.font, widgetWidth, 88, Component.translatable("screen.uncrafteverything.blank"));
        restrictionsInput.setValue(joined);
        this.addRenderableWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        Button toggleEnchantedBtn = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        }).bounds(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantedBtn);

        // Toggle for experienceType
        Button toggleEnchantmentTypeBtn = Button.builder(
                Component.translatable("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Component.translatable("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
                }
        ).bounds(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new EditBox(this.font, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, Component.translatable("screen.uncrafteverything.blank"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addRenderableWidget(experienceInput);

        // Toggle for allowUnsmithing
        Button toggleAllowUnsmithing = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        }).bounds(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowUnsmithing);

        Button toggleAllowDamaged = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
                allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        }).bounds(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        Button togglePreventModdedIngredientsFromVanillaItems = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        }).bounds(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(togglePreventModdedIngredientsFromVanillaItems);

        // Restricted Mod input box
        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = MultiLineEditBox.builder().setX(x).setY((int) (baseY + 270 - scrollAmount)).build(font, widgetWidth, 88, Component.translatable("screen.uncrafteverything.blank"));
        restrictedModInput.setValue(joinedMod);
        this.addRenderableWidget(restrictedModInput);

        // Save button
        saveButton = Button.builder(Component.translatable("screen.uncrafteverything.save"), this::pressSaveButton).bounds(this.width / 2 - 100, (this.height - 45) + 15, 200, 20).build();
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderMenuBackground(guiGraphics);
        renderBlurredBackground(guiGraphics);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        pGuiGraphics.drawString(this.font, Component.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        Component format = Component.translatable("screen.uncrafteverything.config.restricted_item_label");
        pGuiGraphics.drawWordWrap(this.font, format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        pGuiGraphics.pose().pushMatrix();
        pGuiGraphics.pose().scale(0.65f, 0.65f);
        pGuiGraphics.pose().translate((float) (x * 1.55), (float) (((baseY + 25 - scrollAmount) * 1.6f) + this.font.wordWrapHeight(format, textWidth) * 2 - (this.font.lineHeight * 0.65) + 40));
        pGuiGraphics.drawWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5),0xFFAAAAAA);
        pGuiGraphics.pose().popMatrix();


        Component allowEnchantedItem = Component.translatable("screen.uncrafteverything.config.allow_enchanted_label");
        pGuiGraphics.drawWordWrap(this.font, allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth,0xFFFFFFFF);

        Component expRequired = Component.translatable("screen.uncrafteverything.config.exp_required_label");
        pGuiGraphics.drawWordWrap(this.font, expRequired, x, (int) (baseY + 170 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired, textWidth) / 4d), textWidth,0xFFFFFFFF);

        pGuiGraphics.drawWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component allowDamagedItem = Component.translatable("screen.uncrafteverything.config.allow_damaged_label");
        pGuiGraphics.drawWordWrap(this.font, allowDamagedItem, x, (int) (baseY + 220 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component preventModded = Component.translatable("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label");
        pGuiGraphics.drawWordWrap(this.font, preventModded, x, (int) (baseY + 245 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(preventModded, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component restrictedMod = Component.translatable("screen.uncrafteverything.config.prevent_modid");
        pGuiGraphics.drawWordWrap(this.font, restrictedMod, x, (int) (baseY + 304 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.disableScissor();

        // Draw title and scroll indicator outside scissor area
        pGuiGraphics.drawCenteredString(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);

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
        this.clearWidgets();
        this.init();
        return true;
    }

    @Override
    protected int getMaxScroll() {
        return Math.max(0, contentHeight - (height - 100)); // 100 for header and footer space
    }

    @Override
    public void onClose() {
        RequestConfigPayload.INSTANCE.send(new RequestConfigPayload(), PacketDistributor.SERVER.noArg());
        this.getMinecraft().setScreen(parent);
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

    protected void renderSeparator(GuiGraphics guiGraphics){
        ResourceLocation header = this.getMinecraft().level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        ResourceLocation footer = this.getMinecraft().level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private String getLabel(String label, boolean enabled) {
        return label + (enabled ? "yes" : "no");
    }

    private void pressRestrictionTypeButton(Button button){
        UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
        UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
        restrictionType = next;
        button.setMessage(Component.translatable("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
    }

    private void pressSaveButton(Button button){
        restrictions = Arrays.stream(restrictionsInput.getValue().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        experience = Integer.parseInt(experienceInput.getValue());
        restrictedModIngredients = Arrays.stream(restrictedModInput.getValue().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients);
        UEConfigPayload.INSTANCE.send(configPayload, PacketDistributor.SERVER.noArg());
        RequestConfigPayload.INSTANCE.send(new RequestConfigPayload(), PacketDistributor.SERVER.noArg());
        this.getMinecraft().setScreen(parent);
    }
}
