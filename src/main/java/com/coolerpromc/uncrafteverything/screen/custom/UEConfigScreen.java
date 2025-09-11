package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private boolean enableProgression = config.enableProgression();
    private boolean onlyAllowDefinedProgression = config.onlyAllowDefinedProgression();

    private ButtonWidget restrictionTypeButton;
    private ButtonWidget toggleEnchantedBtn;
    private ButtonWidget toggleEnchantmentTypeBtn;
    private ButtonWidget toggleAllowUnsmithing;
    private ButtonWidget toggleAllowDamaged;
    private ButtonWidget toggleEnableProgression;
    private ButtonWidget toggleOnlyAllowDefinedProgression;
    private ButtonWidget togglePreventModdedIngredientsFromVanillaItems;
    private EditBoxWidget restrictionsInput;
    private TextFieldWidget experienceInput;
    private EditBoxWidget restrictedModInput;
    private ButtonWidget saveButton;

    protected UEConfigScreen(Text title, Screen parent) {
        super(title, 388);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        // Restriction Type Config
        restrictionTypeButton =  ButtonWidget.builder(Text.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton).dimensions(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = EditBoxWidget.builder().x(x).y((int) (baseY + 25 - scrollAmount)).build(this.textRenderer, widgetWidth, 90, Text.translatable("screen.uncrafteverything.blank"));
        restrictionsInput.setText(joined);
        this.addDrawableChild(restrictionsInput);

        // Toggle for allowEnchantedItems
       toggleEnchantedBtn = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
       }).dimensions(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnchantedBtn);

        // Toggle for experienceType
        toggleEnchantmentTypeBtn = ButtonWidget.builder(
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
        toggleAllowUnsmithing = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        }).dimensions(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowUnsmithing);

        toggleAllowDamaged = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        }).dimensions(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        togglePreventModdedIngredientsFromVanillaItems = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        }).dimensions(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(togglePreventModdedIngredientsFromVanillaItems);

        // Restricted Mod input box
        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = EditBoxWidget.builder().x(x).y((int) (baseY + 270 - scrollAmount)).build(textRenderer, widgetWidth, 90, Text.translatable("screen.uncrafteverything.blank"));
        restrictedModInput.setText(joinedMod);
        this.addDrawableChild(restrictedModInput);

        toggleEnableProgression = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)), btn -> {
            enableProgression = !enableProgression;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)));
        }).dimensions(x, (int) (baseY + 365 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleEnableProgression);

        toggleOnlyAllowDefinedProgression = ButtonWidget.builder(Text.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)), btn -> {
            onlyAllowDefinedProgression = !onlyAllowDefinedProgression;
            btn.setMessage(Text.translatable(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)));
        }).dimensions(x, (int) (baseY + 390 - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(toggleOnlyAllowDefinedProgression);

        // Save button (always at bottom)
        saveButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.save"), this::pressSaveButton).dimensions(this.width / 2 - 100, (this.height - 45) + 15, 200, 20).build();
        this.addDrawableChild(saveButton);
    }

    public void renderBackground(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDarkening(guiGraphics);
        applyBlur(guiGraphics);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
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
        int baseY = 30;

        pGuiGraphics.drawTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFFFF);

        Text format = Text.translatable("screen.uncrafteverything.config.restricted_item_label");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, format, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 20), textWidth, 0xFFFFFFFF);

        // Format help text
        pGuiGraphics.getMatrices().pushMatrix();
        pGuiGraphics.getMatrices().scale(0.65f, 0.65f);
        pGuiGraphics.getMatrices().translate(x * 1.55f, (float) (((baseY + 25f - scrollAmount) * 1.6f) + this.textRenderer.getWrappedLinesHeight(format, textWidth) * 2f - (this.textRenderer.fontHeight * 0.65f) + 40f));
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5), 0xFFAAAAAA);
        pGuiGraphics.getMatrices().popMatrix();

        Text allowEnchantedItem = Text.translatable("screen.uncrafteverything.config.allow_enchanted_label");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text expRequired = Text.translatable("screen.uncrafteverything.config.exp_required_label");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, expRequired, x, (int) (baseY + 170 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(expRequired, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text allowDamagedItem = Text.translatable("screen.uncrafteverything.config.allow_damaged_label");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, allowDamagedItem, x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(allowDamagedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text preventModded = Text.translatable("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, preventModded, x, (int) (baseY + 245 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(preventModded, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text restrictedMod = Text.translatable("screen.uncrafteverything.config.prevent_modid");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, restrictedMod, x, (int) (baseY + 304 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text enableProgression = Text.translatable("screen.uncrafteverything.config.enable_progression");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, enableProgression, x, (int) (baseY + 367 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        Text onlyAllowDefined = Text.translatable("screen.uncrafteverything.config.only_allow_defined_progression");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, onlyAllowDefined, x, (int) (baseY + 392 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(restrictedMod, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        pGuiGraphics.disableScissor();

        pGuiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);

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
    public boolean mouseClicked(Click click, boolean doubled) {
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

    protected void renderSeparator(DrawContext guiGraphics){
        Identifier header = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier footer = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
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

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression);
        ClientPlayNetworking.send(configPayload);
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }
    
    private void renderWrappedTooltip(DrawContext guiGraphics, List<Text> tooltip, int mouseX, int mouseY) {
        int maxWidth = mouseX - 20;
        List<OrderedText> wrappedTooltip = tooltip.stream()
                .flatMap(text -> {
                    if (text.getString().isEmpty()){
                        return Stream.of(OrderedText.empty());
                    }
                    return this.textRenderer.wrapLines(text, maxWidth).stream();
                })
                .collect(Collectors.toList());
        guiGraphics.drawOrderedTooltip(this.textRenderer, wrappedTooltip, mouseX, mouseY);
    }

    private void renderButtonTooltip(DrawContext guiGraphics, int mouseX, int mouseY){
        if (restrictionTypeButton.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_restriction_type"),
                    valueInfo("tooltip.uncrafteverything.config.blacklist", "tooltip.uncrafteverything.config.blacklist_info"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.whitelist", "tooltip.uncrafteverything.config.whitelist_info")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (restrictionsInput.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_items"),
                    description("tooltip.uncrafteverything.config.edit_restricted_items_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleEnchantedBtn.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_enchanted_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.allow_enchanted_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.allow_enchanted_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleEnchantmentTypeBtn.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_experience_type"),
                    valueInfo("tooltip.uncrafteverything.config.point", "tooltip.uncrafteverything.config.point_info"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.level", "tooltip.uncrafteverything.config.level_info")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (experienceInput.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_experience_required"),
                    description("tooltip.uncrafteverything.config.edit_experience_required_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleAllowUnsmithing.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_unsmithing"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleAllowDamaged.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_allow_damaged_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (togglePreventModdedIngredientsFromVanillaItems.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (restrictedModInput.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.edit_restricted_mods"),
                    description("tooltip.uncrafteverything.config.edit_restricted_mods_description")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleEnableProgression.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_enable_progression"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_enable_progression_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_enable_progression_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }

        if (toggleOnlyAllowDefinedProgression.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression"),
                    description("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_description"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_yes"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_no")
            );
            renderWrappedTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }
    }

    private Text title(String title){
        return Text.translatable(title).formatted(Formatting.BLUE);
    }

    private Text valueInfo(String value, String info){
        return Text.translatable(value).append(": ").formatted(Formatting.AQUA).append(Text.translatable(info).formatted(Formatting.GRAY));
    }

    private Text description(String desc){
        return Text.translatable(desc).formatted(Formatting.GRAY);
    }
}