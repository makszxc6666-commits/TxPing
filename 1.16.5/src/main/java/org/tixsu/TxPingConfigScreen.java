package org.tixsu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TxPingConfigScreen extends Screen {
	private static final int WIDGET_WIDTH = 200;
	private static final int WIDGET_HEIGHT = 20;
	private static final int PANEL_HEIGHT = 32;

	private final Screen parent;
	private final TxPingConfig config = TxPingConfig.get();

	private boolean isDragging = false;
	private double dragOffsetX = 0;
	private double dragOffsetY = 0;

	public TxPingConfigScreen(Screen parent) {
		super(new TranslatableComponent("txping.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = (width - WIDGET_WIDTH) / 2;
		int startY = height / 2 - 20;

		addButton(new Button(centerX, startY, WIDGET_WIDTH, WIDGET_HEIGHT, enabledText(), button -> {
			config.enabled = !config.enabled;
			button.setMessage(enabledText());
		}));

		addButton(new Button(centerX, startY + 28, WIDGET_WIDTH, WIDGET_HEIGHT, showLabelText(), button -> {
			config.showLabel = !config.showLabel;
			button.setMessage(showLabelText());
		}));

		addButton(new Button(centerX, height - 40, WIDGET_WIDTH, WIDGET_HEIGHT,
				new TranslatableComponent("gui.done"), button -> onClose()));
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTick);

		// Title and drag instruction
		GuiComponent.drawCenteredString(poseStack, font, title, width / 2, 20, 0xFFFFFFFF);
		GuiComponent.drawCenteredString(poseStack, font, new TranslatableComponent("txping.config.drag_hint"),
				width / 2, 35, 0xFFAAAAAA);

		// Render draggable widget preview
		renderWidgetPreview(poseStack, mouseX, mouseY);
	}

	private Component enabledText() {
		return new TranslatableComponent("txping.config.enabled")
				.append(new TextComponent(": "))
				.append(new TextComponent(config.enabled ? "ON" : "OFF"));
	}

	private Component showLabelText() {
		return new TranslatableComponent("txping.config.show_label")
				.append(new TextComponent(": "))
				.append(new TextComponent(config.showLabel ? "ON" : "OFF"));
	}

	private void renderWidgetPreview(PoseStack poseStack, int mouseX, int mouseY) {
		Font font = this.font;
		int panelWidth = getPanelWidth();

		// Keep widget inside screen bounds
		config.x = Math.max(0, Math.min(config.x, width - panelWidth));
		config.y = Math.max(0, Math.min(config.y, height - PANEL_HEIGHT));

		int boxY = config.y + 1;
		int boxHeight = 12;

		// Check if mouse hovers over the widget
		boolean hovered = mouseX >= config.x && mouseX <= config.x + panelWidth &&
				mouseY >= config.y && mouseY <= config.y + PANEL_HEIGHT;

		// Draw selection outline when hovered or dragging
		if (hovered || isDragging) {
			GuiComponent.fill(poseStack, config.x - 2, config.y - 2,
					config.x + panelWidth + 2, config.y + PANEL_HEIGHT + 2, 0x66FFFFFF);
		}

		// 1. Left box (icon)
		int leftBoxWidth = 16;
		int leftBoxX = config.x + 1;
		fillChamfered(poseStack, leftBoxX, boxY, leftBoxWidth, boxHeight, 0x80000000);

		// Ping bars
		int iconWidth = 7;
		int iconHeight = 6;
		int iconX = leftBoxX + (leftBoxWidth - iconWidth) / 2;
		int iconBottomY = boxY + (boxHeight - iconHeight) / 2 + iconHeight;
		int white = 0xFFFFFFFF;

		GuiComponent.fill(poseStack, iconX, iconBottomY - 2, iconX + 1, iconBottomY, white);
		GuiComponent.fill(poseStack, iconX + 2, iconBottomY - 3, iconX + 3, iconBottomY, white);
		GuiComponent.fill(poseStack, iconX + 4, iconBottomY - 5, iconX + 5, iconBottomY, white);
		GuiComponent.fill(poseStack, iconX + 6, iconBottomY - 6, iconX + 7, iconBottomY, white);

		// 2. Right box (text)
		String label = config.showLabel ? "PING " : "";
		String value = "42";
		int labelWidth = font.width(label);
		int valueWidth = font.width(value);
		int totalTextWidth = labelWidth + valueWidth;
		int rightBoxWidth = totalTextWidth + 12;
		int rightBoxX = leftBoxX + leftBoxWidth + 2;

		fillChamfered(poseStack, rightBoxX, boxY, rightBoxWidth, boxHeight, 0x80000000);

		int textX = rightBoxX + (rightBoxWidth - totalTextWidth) / 2;
		int textY = boxY + (boxHeight - font.lineHeight) / 2 + 1;

		if (config.showLabel) {
			font.draw(poseStack, label, textX, textY, white);
		}
		font.draw(poseStack, value, textX + labelWidth, textY, 0xFF55FF00);
	}

	private int getPanelWidth() {
		String label = config.showLabel ? "PING " : "";
		String value = "42";
		int labelWidth = font.width(label);
		int valueWidth = font.width(value);
		int leftBoxWidth = 16;
		int rightBoxWidth = labelWidth + valueWidth + 12;
		return leftBoxWidth + rightBoxWidth + 4;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int panelWidth = getPanelWidth();
			if (mouseX >= config.x && mouseX <= config.x + panelWidth &&
					mouseY >= config.y && mouseY <= config.y + PANEL_HEIGHT) {
				isDragging = true;
				dragOffsetX = mouseX - config.x;
				dragOffsetY = mouseY - config.y;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (isDragging && button == 0) {
			int panelWidth = getPanelWidth();
			config.x = (int) (mouseX - dragOffsetX);
			config.y = (int) (mouseY - dragOffsetY);
			config.x = Math.max(0, Math.min(config.x, width - panelWidth));
			config.y = Math.max(0, Math.min(config.y, height - PANEL_HEIGHT));
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			isDragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void onClose() {
		config.save();
		minecraft.setScreen(parent);
	}

	private static void fillChamfered(PoseStack poseStack, int x, int y, int width, int height, int color) {
		GuiComponent.fill(poseStack, x + 1, y, x + width - 1, y + 1, color);
		GuiComponent.fill(poseStack, x, y + 1, x + width, y + height - 1, color);
		GuiComponent.fill(poseStack, x + 1, y + height - 1, x + width - 1, y + height, color);
	}
}
