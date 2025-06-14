package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

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

    private ButtonWidget restrictionTypeButton;
    private ButtonWidget toggleEnchantedBtn;
    private ButtonWidget toggleEnchantmentTypeBtn;
    private ButtonWidget toggleAllowUnsmithing;
    private ButtonWidget toggleAllowDamaged;
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
        restrictionTypeButton = new ButtonWidget(x, (int) (baseY - scrollAmount), widgetWidth, 20, new TranslatableText("screen.uncrafteverything.config.restriction_type_" + restrictionType.toString().toLowerCase()), btn -> {
            UncraftEverythingConfig.RestrictionType[] values = UncraftEverythingConfig.RestrictionType.values();
            UncraftEverythingConfig.RestrictionType next = values[(restrictionType.ordinal() + 1) % values.length];
            restrictionType = next;
            btn.setMessage(new TranslatableText("screen.uncrafteverything.config.restriction_type_" + next.toString().toLowerCase()));
        });

        this.addChild(restrictionTypeButton);

        // Restrictions input box
        String joined = String.join("\n", restrictions);
        restrictionsInput = new MultiLineEditBox(this.textRenderer, x, (int) (baseY + 25 - scrollAmount), widgetWidth, 88, Integer.MAX_VALUE);
        restrictionsInput.setText(joined);
        this.addChild(restrictionsInput);

        // Toggle for allowEnchantedItems
        toggleEnchantedBtn = new ButtonWidget(x, (int) (baseY + 120 - scrollAmount), widgetWidth, 20, new TranslatableText(getLabel(allowEnchantedItems)), btn -> {
            allowEnchantedItems = !allowEnchantedItems;
            btn.setMessage(new TranslatableText(getLabel(allowEnchantedItems)));
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
        toggleAllowUnsmithing = new ButtonWidget(x, (int) (baseY + 195 - scrollAmount), widgetWidth, 20, new TranslatableText(getUnsmithingLabel(allowUnsmithing)), btn -> {
            allowUnsmithing = !allowUnsmithing;
            btn.setMessage(new TranslatableText(getUnsmithingLabel(allowUnsmithing)));
        });
        this.addChild(toggleAllowUnsmithing);

        toggleAllowDamaged = new ButtonWidget(x, (int) (baseY + 220 - scrollAmount), widgetWidth, 20, new TranslatableText(getDamagedLabel(allowDamagedItems)), btn -> {
            allowDamagedItems = !allowDamagedItems;
            btn.setMessage(new TranslatableText(getDamagedLabel(allowDamagedItems)));
        });
        this.addChild(toggleAllowDamaged);

        doneButton = new ButtonWidget(this.width / 2 - 100, this.height - 23, 200, 20, new TranslatableText("screen.uncrafteverything.save"), btn -> {
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
            this.client.openScreen(parent);
        });

        // Save button
        this.addChild(doneButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double newScroll = scrollAmount - amount * 10; // 10 is scroll speed
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));

        this.children().clear();
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

        int scale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
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

        drawTextWithShadow(pGuiGraphics, this.textRenderer, new TranslatableText("screen.uncrafteverything.config.restriction_type_label"), x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), 0xFFFFFF);
        TranslatableText format = new TranslatableText("screen.uncrafteverything.config.restricted_item_label");
        this.textRenderer.drawTrimmed(format, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 20), textWidth, 0xFFFFFF);
        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.format_label"), x, (int) (((baseY - scrollAmount)) + this.textRenderer.getStringBoundedHeight(format.getString(), textWidth) * 2 - (this.textRenderer.fontHeight * 0.65) + 40), textWidth,11184810);
        TranslatableText allowEnchantedItem = new TranslatableText("screen.uncrafteverything.config.allow_enchanted_label");
        this.textRenderer.drawTrimmed(allowEnchantedItem, x, (int) (baseY + 120 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(allowEnchantedItem.getString(), textWidth) / 4d), textWidth, 0xFFFFFF);
        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.exp_type_label"), x, (int) (baseY + 145 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFF);
        TranslatableText expRequired = new TranslatableText("screen.uncrafteverything.config.exp_required_label");
        this.textRenderer.drawTrimmed(expRequired, x, (int) (baseY + 170 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(expRequired.getString(), textWidth) / 4d), textWidth, 0xFFFFFF);
        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.allow_unsmithing_label"), x, (int) (baseY + 195 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFF);
        this.textRenderer.drawTrimmed(new TranslatableText("screen.uncrafteverything.config.allow_damaged_label"), x, (int) (baseY + 220 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 1 - this.textRenderer.getStringBoundedHeight(new TranslatableText("screen.uncrafteverything.config.allow_damaged_label").getString(), textWidth) / 4d), textWidth, 0xFFFFFF);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        drawCenteredText(pGuiGraphics, this.textRenderer, new TranslatableText("screen.uncrafteverything.uncraft_everything_config").setStyle(Style.EMPTY.withUnderline(true)), this.width / 2, 4, 0xFFFFFF);
        if (getMaxScroll() > 0) {
            int scrollBarHeight = Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) CONTENT_HEIGHT));
            int scrollBarY = (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 70 - scrollBarHeight));
            fill(pGuiGraphics, this.width - 6, scrollBarY, this.width, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
            fill(pGuiGraphics, this.width - 6, 25, this.width, this.height - 45, 0x44000000);
        }

        doneButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
    }
}