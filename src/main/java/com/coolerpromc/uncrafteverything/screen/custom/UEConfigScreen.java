package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UEConfigScreen extends Screen {
    private final Screen parent;
    private final ResponseConfigPayload config = ClientPayloadHandler.payloadFromServer;

    private UncraftEverythingConfig.ExperienceType experienceType = config.experienceType();
    private final int experience = config.experience();
    private UncraftEverythingConfig.RestrictionType restrictionType = config.restrictionType();
    private final List<? extends String> restrictions = config.restrictedItems();
    private boolean allowEnchantedItems = config.allowEnchantedItem();
    private boolean allowUnsmithing = config.allowUnsmithing();

    private Button restrictionTypeButton;
    private Button toggleEnchantedBtn;
    private Button toggleEnchantmentTypeBtn;
    private Button toggleAllowUnsmithing;
    private Button doneButton;

    private MultiLineEditBox restrictionsInput;
    private TextFieldWidget experienceInput;

    protected UEConfigScreen(ITextComponent title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        // Restriction Type Config
        restrictionTypeButton = new Button(x, 18, widgetWidth, 20, new StringTextComponent("Restriction Type: " + restrictionType), btn -> {
            UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
            UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
            restrictionType = next;
            btn.setMessage(new StringTextComponent("Restriction Type: " + next));
        });

        this.addWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.font, x, 42, widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new Button(x, 135, widgetWidth, 20, new StringTextComponent(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new StringTextComponent(getLabel(allowEnchantedItems)));
        });
        this.addWidget(toggleEnchantedBtn);

        // Toggle for enchantmentType
        toggleEnchantmentTypeBtn = new Button(x, 158, widgetWidth, 20, new StringTextComponent("Experience Type: " + experienceType), btn -> {
            UncraftEverythingConfig.ExperienceType[] values = UncraftEverythingConfig.ExperienceType.values();
            UncraftEverythingConfig.ExperienceType next = values[(experienceType.ordinal() + 1) % values.length];
            experienceType = next;
            btn.setMessage(new StringTextComponent("Experience Type: " + next));
        });
        this.addWidget(toggleEnchantmentTypeBtn);

        // Experience input box
        experienceInput = new TextFieldWidget(this.font, x, 183, widgetWidth, 20, new StringTextComponent("Experience Input"));
        experienceInput.setValue(Integer.toString(experience));
        experienceInput.setFilter(s -> s.matches("\\d*")); // only digits allowed
        this.addWidget(experienceInput);

        // Toggle for allowUnsmithing
        toggleAllowUnsmithing = new Button(x, 208, widgetWidth, 20, new StringTextComponent(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new StringTextComponent(getUnsmithingLabel(allowUnsmithing)));
        });
        this.addWidget(toggleAllowUnsmithing);

        doneButton = new Button(this.width / 2 - 100, this.height - 23, 200, 20, new StringTextComponent("Done"), btn -> {
            String[] entries = restrictionsInput.getText().split("\n");
            List<String> restrictedItems = Arrays.stream(entries)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            int expValue = this.experience;
            try {
                expValue = Integer.parseInt(experienceInput.getValue());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid experience value, using default: " + this.experience);
            }

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing);
            UEConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
            RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
            this.getMinecraft().setScreen(parent);
        });

        // Save button
        this.addWidget(doneButton);
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

        drawCenteredString(pGuiGraphics, this.font, new StringTextComponent("Uncraft Everything Config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFF);
        drawString(pGuiGraphics, this.font, "Restriction Type", x, 18 + (this.font.lineHeight / 2) + 2, 0xFFFFFF);
        StringTextComponent format = new StringTextComponent("Restricted Items \n(Please write each item in new line)");
        this.font.drawWordWrap(format, x, 42 + (this.font.lineHeight / 2) + 20, textWidth,0xFFFFFF);
        pGuiGraphics.pushPose();
        pGuiGraphics.scale(0.65f, 0.65f, 1f);
        pGuiGraphics.translate(x * 1.55, (42 * 1.6) + this.font.wordWrapHeight(format.getString(), textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 0);
        drawString(pGuiGraphics, this.font, new StringTextComponent("Format: modid:item_name / modid:* / modid:*_glass / modid:black_*"), 0, 0,11184810);
        pGuiGraphics.popPose();
        pGuiGraphics.pushPose();
        pGuiGraphics.scale(0.65f, 0.65f, 1f);
        pGuiGraphics.translate(x * 1.55, (42 * 1.6) + this.font.wordWrapHeight(format.getString(), textWidth) * 2 - (this.font.lineHeight * 0.65) + 50, 0);
        drawString(pGuiGraphics, this.font, new StringTextComponent("/ modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name"), 0, 0,11184810);
        pGuiGraphics.popPose();
        StringTextComponent allowEnchantedItem = new StringTextComponent("Allow Enchanted Item to be Uncrafted (Enchanted Book will be given)");
        this.font.drawWordWrap(allowEnchantedItem, x, 135 + (this.font.lineHeight / 2) + 1 - this.font.wordWrapHeight(allowEnchantedItem.getString(), textWidth) / 4, textWidth, 0xFFFFFF);
        this.font.drawWordWrap(new StringTextComponent("Experience Type"), x, 158 + (this.font.lineHeight / 2) + 2, textWidth,0xFFFFFF);
        StringTextComponent expRequired = new StringTextComponent("Experience Required \n(Level/Point based on previous config)");
        this.font.drawWordWrap(expRequired, x, 183 + (this.font.lineHeight / 2) + 1 - this.font.wordWrapHeight(expRequired.getString(), textWidth) / 4, textWidth,0xFFFFFF);
        this.font.drawWordWrap(new StringTextComponent("Allow Unsmithing (Netherite/Trimmed Armor)"), x, 208 + (this.font.lineHeight / 2) + 2, textWidth, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }
}
