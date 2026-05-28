package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientBoundResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ServerBoundRequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ServerBoundUEConfigPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UEConfigScreen extends AbstractScrollableScreen {
    private final ClientBoundResponseConfigPayload config = UncraftEverythingClient.payloadFromServer;

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
    private boolean prioritizeVanillaIngredientRecipe = config.prioritizeVanillaIngredientRecipe();
    private boolean restrictAmbiguouslyCraftedItems = config.restrictAmbiguouslyCraftedItems();
    private boolean allowDamagedNonRepairable = config.allowDamagedNonRepairable();
    private double minimumDurability = config.minimumDurability();

    private Button restrictionTypeButton;
    private Button toggleEnchantedBtn;
    private Button toggleExpTypeBtn;
    private Button toggleRestrictAmbiguouslyCraftedItems;
    private Button toggleAllowUnsmithing;
    private Button toggleAllowDamaged;
    private Button toggleEnableProgression;
    private Button toggleOutputEnchantedBook;
    private Button toggleOnlyAllowDefinedProgression;
    private Button togglePreventModdedIngredientsFromVanillaItems;
    private Button togglePrioritizeVanillaIngredientRecipe;
    private Button toggleAllowDamagedNonRepairable;
    private MultiLineEditBox restrictionsInput;
    private EditBox experienceInput;
    private MultiLineEditBox restrictedModInput;
    private EditBox minimumDurabilityInput;
    private Button saveButton;

    public UEConfigScreen(Component title) {
        super(title, 713);
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int y = 55;

        // Restrictions
        restrictionTypeButton = Button.builder(Component.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(restrictionTypeButton);
        y += 25;

        String joined = String.join("\n", restrictions);
        restrictionsInput = MultiLineEditBox.builder().setX(x).setY((int) (y - scrollAmount)).build(this.font, widgetWidth, 90, Component.translatable("screen.uncrafteverything.blank"));
        restrictionsInput.setValue(joined);
        this.addRenderableWidget(restrictionsInput);
        y += 95;

        toggleRestrictAmbiguouslyCraftedItems = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.restrict_ambiguously_", restrictAmbiguouslyCraftedItems)), btn -> {
            restrictAmbiguouslyCraftedItems = !restrictAmbiguouslyCraftedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.restrict_ambiguously_", restrictAmbiguouslyCraftedItems)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleRestrictAmbiguouslyCraftedItems);
        y += 50;

        // Experience
        toggleExpTypeBtn = Button.builder(
                Component.translatable("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Component.translatable("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
                }
        ).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleExpTypeBtn);
        y += 25;

        experienceInput = new EditBox(this.font, x, (int) (y - scrollAmount), widgetWidth, 20, Component.translatable("screen.uncrafteverything.blank"));
        experienceInput.setValue(Integer.toString(experience));
        this.addRenderableWidget(experienceInput);
        y += 50;

        // UnSmithing
        toggleAllowUnsmithing = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowUnsmithing);
        y += 50;

        // Damaged
        toggleAllowDamaged = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowDamaged);
        y += 25;

        toggleAllowDamagedNonRepairable = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_non_repairable_", allowDamagedNonRepairable)), btn -> {
            allowDamagedNonRepairable = !allowDamagedNonRepairable;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_non_repairable_", allowDamagedNonRepairable)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleAllowDamagedNonRepairable);
        y += 25;

        minimumDurabilityInput = new EditBox(this.font, x, (int) (y - scrollAmount), widgetWidth, 20, Component.empty());
        minimumDurabilityInput.setValue(Double.toString(minimumDurability));
        this.addRenderableWidget(minimumDurabilityInput);
        y += 50;

        // Modded Ingredients
        togglePreventModdedIngredientsFromVanillaItems = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(togglePreventModdedIngredientsFromVanillaItems);
        y += 25;

        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = MultiLineEditBox.builder().setX(x).setY((int) (y - scrollAmount)).build(font, widgetWidth, 90, Component.translatable("screen.uncrafteverything.blank"));
        restrictedModInput.setValue(joinedMod);
        this.addRenderableWidget(restrictedModInput);
        y += 120;

        // Enchanted
        toggleOutputEnchantedBook = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.output_enchanted_book_", outputEnchantedBook)), btn -> {
            outputEnchantedBook = !outputEnchantedBook;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.output_enchanted_book_", outputEnchantedBook)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleOutputEnchantedBook);
        y += 25;

        toggleEnchantedBtn = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnchantedBtn);
        y += 50;

        // RecipeSelectionOrder
        togglePrioritizeVanillaIngredientRecipe = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.prioritize_", prioritizeVanillaIngredientRecipe)), btn -> {
            prioritizeVanillaIngredientRecipe = !prioritizeVanillaIngredientRecipe;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.prioritize_", prioritizeVanillaIngredientRecipe)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(togglePrioritizeVanillaIngredientRecipe);
        y += 50;

        // FTB Quest Progression
        toggleEnableProgression = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)), btn -> {
            enableProgression = !enableProgression;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleEnableProgression);
        y += 25;

        toggleOnlyAllowDefinedProgression = Button.builder(Component.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)), btn -> {
            onlyAllowDefinedProgression = !onlyAllowDefinedProgression;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)));
        }).bounds(x, (int) (y - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(toggleOnlyAllowDefinedProgression);

        saveButton = Button.builder(Component.translatable("screen.uncrafteverything.save"), this::pressSaveButton).bounds(this.width / 2 - 100, (this.height - 45) + 15, 200, 20).build();
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        renderSeparator(graphics);
        renderScrollbar(graphics, 70);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Render labels with scroll offset
        int x = 10;
        int textWidth = this.width / 2 - 10;

        pGuiGraphics.text(this.font, Component.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (restrictionTypeButton.getY() + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        Component format = Component.translatable("screen.uncrafteverything.config.restricted_item_label");
        pGuiGraphics.textWithWordWrap(this.font, format, x, (int) (restrictionsInput.getY() + (this.font.lineHeight / 2d) + 20), textWidth, 0xFFFFFFFF);

        // Format help text
        pGuiGraphics.pose().pushMatrix();
        pGuiGraphics.pose().scale(0.65f, 0.65f);
        pGuiGraphics.pose().translate(x * 1.55f, (float) ((restrictionsInput.getY() * 1.6f) + this.font.wordWrapHeight(format, textWidth) * 2f - (this.font.lineHeight * 0.65f) + 40f));
        pGuiGraphics.textWithWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5), 0xFFAAAAAA);
        pGuiGraphics.pose().popMatrix();

        Component restrictAmbiguouslyCraftedItems = Component.translatable("screen.uncrafteverything.config.restrict_mbiguously_label");
        pGuiGraphics.textWithWordWrap(this.font, restrictAmbiguouslyCraftedItems, x, (int) (toggleRestrictAmbiguouslyCraftedItems.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictAmbiguouslyCraftedItems, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component allowEnchantedItem = Component.translatable("screen.uncrafteverything.config.allow_enchanted_label");
        pGuiGraphics.textWithWordWrap(this.font, allowEnchantedItem, x, (int) (toggleEnchantedBtn.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.textWithWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.exp_type_label"), x, (int) (toggleExpTypeBtn.getY() + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component expRequired = Component.translatable("screen.uncrafteverything.config.exp_required_label");
        pGuiGraphics.textWithWordWrap(this.font, expRequired, x, (int) (experienceInput.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.textWithWordWrap(this.font, Component.translatable("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (toggleAllowUnsmithing.getY() + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component allowDamagedItem = Component.translatable("screen.uncrafteverything.config.allow_damaged_label");
        pGuiGraphics.textWithWordWrap(this.font, allowDamagedItem, x, (int) (toggleAllowDamaged.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component allowDamagedNonRepairableItem = Component.translatable("screen.uncrafteverything.config.allow_damaged_non_repairable_label");
        pGuiGraphics.textWithWordWrap(this.font, allowDamagedNonRepairableItem, x, (int) (toggleAllowDamagedNonRepairable.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component minimumDurability = Component.translatable("screen.uncrafteverything.config.minimum_durability_label");
        pGuiGraphics.textWithWordWrap(this.font, minimumDurability, x, (int) (minimumDurabilityInput.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component preventModded = Component.translatable("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label");
        pGuiGraphics.textWithWordWrap(this.font, preventModded, x, (int) (togglePreventModdedIngredientsFromVanillaItems.getY() + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(preventModded, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component restrictedMod = Component.translatable("screen.uncrafteverything.config.prevent_modid");
        pGuiGraphics.textWithWordWrap(this.font, restrictedMod, x, (int) (restrictedModInput.getY() + 34 + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component enableProgression = Component.translatable("screen.uncrafteverything.config.enable_progression");
        pGuiGraphics.textWithWordWrap(this.font, enableProgression, x, (int) (toggleEnableProgression.getY() + 2 + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(enableProgression, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component onlyAllowDefined = Component.translatable("screen.uncrafteverything.config.only_allow_defined_progression");
        pGuiGraphics.textWithWordWrap(this.font, onlyAllowDefined, x, (int) (toggleOnlyAllowDefinedProgression.getY() + 2 + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(onlyAllowDefined, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component allowEnchantedBook = Component.translatable("screen.uncrafteverything.config.output_enchanted_book");
        pGuiGraphics.textWithWordWrap(this.font, allowEnchantedBook, x, (int) (toggleOutputEnchantedBook.getY() + 3 + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedBook, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component prioritizeVanillaIngredientRecipe = Component.translatable("screen.uncrafteverything.config.prioritize");
        pGuiGraphics.textWithWordWrap(this.font, prioritizeVanillaIngredientRecipe, x, (int) (togglePrioritizeVanillaIngredientRecipe.getY() + 3 + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(prioritizeVanillaIngredientRecipe, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Component restrictions = Component.translatable("screen.uncrafteverything.config.restrictions").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component experience = Component.translatable("screen.uncrafteverything.config.experience").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component unSmithing = Component.translatable("screen.uncrafteverything.config.unsmithing").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component damaged = Component.translatable("screen.uncrafteverything.config.damaged").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component moddedIngredients = Component.translatable("screen.uncrafteverything.config.modded_ingredients").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component enchanted = Component.translatable("screen.uncrafteverything.config.enchanted").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component recipeSelectionOrder = Component.translatable("screen.uncrafteverything.config.recipe_selection_order").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        Component ftbQuestProgression = Component.translatable("screen.uncrafteverything.config.ftb_quest_progression").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);

        pGuiGraphics.centeredText(this.font, restrictions, this.width / 2, restrictionTypeButton.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, experience, this.width / 2, toggleExpTypeBtn.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, unSmithing, this.width / 2, toggleAllowUnsmithing.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, damaged, this.width / 2, toggleAllowDamaged.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, moddedIngredients, this.width / 2, togglePreventModdedIngredientsFromVanillaItems.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, enchanted, this.width / 2, toggleOutputEnchantedBook.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, recipeSelectionOrder, this.width / 2, togglePrioritizeVanillaIngredientRecipe.getY() - 25 + (font.lineHeight / 2), -1);
        pGuiGraphics.centeredText(this.font, ftbQuestProgression, this.width / 2, toggleEnableProgression.getY() - 25 + (font.lineHeight / 2), -1);

        pGuiGraphics.disableScissor();

        pGuiGraphics.centeredText(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);

        saveButton.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
        Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
        super.onClose();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int scrollTop = 25;
        int scrollBottom = this.height - 45;

        boolean inScrollArea = click.y() >= scrollTop && click.y() <= scrollBottom;

        if (!inScrollArea) {
            if (!saveButton.isMouseOver(click.x(), click.y())) {
                return false;
            }
            else{
                return saveButton.mouseClicked(click, doubled);
            }
        }

        return super.mouseClicked(click, doubled);
    }

    protected void renderSeparator(GuiGraphicsExtractor guiGraphics){
        Identifier header = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        Identifier footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
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
        minimumDurability = Double.parseDouble(minimumDurabilityInput.getValue());

        ServerBoundUEConfigPayload configPayload = new ServerBoundUEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook, prioritizeVanillaIngredientRecipe, restrictAmbiguouslyCraftedItems, allowDamagedNonRepairable, minimumDurability);
        Services.NETWORK.sendToServer(configPayload);
        Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
        onClose();
    }
    
    private void renderWrappedTooltip(GuiGraphicsExtractor guiGraphics, List<Component> tooltip, int mouseX, int mouseY) {
        int maxWidth = mouseX - 20;
        List<FormattedCharSequence> wrappedTooltip = tooltip.stream()
                .flatMap(text -> {
                    if (text.getString().isEmpty()){
                        return Stream.of(FormattedCharSequence.composite());
                    }
                    return this.font.split(text, maxWidth).stream();
                })
                .collect(Collectors.toList());
        guiGraphics.setTooltipForNextFrame(this.font, wrappedTooltip, mouseX, mouseY);
    }

    private void renderButtonTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY){
        if (restrictionTypeButton.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_restriction_type"),
                    valueInfo("tooltip.uncrafteverything.config.blacklist", "tooltip.uncrafteverything.config.blacklist_info"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.whitelist", "tooltip.uncrafteverything.config.whitelist_info")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (restrictionsInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_items"),
                    description("tooltip.uncrafteverything.config.edit_restricted_items_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleRestrictAmbiguouslyCraftedItems.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_restrict_ambiguously"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.restrict_ambiguously_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.restrict_ambiguously_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleEnchantedBtn.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_enchanted_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.allow_enchanted_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.allow_enchanted_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleExpTypeBtn.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_experience_type"),
                    valueInfo("tooltip.uncrafteverything.config.point", "tooltip.uncrafteverything.config.point_info"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.level", "tooltip.uncrafteverything.config.level_info")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (experienceInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_experience_required"),
                    description("tooltip.uncrafteverything.config.edit_experience_required_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleAllowUnsmithing.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_unsmithing"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleAllowDamaged.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_damaged_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (togglePreventModdedIngredientsFromVanillaItems.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (restrictedModInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_mods"),
                    description("tooltip.uncrafteverything.config.edit_restricted_mods_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleEnableProgression.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_enable_progression"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_enable_progression_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_enable_progression_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
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
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
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
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (togglePrioritizeVanillaIngredientRecipe.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_prioritize"),
                    description("tooltip.uncrafteverything.config.toggle_prioritize_description"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_prioritize_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_prioritize_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleAllowDamagedNonRepairable.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_damaged_non_repairable_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_damaged_non_repairable_items_yes"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_damaged_non_repairable_items_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (minimumDurabilityInput.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.minimum_durability"),
                    description("tooltip.uncrafteverything.config.minimum_durability_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
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