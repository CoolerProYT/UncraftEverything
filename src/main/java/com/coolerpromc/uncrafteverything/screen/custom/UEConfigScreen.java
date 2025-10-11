package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private boolean enableProgression = config.enableProgression();
    private boolean onlyAllowDefinedProgression = config.onlyAllowDefinedProgression();
    private boolean outputEnchantedBook = config.outputEnchantedBook();

    private Button restrictionTypeButton;
    private Button toggleEnchantedBtn;
    private Button toggleEnchantmentTypeBtn;
    private Button toggleAllowUnsmithing;
    private Button toggleAllowDamaged;
    private Button toggleEnableProgression;
    private Button toggleOutputEnchantedBook;
    private Button toggleOnlyAllowDefinedProgression;
    private Button togglePreventModdedIngredientsFromVanillaItems;
    private MultiLineEditBox restrictionsInput;
    private EditBox experienceInput;
    private MultiLineEditBox restrictedModInput;
    private Button saveButton;

    protected UEConfigScreen(Component title, Screen parent) {
        super(title, 413);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        restrictionTypeButton = Button.builder(Component.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton).bounds(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.font, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 90, Component.translatable("screen.uncrafteverything.blank"), Component.translatable("screen.uncrafteverything.blank"));
        restrictionsInput.setValue(joined);
        this.addRenderableWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        }).bounds(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantedBtn);

        // Toggle for experienceType
        toggleEnchantmentTypeBtn = Button.builder(
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
        toggleAllowUnsmithing = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        }).bounds(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowUnsmithing);

        toggleAllowDamaged = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
                allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        }).bounds(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        togglePreventModdedIngredientsFromVanillaItems = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        }).bounds(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(togglePreventModdedIngredientsFromVanillaItems);

        // Restricted Mod input box
        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = new MultiLineEditBox(font, x, (int) (baseY + 270 - scrollAmount), widgetWidth, 90, Component.translatable("screen.uncrafteverything.blank"), Component.translatable("screen.uncrafteverything.blank"));
        restrictedModInput.setValue(joinedMod);
        this.addRenderableWidget(restrictedModInput);

        toggleEnableProgression = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)), btn -> {
            enableProgression = !enableProgression;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)));
        }).bounds(x, (int) (baseY + 365 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnableProgression);

        toggleOnlyAllowDefinedProgression = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)), btn -> {
            onlyAllowDefinedProgression = !onlyAllowDefinedProgression;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)));
        }).bounds(x, (int) (baseY + 390 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleOnlyAllowDefinedProgression);

        // Toggle for allowEnchantedItems
        toggleOutputEnchantedBook = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.output_enchanted_book_", outputEnchantedBook)), btn -> {
            outputEnchantedBook = !outputEnchantedBook;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.output_enchanted_book_", outputEnchantedBook)));
        }).bounds(x, (int) (baseY + 415 - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleOutputEnchantedBook);

        // Save button
        saveButton = Button.builder(Component.translatable("screen.uncrafteverything.save"), this::pressSaveButton).bounds(this.width / 2 - 100, (this.height - 45) + 15, 200, 20).build();
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, guiGraphics));
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        for(Renderable renderable : this.renderables) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        pGuiGraphics.drawString(this.font, Component.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        Component format = Component.translatable("screen.uncrafteverything.config.restricted_item_label");
        pGuiGraphics.drawWordWrap(this.font, format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.65f, 0.65f, 0f);
        pGuiGraphics.pose().translate(x * 1.55, ((baseY + 25 - scrollAmount) * 1.6) + this.font.wordWrapHeight(format, textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 0);
        pGuiGraphics.drawWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5),0xFFAAAAAA);
        pGuiGraphics.pose().popPose();

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

        Component enableProgression = Component.translatable("screen.uncrafteverything.config.enable_progression");
        pGuiGraphics.drawWordWrap(this.font, enableProgression, x, (int) (baseY + 367 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(enableProgression, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component onlyAllowDefined = Component.translatable("screen.uncrafteverything.config.only_allow_defined_progression");
        pGuiGraphics.drawWordWrap(this.font, onlyAllowDefined, x, (int) (baseY + 392 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(onlyAllowDefined, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component allowEnchantedBook = Component.translatable("screen.uncrafteverything.config.output_enchanted_book");
        pGuiGraphics.drawWordWrap(this.font, allowEnchantedBook, x, (int) (baseY + 417 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedBook, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.disableScissor();

        // Draw title and scroll indicator outside scissor area
        pGuiGraphics.drawCenteredString(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);

        saveButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderButtonTooltip(pGuiGraphics, pMouseX, pMouseY);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        super.mouseScrolled(mouseX, mouseY, delta);
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
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
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
        ResourceLocation header = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        ResourceLocation footer = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        guiGraphics.blit(header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.blit(footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
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

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook);
        UEConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }

    private void renderButtonTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY){
        if (restrictionTypeButton.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_restriction_type"),
                    valueInfo("tooltip.uncrafteverything.config.blacklist", "tooltip.uncrafteverything.config.blacklist_info"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.whitelist", "tooltip.uncrafteverything.config.whitelist_info")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (restrictionsInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_items"),
                    description("tooltip.uncrafteverything.config.edit_restricted_items_description")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleEnchantedBtn.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_enchanted_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.allow_enchanted_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.allow_enchanted_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleEnchantmentTypeBtn.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_experience_type"),
                    valueInfo("tooltip.uncrafteverything.config.point", "tooltip.uncrafteverything.config.point_info"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.level", "tooltip.uncrafteverything.config.level_info")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (experienceInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_experience_required"),
                    description("tooltip.uncrafteverything.config.edit_experience_required_description")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleAllowUnsmithing.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_unsmithing"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleAllowDamaged.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_damaged_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (togglePreventModdedIngredientsFromVanillaItems.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (restrictedModInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_mods"),
                    description("tooltip.uncrafteverything.config.edit_restricted_mods_description")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleEnableProgression.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_enable_progression"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_enable_progression_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_enable_progression_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleOnlyAllowDefinedProgression.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression"),
                    description("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_description"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (toggleOutputEnchantedBook.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_output_enchanted_book"),
                    description("tooltip.uncrafteverything.config.toggle_output_enchanted_book_description"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_output_enchanted_book_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_output_enchanted_book_no")
            );
            guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private Component title(String title){
        return Component.translatable(title).withStyle(ChatFormatting.BLUE);
    }

    private Component valueInfo(String value, String info){
        return Component.translatable(value).append(": ").withStyle(ChatFormatting.AQUA).append(Component.translatable(info).withStyle(ChatFormatting.GRAY));
    }

    private Component description(String desc){
        return Component.translatable(desc).withStyle(ChatFormatting.GRAY);
    }
}
