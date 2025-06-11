package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.opengl.GL11;

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
    private boolean allowDamagedItems = config.allowDamaged();

    // Scroll variablesAdd commentMore actions
    private double scrollAmount = 0.0;
    private final int CONTENT_HEIGHT = 240;

    private Button restrictionTypeButton;
    private Button toggleEnchantedBtn;
    private Button toggleEnchantmentTypeBtn;
    private Button toggleAllowUnsmithing;
    private Button toggleAllowDamaged;
    private Button doneButton;

    private MultiLineEditBox restrictionsInput;
    private TextFieldWidget experienceInput;

    protected UEConfigScreen(ITextComponent title, Screen parent) {
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
        restrictionTypeButton = new Button(x, (int) (baseY - scrollAmount), widgetWidth, 20, new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), btn -> {
            UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
            UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
            restrictionType = next;
            btn.setMessage(new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
        });

        this.addWidget(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.font, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addWidget(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new Button(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new TranslationTextComponent(getLabel(allowEnchantedItems)));
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
        toggleAllowUnsmithing = new Button(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new TranslationTextComponent(getUnsmithingLabel(allowUnsmithing)));
        });
        this.addWidget(toggleAllowUnsmithing);

        toggleAllowDamaged = new Button(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20, new TranslationTextComponent(getDamagedLabel(allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(new TranslationTextComponent(getDamagedLabel(allowDamagedItems)));
        });
        this.addWidget(toggleAllowDamaged);

        doneButton = new Button(this.width / 2 - 100, this.height - 23, 200, 20, new TranslationTextComponent("screen.uncrafteverything.save"), btn -> {
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

            UEConfigPayload configPayload = new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItems, experienceType, expValue, allowUnsmithing, allowDamagedItems);
            UEConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
            RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
            this.getMinecraft().setScreen(parent);
        });
        this.addWidget(doneButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        double newScroll = scrollAmount - verticalAmount * 10; // 10 is scroll speed
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));

        this.children.clear();
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
        return "screen.uncrafteverything.config.allow_enchanted_" + (enabled ? "yes" : "no");
    }

    private String getUnsmithingLabel(boolean enabled) {
        return "screen.uncrafteverything.config.allow_unsmithing_" + (enabled ? "yes" : "no");
    }

    private String getDamagedLabel(boolean enabled) {
        return "screen.uncrafteverything.config.allow_damaged_" + (enabled ? "yes" : "no");
    }

    @Override
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        // Enable scissor test to clip content outside the scrollable area
        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int clipTop = 45;
        int clipBottom = this.height - 25;
        int clipHeight = clipBottom - clipTop;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, clipTop * scale, this.width * scale, clipHeight * scale);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.restrictionsInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.experienceInput.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.restrictionTypeButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantedBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleEnchantmentTypeBtn.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowUnsmithing.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toggleAllowDamaged.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.doneButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 40;

        drawString(pGuiGraphics, this.font, new TranslationTextComponent("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), 0xFFFFFFFF);

        TranslationTextComponent format = new TranslationTextComponent("screen.uncrafteverything.config.restricted_item_label");
        font.drawWordWrap(format, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 20), textWidth,0xFFFFFFFF);

        pGuiGraphics.pushPose();
        pGuiGraphics.scale(0.65f, 0.65f, 0f);
        pGuiGraphics.translate(x * 1.55, ((baseY + 25 - scrollAmount) * 1.6) + this.font.wordWrapHeight(format.getString(), textWidth) * 2 - (this.font.lineHeight * 0.65) + 40, 0);
        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.format_label"), 0, 0, (int) (textWidth * 1.5),0xFFAAAAAA);
        pGuiGraphics.popPose();

        TranslationTextComponent allowEnchantedItem = new TranslationTextComponent("screen.uncrafteverything.config.allow_enchanted_label");
        font.drawWordWrap(allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(allowEnchantedItem.getString(), textWidth) / 4d), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth,0xFFFFFFFF);

        TranslationTextComponent expRequired = new TranslationTextComponent("screen.uncrafteverything.config.exp_required_label");
        font.drawWordWrap(expRequired, x, (int) (baseY + 170 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(expRequired.getString(), textWidth) / 4d), textWidth,0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        font.drawWordWrap(new TranslationTextComponent("screen.uncrafteverything.config.allow_damaged_label"), x, (int) (baseY + 220 - scrollAmount + (this.font.lineHeight / 2d) + 1 - this.font.wordWrapHeight(new TranslationTextComponent("screen.uncrafteverything.config.allow_damaged_label").getString(), textWidth) / 4d), textWidth, 0xFFFFFF);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw title and scroll indicator outside scissor area
        drawCenteredString(pGuiGraphics, this.font, new TranslationTextComponent("screen.uncrafteverything.uncraft_everything_config").setStyle(Style.EMPTY.withUnderlined(true)), this.width / 2, 4, 0xFFFFFF);

        // Draw scroll indicator if content overflows
        if (getMaxScroll() > 0) {
            int scrollBarHeight = Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) CONTENT_HEIGHT));
            int scrollBarY = (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 70 - scrollBarHeight));
            fill(pGuiGraphics,this.width - 6, scrollBarY, this.width, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
            fill(pGuiGraphics,this.width - 6, 25, this.width, this.height - 45, 0x44000000);
        }

        doneButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
    }
}
