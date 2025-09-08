package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
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

    private Button restrictionTypeButton;
    private Button toggleEnchantedBtn;
    private Button toggleEnchantmentTypeBtn;
    private Button toggleAllowUnsmithing;
    private Button toggleAllowDamaged;
    private Button toggleEnableProgression;
    private Button toggleOnlyAllowDefinedProgression;
    private Button togglePreventModdedIngredientsFromVanillaItems;
    private MultiLineEditBox restrictionsInput;
    private TextFieldWidget experienceInput;
    private MultiLineEditBox restrictedModInput;
    private Button saveButton;

    protected UEConfigScreen(ITextComponent title, Screen parent) {
        super(title, 388);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        restrictionTypeButton = new Button(
                x, (int) (baseY - scrollAmount), widgetWidth, 20,
                new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()),
                this::pressRestrictionTypeButton
        );
        this.addWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.font, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 90, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new Button(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20,
                new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
                allowEnchantedItems = !allowEnchantedItems;
                btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        });
        this.addWidget(toggleEnchantedBtn);

        // Toggle for enchantmentType
        toggleEnchantmentTypeBtn = new Button(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20, new TranslationTextComponent("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()), btn -> {
            UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
            UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
            experienceType = next;
            btn.setMessage(new TranslationTextComponent("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
        });
        this.addWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.font, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, new TranslationTextComponent("screen.uncrafteverything.blank"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addWidget(experienceInput);

        // Toggle for allowUnsmithing
        toggleAllowUnsmithing = new Button(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        });
        this.addWidget(toggleAllowUnsmithing);

        toggleAllowDamaged = new Button(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
                allowDamagedItems = !allowDamagedItems;
            btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        });
        this.addWidget(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        togglePreventModdedIngredientsFromVanillaItems = new Button(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20,
                new TranslationTextComponent(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
                preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
                btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        });
        this.addWidget(togglePreventModdedIngredientsFromVanillaItems);

        // Restricted Mod input box
        String joinedMod = String.join("\n", restrictedModIngredients);
        restrictedModInput = new MultiLineEditBox(font, x, (int) (baseY + 270 - scrollAmount), widgetWidth, 90, Integer.MAX_VALUE);
        restrictedModInput.setText(joinedMod);
        this.addWidget(restrictedModInput);

        toggleEnableProgression = new Button(x, (int) (baseY + 365 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)), btn -> {
            enableProgression = !enableProgression;
            btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.enable_progression_", enableProgression)));
        });
        this.addWidget(toggleEnableProgression);

        toggleOnlyAllowDefinedProgression = new Button(x, (int) (baseY + 390 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)), btn -> {
            onlyAllowDefinedProgression = !onlyAllowDefinedProgression;
            btn.setMessage(new TranslationTextComponent(getLabel("screen.uncrafteverything.config.only_allow_defined_progression_", onlyAllowDefinedProgression)));
        });
        this.addWidget(toggleOnlyAllowDefinedProgression);

        // Save button
        saveButton = new Button(this.width / 2 - 100, this.height - 23, 200, 20, new TranslationTextComponent("screen.uncrafteverything.save"), this::pressSaveButton);
        this.addWidget(saveButton);
    }

    @Override
    public void renderBackground(MatrixStack pPoseStack) {
        fillGradient(pPoseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this, pPoseStack));
        renderSeparator(pPoseStack);
        renderScrollbar(pPoseStack,45);
    }

    @Override
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);

        // Enable scissor test to clip content outside the scrollable area
        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int clipTop = 45;
        int clipBottom = this.height - 25;
        int clipHeight = clipBottom - clipTop;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, clipTop * scale, this.width * scale, clipHeight * scale);

        this.restrictionsInput.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.experienceInput.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        this.restrictionTypeButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantedBtn.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantmentTypeBtn.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowUnsmithing.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowDamaged.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.togglePreventModdedIngredientsFromVanillaItems.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.restrictedModInput.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleEnableProgression.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.toggleOnlyAllowDefinedProgression.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        for(Widget widget : this.buttons) {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        drawString(pPoseStack, this.font, new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        TranslationTextComponent format = new TranslationTextComponent("screen.uncrafteverything.config.restricted_item_label");
        font.drawWordWrap(format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.format_label"), x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 40), (int) (textWidth),0xFFAAAAAA);

        TranslationTextComponent allowEnchantedItem = new TranslationTextComponent("screen.uncrafteverything.config.allow_enchanted_label");
        font.drawWordWrap(allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth,0xFFFFFFFF);

        TranslationTextComponent expRequired = new TranslationTextComponent("screen.uncrafteverything.config.exp_required_label");
        font.drawWordWrap(expRequired, x, (int) (baseY + 170 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired.getString(), textWidth) / 4d), textWidth,0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.allow_damaged_label"), x, (int) (baseY + 220 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(new TranslationTextComponent("screen.uncrafteverything.config.allow_damaged_label").getString(), textWidth) / 4d), textWidth, 0xFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label"), x, (int) (baseY + 245 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label", textWidth) / 4d), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label"), x, (int) (baseY + 245 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label", textWidth) / 4d), textWidth, 0xFFFFFFFF);

        TranslationTextComponent restrictedMod = new TranslationTextComponent("screen.uncrafteverything.config.prevent_modid");
        font.drawWordWrap(restrictedMod, x, (int) (baseY + 304 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictedMod.getContents(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        TranslationTextComponent enableProgression = new TranslationTextComponent("screen.uncrafteverything.config.enable_progression");
        font.drawWordWrap(enableProgression, x, (int) (baseY + 367 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictedMod.getContents(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        TranslationTextComponent onlyAllowDefined = new TranslationTextComponent("screen.uncrafteverything.config.only_allow_defined_progression");
        font.drawWordWrap(onlyAllowDefined, x, (int) (baseY + 392 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(restrictedMod.getContents(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw title and scroll indicator outside scissor area
        drawCenteredString(pPoseStack, this.font, new TranslationTextComponent("screen.uncrafteverything.uncraft_everything_config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFF);

        saveButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        renderButtonTooltip(pPoseStack, pMouseX, pMouseY);
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
        this.children.clear();
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

    protected void renderSeparator(MatrixStack pPoseStack){
        ResourceLocation header = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        ResourceLocation footer = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        this.getMinecraft().getTextureManager().bind(header);
        blit(pPoseStack, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.getMinecraft().getTextureManager().bind(footer);
        blit(pPoseStack, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private String getLabel(String label, boolean enabled) {
        return label + (enabled ? "yes" : "no");
    }

    private void pressRestrictionTypeButton(Button button){
        UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
        UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
        restrictionType = next;
        button.setMessage(new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
    }

    private void pressSaveButton(Button button){
        restrictions = Arrays.stream(restrictionsInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        experience = Integer.parseInt(experienceInput.getValue());
        restrictedModIngredients = Arrays.stream(restrictedModInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression);
        UEConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }

    private void renderButtonTooltip(MatrixStack poseStack, int mouseX, int mouseY){
        if (restrictionTypeButton.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_restriction_type"),
                    valueInfo("tooltip.uncrafteverything.config.blacklist", "tooltip.uncrafteverything.config.blacklist_info"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.whitelist", "tooltip.uncrafteverything.config.whitelist_info")
            );
            
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (restrictionsInput.isMouseOver(mouseX, mouseY)){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.edit_restricted_items"),
                    description("tooltip.uncrafteverything.config.edit_restricted_items_description")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleEnchantedBtn.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_allow_enchanted_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.allow_enchanted_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.allow_enchanted_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleEnchantmentTypeBtn.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_experience_type"),
                    valueInfo("tooltip.uncrafteverything.config.point", "tooltip.uncrafteverything.config.point_info"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.level", "tooltip.uncrafteverything.config.level_info")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (experienceInput.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.edit_experience_required"),
                    description("tooltip.uncrafteverything.config.edit_experience_required_description")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleAllowUnsmithing.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_allow_unsmithing"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_unsmithing_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleAllowDamaged.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_allow_damaged_items"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_allow_damaged_items_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (togglePreventModdedIngredientsFromVanillaItems.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_prevent_modded_ingredients_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (restrictedModInput.isMouseOver(mouseX, mouseY)){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.edit_restricted_mods"),
                    description("tooltip.uncrafteverything.config.edit_restricted_mods_description")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleEnableProgression.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_enable_progression"),
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_enable_progression_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_enable_progression_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }

        if (toggleOnlyAllowDefinedProgression.isHovered()){
            List<ITextProperties> tooltip = Arrays.asList(
                    title("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression"),
                    description("tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_description"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.yes", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_yes"),
                    TextComponent.EMPTY,
                    valueInfo("tooltip.uncrafteverything.config.no", "tooltip.uncrafteverything.config.toggle_only_allow_defined_progression_no")
            );
            renderWrappedToolTip(poseStack, tooltip, mouseX, mouseY, font);
        }
    }

    private ITextComponent title(String title){
        return new TranslationTextComponent(title).withStyle(TextFormatting.BLUE);
    }

    private ITextComponent valueInfo(String value, String info){
        return new TranslationTextComponent(value).append(": ").withStyle(TextFormatting.AQUA).append(new TranslationTextComponent(info).withStyle(TextFormatting.GRAY));
    }

    private ITextComponent description(String desc){
        return new TranslationTextComponent(desc).withStyle(TextFormatting.GRAY);
    }
}
