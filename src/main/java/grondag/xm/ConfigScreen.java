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

import java.util.Arrays;
import java.util.stream.Collectors;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ConfigScreen {
	private static ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

	static Text[] parse(String key) {
		return Arrays.stream(I18n.translate("config.xblocks.help.force_key").split(";")).map(s ->  new LiteralText(s)).collect(Collectors.toList()).toArray(new Text[0]);
	}

	static Screen getScreen(Screen parent) {

		final ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("config.xm.title")).setSavingRunnable(ConfigScreen::saveUserInput);

		// DEBUG
		final ConfigCategory blocks = builder.getOrCreateCategory(new TranslatableText("config.xm.category.debug"));

		blocks.addEntry(ENTRY_BUILDER.startBooleanToggle(new TranslatableText("config.xm.value.debug_collision_boxes"), debugCollisionBoxes)
				.setDefaultValue(DEFAULTS.debugCollisionBoxes)
				.setSaveConsumer(b -> debugCollisionBoxes = b)
				.setTooltip(parse("config.xm.help.debug_collision_boxes"))
				.build());

		return builder.build();
	}

	private static void saveUserInput() {
		XmConfig.saveConfig();
	}
}
