package org.tixsu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;

public class TxPing implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = TxPingConfig.MOD_ID;

	private static final int PANEL_HEIGHT = 32;

	// Colors
	private static final int PANEL_COLOR = 0x80000000;
	private static final int TEXT_WHITE_COLOR = 0xFFFFFFFF;

	@Override
	public void onInitialize() {
	}

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((graphics, delta) -> renderPing(graphics));
	}

	private static void renderPing(GuiGraphics graphics) {
		TxPingConfig config = TxPingConfig.get();

		if (!config.enabled) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null || minecraft.getConnection() == null) {
			return;
		}

		PlayerInfo playerInfo = minecraft.getConnection()
				.getPlayerInfo(minecraft.player.getUUID());

		int ping = playerInfo == null
				? 0
				: Math.max(playerInfo.getLatency(), 0);

		Font font = minecraft.font;

		String label = config.showLabel ? "Ping " : "";
		String value = String.valueOf(ping);

		int labelWidth = font.width(label);
		int valueWidth = font.width(value);
		int totalTextWidth = labelWidth + valueWidth;

		// Panel layout sizes
		int leftBoxWidth = 16;
		int rightBoxWidth = totalTextWidth + 12;

		// Use coordinates directly from config (drag system)
		int x = config.x;
		int y = config.y;

		int boxY = y + 1;
		int boxHeight = PANEL_HEIGHT - 20; // 12px

		// 1. Left panel (ping icon)
		int leftBoxX = x + 1;
		fillChamfered(graphics, leftBoxX, boxY, leftBoxWidth, boxHeight, PANEL_COLOR);

		// 4 white ping bars
		int iconWidth = 7;
		int iconHeight = 6;
		int iconX = leftBoxX + (leftBoxWidth - iconWidth) / 2;
		int iconBottomY = boxY + (boxHeight - iconHeight) / 2 + iconHeight;

		graphics.fill(iconX, iconBottomY - 2, iconX + 1, iconBottomY, TEXT_WHITE_COLOR);
		graphics.fill(iconX + 2, iconBottomY - 3, iconX + 3, iconBottomY, TEXT_WHITE_COLOR);
		graphics.fill(iconX + 4, iconBottomY - 5, iconX + 5, iconBottomY, TEXT_WHITE_COLOR);
		graphics.fill(iconX + 6, iconBottomY - 6, iconX + 7, iconBottomY, TEXT_WHITE_COLOR);

		// 2. Right panel (ping text)
		int rightBoxX = leftBoxX + leftBoxWidth + 2;
		fillChamfered(graphics, rightBoxX, boxY, rightBoxWidth, boxHeight, PANEL_COLOR);

		// Center text vertically in the right panel with a slight downward offset (+1 pixel)
		int textX = rightBoxX + (rightBoxWidth - totalTextWidth) / 2;
		int textY = boxY + (boxHeight - font.lineHeight) / 2 + 1;

		if (config.showLabel) {
			graphics.drawString(
					font,
					label,
					textX,
					textY,
					TEXT_WHITE_COLOR,
					false
			);
		}

		graphics.drawString(
				font,
				value,
				textX + labelWidth,
				textY,
				pingColor(ping),
				false
		);
	}

	private static int pingColor(int ping) {
		if (ping <= 50) {
			return 0xFF55FF00; // Green
		}

		if (ping <= 100) {
			return 0xFFFFD740; // Yellow
		}

		if (ping <= 150) {
			return 0xFFFF9F43; // Orange
		}

		return 0xFFFF4545; // Red
	}

	private static void fillChamfered(
			GuiGraphics graphics,
			int x,
			int y,
			int width,
			int height,
			int color
	) {
		graphics.fill(x + 1, y, x + width - 1, y + 1, color);
		graphics.fill(x, y + 1, x + width, y + height - 1, color);
		graphics.fill(
				x + 1, y + height - 1,
				x + width - 1, y + height,
				color
		);
	}
}
