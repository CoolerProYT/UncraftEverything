package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private ButtonWidget restrictionTypeButton;
    private ButtonWidget toggleEnchantedBtn;
    private ButtonWidget toggleEnchantmentTypeBtn;
    private ButtonWidget toggleAllowUnsmithing;
    private ButtonWidget doneButton;

    private MultiLineEditBox restrictionsInput;
    private TextFieldWidget experienceInput;

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
        restrictionTypeButton = new ButtonWidget(x, (int) (baseY - scrollAmount), widgetWidth, 20, new LiteralText("Restriction Type: " + restrictionType), btn -> {
            UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
            UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
            restrictionType = next;
            btn.setMessage(new LiteralText("Restriction Type: " + next));
        });

        this.addChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.textRenderer, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new ButtonWidget(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20, new LiteralText(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new LiteralText(getLabel(allowEnchantedItems)));
        });
        this.addChild(toggleEnchantedBtn);

        // Toggle for enchantmentType
        toggleEnchantmentTypeBtn = new ButtonWidget(x, (int) (baseY + 145 - scrollAmount), widgetWidth, 20, new LiteralText("Experience Type: " + experienceType), btn -> {
            UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
            UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
            experienceType = next;
            btn.setMessage(new LiteralText("Experience Type: " + next));
        });
        this.addChild(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.textRenderer, x, (int) (baseY + 170 - scrollAmount), widgetWidth, 20, new LiteralText("Experience Input"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addChild(experienceInput);

        // Toggle for allowUnsmithing
        toggleAllowUnsmithing = new ButtonWidget(x, 208, widgetWidth, 20, new LiteralText(getUnsmithingLabel(allowUnsmithing)), btn -> {
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
                    .collect(Collectors.toList());

            int expValue = this.experience;
            try {
                expValue = Integer.parseInt(experienceInput.getText());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid experience value, using default: " + this.experience);
            }

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing, allowDamagedItems);
            ClientPlayNetworking.send(UEConfigPayload.TYPE, UEConfigPayload.encode(PacketByteBufs.create(), configPayload));
            ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 23, 200, 20).build();
        this.addDrawableChild(saveButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double newScroll = scrollAmount - amount * 10; // 10 is scroll speed
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));

        this.clearChildren();
        this.init();

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264) { // Down arrow
            mouseScrolled(0, 0, -1);
            return true;
        } else if (keyCode == 265) { // Up arrow
            mouseScrolled(0, 0, 1);
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
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        // Enable scissor test to clip content outside the scrollable area
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.restrictionsInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.experienceInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.restrictionTypeButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantedBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantmentTypeBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowUnsmithing.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.doneButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 40;

        pGuiGraphics.drawTextWithShadow(this.textRenderer, "Restriction Type", x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFF);

        drawCenteredText(pGuiGraphics, this.textRenderer, new LiteralText("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderline(true)), this.width / 2, 4, 0xFFFFFF);
        drawStringWithShadow(pGuiGraphics, this.textRenderer, "Restriction Type", x, 18 + (this.textRenderer.fontHeight / 2) + 2, 0xFFFFFF);
        LiteralText format = new LiteralText("Restricted Items \n(Please write each item in new line)");
        this.textRenderer.drawTrimmed(format, x, 42 + (this.textRenderer.fontHeight / 2) + 20, textWidth,0xFFFFFF);
        pGuiGraphics.push();
        pGuiGraphics.scale(0.65f, 0.65f, 1f);
        pGuiGraphics.translate(x * 1.55, (42 * 1.6) + this.textRenderer.getStringBoundedHeight(format.getString(), textWidth) * 2 - (this.textRenderer.fontHeight * 0.65) + 40, 0);
        drawTextWithShadow(pGuiGraphics, this.textRenderer, new LiteralText("Format: modid:item_name / modid:* / modid:*_glass / modid:black_*"), 0, 0,11184810);
        pGuiGraphics.pop();
        pGuiGraphics.push();
        pGuiGraphics.scale(0.65f, 0.65f, 1f);
        pGuiGraphics.translate(x * 1.55, (42 * 1.6) + this.textRenderer.getStringBoundedHeight(format.getString(), textWidth) * 2 - (this.textRenderer.fontHeight * 0.65) + 50, 0);
        drawTextWithShadow(pGuiGraphics, this.textRenderer, new LiteralText("/ modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0,11184810);
        pGuiGraphics.pop();
        LiteralText allowEnchantedItem = new LiteralText("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        this.textRenderer.drawTrimmed(allowEnchantedItem, x, 135 + (this.textRenderer.fontHeight / 2) + 1 - this.textRenderer.getStringBoundedHeight(allowEnchantedItem.getString(), textWidth) / 4, textWidth, 0xFFFFFF);
        this.textRenderer.drawTrimmed(new LiteralText("Experience Type"), x, 158 + (this.textRenderer.fontHeight / 2) + 2, textWidth,0xFFFFFF);
        LiteralText expRequired = new LiteralText("Experience Required \n(Level/Point based on previous config)");
        this.textRenderer.drawTrimmed(expRequired, x, 183 + (this.textRenderer.fontHeight / 2) + 1 - this.textRenderer.getStringBoundedHeight(expRequired.getString(), textWidth) / 4, textWidth,0xFFFFFF);
        this.textRenderer.drawTrimmed(new LiteralText("Allow Unsmithing (Netherite/Trimmed Armor)"), x, 208 + (this.textRenderer.fontHeight / 2) + 2, textWidth, 0xFFFFFF);
        pGuiGraphics.drawTextWrapped(this.textRenderer, Text.literal("Allow Damaged Items to be Uncrafted (Reapair Item will be deducted)"), x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getWrappedLinesHeight(Text.literal("Allow Damaged Items to be Uncrafted (Reapair Item will be deducted)"), textWidth) / 4d), textWidth, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
    }
}