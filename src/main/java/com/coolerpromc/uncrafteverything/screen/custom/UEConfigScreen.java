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

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        // Restriction Type Config
        restrictionTypeButton = new ButtonWidget(x, 18, widgetWidth, 20, new LiteralText("Restriction Type: " + restrictionType), btn -> {
            UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
            UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
            restrictionType = next;
            btn.setMessage(new LiteralText("Restriction Type: " + next));
        });

        this.addChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.textRenderer, x, 42, widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new ButtonWidget(x, 135, widgetWidth, 20, new LiteralText(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new LiteralText(getLabel(allowEnchantedItems)));
        });
        this.addChild(toggleEnchantedBtn);

        // Toggle for enchantmentType
        toggleEnchantmentTypeBtn = new ButtonWidget(x, 158, widgetWidth, 20, new LiteralText("Experience Type: " + experienceType), btn -> {
            UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
            UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
            experienceType = next;
            btn.setMessage(new LiteralText("Experience Type: " + next));
        });
        this.addChild(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.textRenderer, x, 183, widgetWidth, 20, new LiteralText("Experience Input"));
        experienceInput.setText(Integer.toString(experience));
        experienceInput.setTextPredicate(s -> s.matches("\\d*")); // only digits allowed
        this.addChild(experienceInput);

        // Toggle for allowUnsmithing
        toggleAllowUnsmithing = new ButtonWidget(x, 208, widgetWidth, 20, new LiteralText(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new LiteralText(getUnsmithingLabel(allowUnsmithing)));
        });
        this.addChild(toggleAllowUnsmithing);

        doneButton = new ButtonWidget(this.width / 2 - 100, this.height - 23, 200, 20, new LiteralText("Done"), btn -> {
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

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing);
            ClientPlayNetworking.send(UEConfigPayload.TYPE, UEConfigPayload.encode(PacketByteBufs.create(), configPayload));
            ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
            this.client.openScreen(parent);
        });

        // Save button
        this.addChild(doneButton);
    }

    private String getLabel(boolean enabled) {
        return "Allow Enchanted Items: " + (enabled ? "Yes" : "No");
    }

    private String getUnsmithingLabel(boolean enabled) {
        return "Allow Unsmithing: " + (enabled ? "Yes" : "No");
    }

    @Override
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
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
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
    }
}