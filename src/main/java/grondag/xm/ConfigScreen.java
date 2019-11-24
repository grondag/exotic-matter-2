/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm;

import static grondag.xm.XmConfig.DEFAULTS;
import static grondag.xm.XmConfig.debugCollisionBoxes;

import java.util.Optional;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

@Environment(EnvType.CLIENT)
public class ConfigScreen {
	static Screen getScreen(Screen parent) {

		final ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("config.xm.title").setSavingRunnable(ConfigScreen::saveUserInput);

		// DEBUG
		final ConfigCategory blocks = builder.getOrCreateCategory("config.xm.category.debug");

		blocks.addEntry(new BooleanListEntry("config.xm.value.debug_collision_boxes", debugCollisionBoxes, "config.xm.reset", () -> DEFAULTS.debugCollisionBoxes,
				b -> debugCollisionBoxes = b, () -> Optional.of(I18n.translate("config.xm.help.debug_collision_boxes").split(";"))));

		return builder.build();
	}

	private static void saveUserInput() {
		XmConfig.saveConfig();
	}
}
