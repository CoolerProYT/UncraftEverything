package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private ButtonWidget restrictionTypeButton;
    private ButtonWidget toggleEnchantedBtn;
    private ButtonWidget toggleEnchantmentTypeBtn;
    private ButtonWidget toggleAllowUnsmithing;
    private ButtonWidget toggleAllowDamaged;
    private ButtonWidget togglePreventModdedIngredientsFromVanillaItems;

    private MultiLineEditBox restrictionsInput;
    private TextFieldWidget experienceInput;
    private ButtonWidget saveButton;

    protected UEConfigScreen(Text title, Screen parent) {
        super(title, 245);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;
        int baseY = 30; // Start position, accounting for title and scroll

        // Restriction Type Config
        restrictionTypeButton = new ButtonWidget(x, (int) (baseY - scrollAmount), widgetWidth, 20, new TranslatableText("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), this::pressRestrictionTypeButton);
        this.addChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.textRenderer, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new ButtonWidget(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20, new TranslatableText(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new TranslatableText(getLabel("screen.uncrafteverything.config.allow_enchanted_", allowEnchantedItems)));
        });
        this.addChild(toggleEnchantedBtn);

        // Toggle for enchantmentType
        toggleEnchantmentTypeBtn = new ButtonWidget(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20, new TranslatableText("screen.uncrafteverything.config.exp_type_" + experienceType.toString().toLowerCase()), btn -> {
            UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
            UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
            experienceType = next;
            btn.setMessage(new TranslatableText("screen.uncrafteverything.config.exp_type_" + next.toString().toLowerCase()));
        });
        this.addChild(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.textRenderer, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, new TranslatableText("screen.uncrafteverything.blank"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addChild(experienceInput);

        // Toggle for allowUnsmithing
        toggleAllowUnsmithing = new ButtonWidget(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20, new TranslatableText(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new TranslatableText(getLabel("screen.uncrafteverything.config.allow_unsmithing_", allowUnsmithing)));
        });
        this.addChild(toggleAllowUnsmithing);

        toggleAllowDamaged = new ButtonWidget(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20, new TranslatableText(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(new TranslatableText(getLabel("screen.uncrafteverything.config.allow_damaged_", allowDamagedItems)));
        });
        this.addChild(toggleAllowDamaged);

        // Toggle for preventModdedIngredientsFromVanillaItems
        togglePreventModdedIngredientsFromVanillaItems = new ButtonWidget(x, (int) (baseY + 245 - scrollAmount), widgetWidth, 20, new TranslatableText(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)), btn -> {
            preventModdedIngredientsFromVanillaItems = !preventModdedIngredientsFromVanillaItems;
            btn.setMessage(new TranslatableText(getLabel("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_", preventModdedIngredientsFromVanillaItems)));
        });
        this.addChild(togglePreventModdedIngredientsFromVanillaItems);

        // Save button (always at bottom)
        saveButton = new ButtonWidget(this.width / 2 - 100, (this.height - 45) + 15, 200, 20, new TranslatableText("screen.uncrafteverything.save"), this::pressSaveButton);
        this.addChild(saveButton);
    }

    public void renderBackground(MatrixStack guiGraphics) {
        fillGradient(guiGraphics,0, 0, this.width, this.height, -1072689136, -804253680);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 45);
    }

    @Override
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        int scale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        int clipTop = 45;
        int clipBottom = this.height - 25;
        int clipHeight = clipBottom - clipTop;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, clipTop * scale, this.width * scale, clipHeight * scale);

        for(Drawable drawable : this.buttons) {
            drawable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        this.restrictionsInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.experienceInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.restrictionTypeButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantedBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantmentTypeBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowUnsmithing.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowDamaged.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.togglePreventModdedIngredientsFromVanillaItems.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        textRenderer.drawWithShadow(pGuiGraphics, new TranslatableText("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFFFF);

        TranslatableText format = new TranslatableText("screen.uncrafteverything.config.restricted_item_label");
        textRenderer.drawTrimmed(format, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 20), textWidth, 0xFFFFFFFF);

        // Format help text
        pGuiGraphics.push();
        pGuiGraphics.scale(0.65f, 0.65f, 0);
        pGuiGraphics.translate(x * 1.55f, (float) (((baseY + 25f - scrollAmount) * 1.6f) + this.textRenderer.getStringBoundedHeight(format.getString(), textWidth) * 2f - (this.textRenderer.fontHeight * 0.65f) + 40f), 0);
        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5), 0xFFAAAAAA);
        pGuiGraphics.pop();

        TranslatableText allowEnchantedItem = new TranslatableText("screen.uncrafteverything.config.allow_enchanted_label");
        this.textRenderer.drawTrimmed(allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(allowEnchantedItem.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        TranslatableText expRequired = new TranslatableText("screen.uncrafteverything.config.exp_required_label");
        this.textRenderer.drawTrimmed(expRequired, x, (int) (baseY + 170 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(expRequired.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        TranslatableText allowDamagedItem = new TranslatableText("screen.uncrafteverything.config.allow_damaged_label");
        this.textRenderer.drawTrimmed(allowDamagedItem, x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(allowDamagedItem.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        TranslatableText preventModded = new TranslatableText("screen.uncrafteverything.config.prevent_modded_ingredients_from_vanilla_items_label");
        this.textRenderer.drawTrimmed(preventModded, x, (int) (baseY + 245 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(preventModded.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawCenteredText(pGuiGraphics, this.textRenderer, new TranslatableText("screen.uncrafteverything.uncraft_everything_config"), this.width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);

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
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
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

    protected void renderSeparator(MatrixStack guiGraphics){
        Identifier header = new Identifier(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        Identifier footer = new Identifier(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        this.client.getTextureManager().bindTexture(header);
        drawTexture(guiGraphics, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.client.getTextureManager().bindTexture(footer);
        drawTexture(guiGraphics, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private String getLabel(String label, boolean enabled) {
        return label + (enabled ? "yes" : "no");
    }

    private void pressRestrictionTypeButton(ButtonWidget button){
        UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
        UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
        restrictionType = next;
        button.setMessage(new TranslatableText("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
    }

    private void pressSaveButton(ButtonWidget button){
        restrictions = Arrays.stream(restrictionsInput.getText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        experience = Integer.parseInt(experienceInput.getText());

        UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictions, allowEnchantedItems, experienceType, experience, allowUnsmithing, allowDamagedItems, preventModdedIngredientsFromVanillaItems);
        ClientPlayNetworking.send(UEConfigPayload.TYPE, UEConfigPayload.encode(PacketByteBufs.create(), configPayload));
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
    }
}