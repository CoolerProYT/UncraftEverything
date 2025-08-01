package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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

    private MultiLineEditBox restrictionsInput;
    private EditBox experienceInput;
    private Button saveButton;

    protected UEConfigScreen(Component title, Screen parent) {
        super(title, 245);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        Button restrictionTypeButton = new Button(
                x, (int) (baseY - scrollAmount), widgetWidth, 20,
                Component.translatable("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()),
                this::pressRestrictionTypeButton
        );
        this.addRenderableWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.font, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addRenderableWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        Button toggleEnchantedBtn = new Button(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20,
                Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
                allowEnchantedItems = !allowEnchantedItems;
                btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        });
        this.addRenderableWidget(toggleEnchantedBtn);

        // Toggle for experienceType
        Button toggleEnchantmentTypeBtn = new Button(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20,
                Component.translatable("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()),
                btn -> {
                    UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
                    UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
                    experienceType = next;
                    btn.setMessage(Component.translatable("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
                }
        );
        this.addRenderableWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new EditBox(this.font, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, Component.translatable("screen.uncrafteverything.blank"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addRenderableWidget(experienceInput);

        // Toggle for allowUnsmithing
        Button toggleAllowUnsmithing = new Button(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20, Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        });
        this.addRenderableWidget(toggleAllowUnsmithing);

        Button toggleAllowDamaged = new Button(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20, Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
                allowDamagedItems = !allowDamagedItems;
            btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        });
        this.addRenderableWidget(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        Button togglePreventModdedIngredientsFromVanillaItems = new Button(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20,
                Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
                preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
                btn.setMessage(Component.translatable(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        });
        this.addRenderableWidget(togglePreventModdedIngredientsFromVanillaItems);

        // Save button
        saveButton = new Button(this.width / 2 - 100, this.height - 23, 200, 20, Component.translatable("screen.uncrafteverything.save"), this::pressSaveButton);
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void renderBackground(@NotNull PoseStack pPoseStack) {
        fillGradient(pPoseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, pPoseStack));
        renderSeparator(pPoseStack);
        renderScrollbar(pPoseStack,45);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);

        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        enableScissor(0, scissorTop, this.width, scissorBottom);

        for(Widget widget : this.renderables) {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        this.restrictionsInput.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        font.draw(pPoseStack, Component.translatable("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        Component format = Component.translatable("screen.uncrafteverything.config.restricted_item_label");
        font.drawWordWrap(format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        List<FormattedCharSequence> formattedCharSequence = this.font.split(Component.translatable("screen.uncrafteverything.config.format_label"), (int) (textWidth * 1.5));
        int lineHeight = 0;

        for (FormattedCharSequence line : formattedCharSequence){
            pPoseStack.pushPose();
            pPoseStack.scale(0.65f, 0.65f, 1.0f);
            pPoseStack.translate(x * 1.55, ((baseY + 25 + lineHeight - scrollAmount) * 1.6) + this.font.wordWrapHeight(format.getString(), textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 1);
            drawString(pPoseStack, font, line, 0, 0,0xFFAAAAAA);
            pPoseStack.popPose();
            lineHeight += (int) (this.font.lineHeight * 0.65);
        }

        Component allowEnchantedItem = Component.translatable("screen.uncrafteverything.config.allow_enchanted_label");
        font.drawWordWrap(allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem, textWidth) / 4d), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(Component.translatable("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth,0xFFFFFFFF);

        Component expRequired = Component.translatable("screen.uncrafteverything.config.exp_required_label");
        font.drawWordWrap(expRequired, x, (int) (baseY + 170 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired, textWidth) / 4d), textWidth,0xFFFFFFFF);

        font.drawWordWrap(Component.translatable("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(Component.translatable("screen.uncrafteverything.config.allow_damaged_label"), x, (int) (baseY + 220 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(Component.translatable("screen.uncrafteverything.config.allow_damaged_label"), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(Component.translatable("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label"), x, (int) (baseY + 245 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label", textWidth) / 4d), textWidth, 0xFFFFFFFF);

        disableScissor();

        // Draw title and scroll indicator outside scissor area
        drawCenteredString(pPoseStack, this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFFFF);

        saveButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
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

    protected void renderSeparator(PoseStack pPoseStack){
        ResourceLocation header = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        ResourceLocation footer = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        RenderSystem.setShaderTexture(0, header);
        blit(pPoseStack, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.setShaderTexture(0, footer);
        blit(pPoseStack, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
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
        restrictions = Arrays.stream(restrictionsInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        experience = Integer.parseInt(experienceInput.getValue());

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems);
            UEConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
            RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }
}
