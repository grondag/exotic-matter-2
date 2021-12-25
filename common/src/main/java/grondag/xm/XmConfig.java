/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm;

import java.io.File;
import java.io.FileOutputStream;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.loader.api.FabricLoader;

// WIP: remove Fabric deps
@Internal
public class XmConfig {
	public static class ConfigData {
		// DEBUG
		@SuppressWarnings("hiding")
		@Comment("Draw detailed collision boxes normally rendered by vanilla. Can be ugly - useful for debugging.")
		public boolean debugCollisionBoxes = false;
	}

	public static final ConfigData DEFAULTS = new ConfigData();
	private static final Gson GSON = new GsonBuilder().create();
	private static final Jankson JANKSON = Jankson.builder().build();
	private static File configFile;

	// DEBUG
	public static boolean debugCollisionBoxes = DEFAULTS.debugCollisionBoxes;

	// reserved
	public static int maxPlacementCheckCount = 32;
	public static boolean logExcavationRenderTracking;

	//TODO: implement and make default = true
	public static boolean simplifyTerrainBlockGeometry = false;

	public static void init() {
		configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "exotic-matter.json5");

		if (configFile.exists()) {
			loadConfig();
		} else {
			saveConfig();
		}
	}

	private static void loadConfig() {
		ConfigData config = new ConfigData();

		try {
			final JsonObject configJson = JANKSON.load(configFile);
			final String regularized = configJson.toJson(false, false, 0);
			config = GSON.fromJson(regularized, ConfigData.class);
		} catch (final Exception e) {
			e.printStackTrace();
			Xm.LOG.error("Unable to load config. Using default values.");
		}

		// DEBUG
		debugCollisionBoxes = config.debugCollisionBoxes;
	}

	public static void saveConfig() {
		final ConfigData config = new ConfigData();

		// DEBUG
		config.debugCollisionBoxes = debugCollisionBoxes;

		try {
			final String result = JANKSON.toJson(config).toJson(true, true, 0);

			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			try (FileOutputStream out = new FileOutputStream(configFile, false);) {
				out.write(result.getBytes());
				out.flush();
				out.close();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Xm.LOG.error("Unable to save config.");
			return;
		}
	}
}
