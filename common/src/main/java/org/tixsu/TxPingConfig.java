package org.tixsu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxPingConfig {
	public static final String MOD_ID = "txping";

	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("txping.json");

	private static TxPingConfig instance;

	public boolean enabled = true;
	public boolean showLabel = true;
	public int x = 6;
	public int y = 6;

	public static TxPingConfig get() {
		if (instance == null) {
			instance = load();
		}

		return instance;
	}

	private static TxPingConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				TxPingConfig config = GSON.fromJson(reader, TxPingConfig.class);

				if (config != null) {
					return config;
				}
			} catch (Exception exception) {
				LOGGER.error("Failed to load config", exception);
			}
		}

		return new TxPingConfig();
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.write(PATH, GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
		} catch (Exception exception) {
			LOGGER.error("Failed to save config", exception);
		}
	}
}
