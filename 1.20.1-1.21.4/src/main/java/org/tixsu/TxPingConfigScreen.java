package org.tixsu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
		super(Component.translatable("txping.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = (width - WIDGET_WIDTH) / 2;
		int startY = height / 2 - 20;

		addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
				.create(centerX, startY, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("txping.config.enabled"),
						(button, value) -> config.enabled = value));

		addRenderableWidget(CycleButton.onOffBuilder(config.showLabel)
				.create(centerX, startY + 28, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("txping.config.show_label"),
						(button, value) -> config.showLabel = value));

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(centerX, height - 40, WIDGET_WIDTH, WIDGET_HEIGHT)
				.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		TxPingBackground.render(this, graphics);
		super.render(graphics, mouseX, mouseY, partialTick);

		// Title and drag instruction
		graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF);
		graphics.drawCenteredString(font, Component.translatable("txping.config.drag_hint"), width / 2, 35, 0xFFAAAAAA);

		// Render draggable widget preview
		renderWidgetPreview(graphics, mouseX, mouseY);
	}

	private void renderWidgetPreview(GuiGraphics graphics, int mouseX, int mouseY) {
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
			graphics.fill(config.x - 2, config.y - 2, config.x + panelWidth + 2, config.y + PANEL_HEIGHT + 2, 0x66FFFFFF);
		}

		// 1. Left box (icon)
		int leftBoxWidth = 16;
		int leftBoxX = config.x + 1;
		fillChamfered(graphics, leftBoxX, boxY, leftBoxWidth, boxHeight, 0x80000000);

		// Ping bars
		int iconWidth = 7;
		int iconHeight = 6;
		int iconX = leftBoxX + (leftBoxWidth - iconWidth) / 2;
		int iconBottomY = boxY + (boxHeight - iconHeight) / 2 + iconHeight;
		int white = 0xFFFFFFFF;

		graphics.fill(iconX, iconBottomY - 2, iconX + 1, iconBottomY, white);
		graphics.fill(iconX + 2, iconBottomY - 3, iconX + 3, iconBottomY, white);
		graphics.fill(iconX + 4, iconBottomY - 5, iconX + 5, iconBottomY, white);
		graphics.fill(iconX + 6, iconBottomY - 6, iconX + 7, iconBottomY, white);

		// 2. Right box (text)
		String label = config.showLabel ? "PING " : "";
		String value = "42";
		int labelWidth = font.width(label);
		int valueWidth = font.width(value);
		int totalTextWidth = labelWidth + valueWidth;
		int rightBoxWidth = totalTextWidth + 12;
		int rightBoxX = leftBoxX + leftBoxWidth + 2;

		fillChamfered(graphics, rightBoxX, boxY, rightBoxWidth, boxHeight, 0x80000000);

		int textX = rightBoxX + (rightBoxWidth - totalTextWidth) / 2;
		int textY = boxY + (boxHeight - font.lineHeight) / 2 + 1;

		if (config.showLabel) {
			graphics.drawString(font, label, textX, textY, white, false);
		}
		graphics.drawString(font, value, textX + labelWidth, textY, 0xFF55FF00, false);
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

	private static void fillChamfered(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x + 1, y, x + width - 1, y + 1, color);
		graphics.fill(x, y + 1, x + width, y + height - 1, color);
		graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
	}
}
